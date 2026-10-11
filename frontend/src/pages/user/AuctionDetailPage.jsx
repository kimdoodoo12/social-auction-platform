import { useCallback, useEffect, useRef, useState } from 'react'
import { useParams } from 'react-router-dom'
import { authApi } from '../admin/member/authApi'
import { formatDateTime, formatNumber, formatPrice } from '../../utils/format'
import { userAuctionApi } from './auctionApi'
import { useAuctionLive } from './useAuctionLive'
import BidConfirmModal from './BidConfirmModal'
import './auction-detail.css'

function remainingText(endTime, now) {
  if (!endTime) return '-'
  const seconds = Math.max(0, Math.ceil((new Date(endTime).getTime() - now) / 1000))
  const hours = String(Math.floor(seconds / 3600)).padStart(2, '0')
  const minutes = String(Math.floor((seconds % 3600) / 60)).padStart(2, '0')
  return `${hours}:${minutes}:${String(seconds % 60).padStart(2, '0')}`
}

function maskedName(name) {
  if (!name) return '회원'
  return `${name.slice(0, 1)}${'*'.repeat(Math.max(1, name.length - 1))}`
}

function statusText(status) {
  if (status === '대기') return '경매 대기'
  if (status === '진행') return '경매 진행 중'
  if (status === '완료') return '경매 종료'
  return status || '상태 확인 중'
}

function PreviewControls({ auctionId, connection, streamError, events }) {
  function changeAuction(event) {
    event.preventDefault()
    const value = new FormData(event.currentTarget).get('auctionId')?.trim()
    if (/^[1-9]\d*$/.test(value)) {
      window.location.assign(`preview.html?auctionId=${encodeURIComponent(value)}`)
    }
  }

  return (
    <aside className="ua-auction-preview" aria-label="SSE 점검 도구">
      <form onSubmit={changeAuction}>
        <strong>SSE 점검</strong>
        <label>경매 ID <input name="auctionId" type="number" min="1" defaultValue={auctionId} /></label>
        <button type="submit">이동</button>
      </form>
      <p>연결: <strong>{connection === 'connected' ? '연결됨' : connection === 'connecting' ? '연결 중' : '재연결 중 · CORS/서버 확인'}</strong> · 최근 이벤트: {events.length}건 표시</p>
      {streamError && <p role="alert">{streamError}</p>}
      <p className="ua-auction-preview-note">백엔드는 초기 이벤트를 보내지 않습니다. 연결 후 입찰이 성공하면 아래에 priceUpdate가 표시됩니다.</p>
      {events.map((event, index) => (
        <p key={`${event.receivedAt}-${index}`}>
          {event.receivedAt} · priceUpdate · {formatPrice(event.price)} · 회원 ID {String(event.bidderName)}
        </p>
      ))}
    </aside>
  )
}

function AuctionContent({ auctionId, preview }) {
  const pendingBid = useRef(null)
  const waitTimer = useRef(null)
  const receivedEventCount = useRef(0)
  const latestPriceEvent = useRef(null)
  const [now, setNow] = useState(0)
  const [enteredAmount, setEnteredAmount] = useState(null)
  const [modalOpen, setModalOpen] = useState(false)
  const [bidBusy, setBidBusy] = useState(false)
  const [awaitingEvent, setAwaitingEvent] = useState(false)
  const [bidMessage, setBidMessage] = useState('')
  const [activeTab, setActiveTab] = useState('description')

  const onPriceUpdate = useCallback(({ price, bidderName }) => {
    receivedEventCount.current += 1
    latestPriceEvent.current = { price, bidderName }
    const pending = pendingBid.current
    if (pending && pending.price === price && pending.memberId === Number(bidderName)) {
      pending.sseReceived = true
      if (pending.accepted === true) {
        pendingBid.current = null
        clearTimeout(waitTimer.current)
        setAwaitingEvent(false)
        setBidMessage('입찰 성공(true) · 내 입찰의 priceUpdate 이벤트도 확인했습니다.')
      }
    }
  }, [])

  const { auction, product, bids, loading, error, connection, streamError, events } =
    useAuctionLive(auctionId, onPriceUpdate)

  useEffect(() => {
    const timer = setInterval(() => setNow(Date.now()), 1000)
    return () => clearInterval(timer)
  }, [])

  useEffect(() => () => clearTimeout(waitTimer.current), [])

  if (loading && !auction) {
    return <main className="ua-auction-page"><p role="status">경매 정보를 불러오는 중입니다…</p></main>
  }
  if (!auction) {
    return <main className="ua-auction-page"><p role="alert">{error || '경매 상품을 찾지 못했습니다.'}</p></main>
  }

  const waiting = auction.status === '대기'
  const live = auction.status === '진행'
  const ended = auction.status === '완료' || (live && now > 0 && auction.endTime && new Date(auction.endTime).getTime() <= now)
  const currentPrice = Number(auction.topPrice ?? auction.startPrice ?? 0)
  // ! 현재 백엔드는 첫 입찰도 topPrice보다 1,000원 이상 높아야 받는다.
  const minimumBid = currentPrice + 1000
  const amount = enteredAmount === null ? minimumBid : Number(enteredAmount)
  const validBid = (waiting || live) && !ended && enteredAmount !== '' &&
    Number.isSafeInteger(amount) && amount >= minimumBid
  const title = product?.productName || auction.productName || `경매 #${auctionId}`

  async function confirmBid() {
    setBidBusy(true)
    setBidMessage('')
    try {
      const member = await authApi.me()
      if (!member?.memberId) {
        setModalOpen(false)
        setBidMessage('입찰하려면 먼저 로그인해야 합니다.')
        return
      }

      const pending = {
        price: amount,
        memberId: Number(member.memberId),
        accepted: null,
        sseReceived: false,
        eventCountAtSubmit: receivedEventCount.current,
      }
      pendingBid.current = pending
      const accepted = await userAuctionApi.placeBid({ auctionId, memberId: pending.memberId, price: amount })
      setModalOpen(false)
      pending.accepted = accepted

      if (accepted === false) {
        pendingBid.current = null
        setBidMessage('입찰이 거절되었습니다(false). SSE로 받은 현재 최고가와 종료 시각을 확인해 주세요.')
      } else if (pending.sseReceived) {
        pendingBid.current = null
        setBidMessage('입찰 성공(true) · 내 입찰의 priceUpdate 이벤트도 확인했습니다.')
      } else {
        setAwaitingEvent(true)
        setBidMessage('입찰 성공(true). SSE priceUpdate 이벤트를 기다리고 있습니다.')
        clearTimeout(waitTimer.current)
        waitTimer.current = setTimeout(() => {
          if (pendingBid.current === pending) {
            pendingBid.current = null
            setAwaitingEvent(false)
            if (receivedEventCount.current === pending.eventCountAtSubmit) {
              setBidMessage('입찰은 성공했지만 priceUpdate 수신은 확인되지 않았습니다. SSE 연결 상태를 확인해 주세요.')
            } else {
              const latest = latestPriceEvent.current
              setBidMessage(`입찰은 성공했고 priceUpdate도 수신했지만 내 입찰과 일치하지 않습니다. 최근 이벤트: ${formatPrice(latest.price)} / 회원 ID ${String(latest.bidderName)}.`)
            }
          }
        }, 8000)
      }
    } catch (caught) {
      pendingBid.current = null
      clearTimeout(waitTimer.current)
      setAwaitingEvent(false)
      setModalOpen(false)
      setBidMessage(caught.message || '입찰 요청에 실패했습니다.')
    } finally {
      setBidBusy(false)
    }
  }

  return (
    <>
      <header className="ua-auction-header">
        <div className="ua-auction-header-main">
          <strong className="ua-auction-brand"><span aria-hidden="true" /> 이음옥션</strong>
          <input aria-label="상품 검색" placeholder="상품명 또는 제작 기관을 검색해 보세요" disabled />
          <nav aria-label="회원 메뉴"><a href="/login">로그인</a><a href="/signup">회원가입</a><a href="/mypage">마이페이지</a></nav>
        </div>
        <div className="ua-auction-categories">전체 카테고리 · 수공예 · 식품·가공 · 원예 · 생활잡화 · 의류·패브릭</div>
      </header>

      <main className="ua-auction-page">
        {preview && <PreviewControls auctionId={auctionId} connection={connection} streamError={streamError} events={events} />}
        <p className="ua-auction-breadcrumb">홈 › 경매 상품 › {title}</p>

        <div className="ua-auction-top">
          <div className="ua-auction-gallery">
            <div className="ua-auction-image" role="img" aria-label={`${title} 이미지 준비 중`}>▧</div>
            <div className="ua-auction-thumbnails" aria-label="상품 이미지 준비 중">
              {[0, 1, 2, 3].map((number) => <span key={number} className={number === 0 ? 'selected' : ''}>▧</span>)}
            </div>
          </div>

          <div className="ua-auction-info">
            <div className="ua-auction-meta"><span className={live && !ended ? 'live' : ''}>{ended ? '경매 종료' : statusText(auction.status)}</span> · {product?.organizationName || '제작 기관'}</div>
            <h1>{title}</h1>
            <p className="ua-auction-description">{product?.description || auction.productContent || '상품 설명을 준비 중입니다.'}</p>

            <section className={`ua-auction-bid-card ${live ? 'live' : ''}`}>
              <div className="ua-auction-bid-heading">
                <span>{waiting ? '시작가' : '현재 최고 입찰가'}</span>
                <small>총 {formatNumber(auction.bidCount ?? 0)}회 입찰 · 입찰자 {formatNumber(auction.bidMemberCount ?? 0)}명</small>
              </div>
              <strong className="ua-auction-current-price">{formatPrice(currentPrice)}</strong>

              {waiting && <div className="ua-auction-waiting-message"><strong>아직 경매가 시작되지 않았습니다.</strong><p>첫 입찰이 등록되면 경매가 시작됩니다.</p></div>}

              <div className="ua-auction-stats">
                <div><span>시작가</span><strong>{formatPrice(auction.startPrice)}</strong></div>
                <div><span>현재 최소 입찰가</span><strong>{formatPrice(minimumBid)}</strong></div>
                <div><span>남은 시간</span><strong className="time">{waiting ? '시작 전' : !auction.endTime ? '종료 시각 미수신' : now === 0 ? '계산 중' : remainingText(auction.endTime, now)}</strong></div>
              </div>

              <div className="ua-auction-bid-form">
                <label><span className="ua-auction-sr-only">입찰 금액</span><input type="text" inputMode="numeric" value={enteredAmount === '' ? '' : formatNumber(amount)} onChange={(event) => setEnteredAmount(event.target.value.replace(/\D/g, ''))} disabled={ended} /><span>원</span></label>
                <button type="button" disabled={!validBid || bidBusy || awaitingEvent} onClick={() => setModalOpen(true)}>{waiting ? '첫 입찰하기' : '입찰하기'}</button>
              </div>
              <p className="ua-auction-small">{formatPrice(minimumBid)} 이상부터 입찰할 수 있습니다. 입찰 후 취소할 수 없습니다.</p>
              {!validBid && !ended && enteredAmount !== null && <p className="ua-auction-error">현재 최소 입찰가 이상을 입력해 주세요.</p>}
              {ended && <p className="ua-auction-error">경매가 종료되었거나 종료 시각이 지났습니다.</p>}
              {bidMessage && <p className="ua-auction-feedback" role="status">{bidMessage}</p>}
            </section>

            <section className="ua-auction-history">
              <div className="ua-auction-history-heading"><strong>입찰 내역</strong><small>최근 5건 · SSE로 갱신</small></div>
              {bids.length === 0 ? <p className="ua-auction-empty">아직 입찰 내역이 없습니다.</p> : bids.map((bid, index) => (
                <div className={`ua-auction-history-row ${index === 0 ? 'highest' : ''}`} key={`${bid.bidNumber}-${bid.memberId}`}>
                  <span>{maskedName(bid.memberName)} {index === 0 && <em>최고가</em>}</span>
                  <strong>{formatPrice(bid.bidPrice)}</strong>
                  <small>{formatDateTime(bid.bidTime)}</small>
                </div>
              ))}
            </section>
            <div className="ua-auction-connection" role="status">
              SSE: {connection === 'connected' ? '연결됨' : connection === 'connecting' ? '연결 중' : '재연결 중'} · 최근 수신 {events.length}건
              {events[0] && <span> · 최근 {formatPrice(events[0].price)} / 회원 ID {String(events[0].bidderName)}</span>}
              {streamError && <p>{streamError}</p>}
            </div>
          </div>
        </div>

        <section className="ua-auction-details">
          <div className="ua-auction-tabs" role="tablist" aria-label="상품 정보">
            {[
              ['description', '상품 상세 설명'],
              ['background', '상품 제작 배경'],
              ['organization', '제작 기관 소개'],
            ].map(([key, label]) => <button key={key} type="button" role="tab" aria-selected={activeTab === key} className={activeTab === key ? 'active' : ''} onClick={() => setActiveTab(key)}>{label}</button>)}
          </div>
          <div className="ua-auction-detail-body" role="tabpanel">
            {activeTab === 'description' && <><h2>상품 상세 설명</h2><p>{product?.description || auction.productContent || '설명이 없습니다.'}</p></>}
            {activeTab === 'background' && <><h2>상품 제작 배경</h2><p>{product?.background || '제작 배경이 없습니다.'}</p></>}
            {activeTab === 'organization' && <><h2>{product?.organizationName || '제작 기관'}</h2><p>{product?.organizationDescription || '기관 소개가 없습니다.'}</p><p>등록 상품 {formatNumber(product?.registeredProductCount)}개</p></>}
          </div>
        </section>
      </main>

      {modalOpen && <BidConfirmModal title={title} currentPrice={currentPrice} amount={amount} busy={bidBusy} onClose={() => setModalOpen(false)} onConfirm={confirmBid} />}
    </>
  )
}

export default function AuctionDetailPage({ auctionId, preview = false }) {
  const id = Number(auctionId)
  if (!Number.isSafeInteger(id) || id < 1) return <p role="alert">올바른 경매 ID를 입력해 주세요.</p>
  return <AuctionContent key={id} auctionId={id} preview={preview} />
}

// App.jsx 라우트에 나중에 연결할 때 사용하는 컴포넌트다.
export function AuctionRoutePage() {
  const { auctionId } = useParams()
  return <AuctionDetailPage auctionId={auctionId} />
}
