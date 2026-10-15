import { useCallback, useEffect, useRef, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { authApi } from '../admin/member/authApi'
import { organizationFilePath } from '../../api/organization'
import { formatDate, formatDateTime, formatNumber, formatPrice } from '../../utils/format'
import { userAuctionApi } from './auctionApi'
import { useAuctionLive } from './useAuctionLive'
import { useNow } from './useNow'
import { remainingClock } from './catalogHelpers'
import { AUCTION_DURATION_LABEL, BID_UNIT, maskedName, relativeTime, STATUS_BADGE } from './auctionDetailHelpers'
import BidConfirmModal from './BidConfirmModal'
import './auction-detail.css'
import UserHeader from './UserHeader'
import UserFooter from './UserFooter'
import { CatalogState, ProductGrid, ProductImage } from './CatalogParts'
import { useAsync } from '../../hooks/useAsync'

// [USER] 04 상세 진행 (Figma 25:2415) / 03 상세 대기 (25:2559) / 05 입찰 모달 (25:2673)
// 사용자 결정
// * 첫 입찰은 시작가부터, 진행 중 입찰은 현재 최고가 + 1,000원부터 가능하다.
// - 경로의 카테고리는 상세 API에 없어 '-'
// - 배송 · 환불 안내 탭은 데이터가 없어 비활성
// - 관심 상품 담기는 API가 없어 비활성 (찜 버튼과 같은 처리)

const THUMB_SLOTS = 4
const DETAIL_SHOTS = 2
const RELATED_CARDS = 4

function PreviewControls({ auctionId, connection, streamError, events }) {
  function changeAuction(event) {
    event.preventDefault()
    const value = new FormData(event.currentTarget).get('auctionId')?.trim()
    if (/^[1-9]\d*$/.test(value)) window.location.assign(`preview.html?auctionId=${encodeURIComponent(value)}`)
  }
  return (
    <aside className="ua-auction-preview" aria-label="SSE 점검 도구">
      <form onSubmit={changeAuction}>
        <strong>SSE 점검</strong>
        <label>경매 ID <input name="auctionId" type="number" min="1" defaultValue={auctionId} /></label>
        <button type="submit">이동</button>
      </form>
      <p>연결: <strong>{connection === 'connected' ? '연결됨' : connection === 'connecting' ? '연결 중' : '재연결 중'}</strong> · 최근 이벤트 {events.length}건</p>
      {streamError && <p role="alert">{streamError}</p>}
      {events.map((event, index) => (
        <p key={`${event.receivedAt}-${index}`}>{event.receivedAt} · priceUpdate · {formatPrice(event.price)} · 회원 ID {String(event.bidderName)}</p>
      ))}
    </aside>
  )
}

function Gallery({ images, title }) {
  const [selected, setSelected] = useState(0)
  return (
    <div className="ud-gallery">
      <div className="ud-gallery-main"><ProductImage image={images[selected]} alt={title} /></div>
      <div className="ud-gallery-thumbs" aria-label="상품 이미지 선택">
        {Array.from({ length: THUMB_SLOTS }, (_, i) => (images[i] ? (
          <button type="button" key={images[i]} aria-label={`상품 이미지 ${i + 1}`} aria-pressed={selected === i}
            className={selected === i ? 'selected' : ''} onClick={() => setSelected(i)}>
            <ProductImage image={images[i]} alt="" />
          </button>
        ) : (
          <span key={`empty-${i}`} className="ud-gallery-empty"><ProductImage image={null} alt="" /></span>
        )))}
      </div>
    </div>
  )
}

function BidHistory({ bids, now, connection }) {
  if (!bids.length) {
    return (
      <section className="ud-history ud-history-empty">
        <span className="ud-history-empty-icon" aria-hidden="true" />
        <strong>아직 입찰 내역이 없습니다</strong>
        <p>첫 입찰이 등록되면 이 자리에 실시간으로 쌓입니다.</p>
      </section>
    )
  }
  return (
    <section className="ud-history">
      <div className="ud-history-head">
        <h2>입찰 내역</h2>
        <small>{connection === 'reconnecting' ? '실시간 연결을 다시 시도하는 중' : '새 입찰이 들어오면 자동 갱신'}</small>
      </div>
      {bids.slice(0, 5).map((bid, index) => (
        <div className={`ud-history-row${index === 0 ? ' top' : ''}`} key={`${bid.bidNumber}-${bid.memberId}-${bid.bidPrice}`}>
          <span className="ud-history-who">{maskedName(bid.memberName)}{index === 0 && <em>최고가</em>}</span>
          <strong>{formatPrice(bid.bidPrice)}</strong>
          <small>{relativeTime(bid.bidTime, now)}</small>
        </div>
      ))}
    </section>
  )
}

function DetailTabs({ product, auction, images }) {
  const [tab, setTab] = useState('description')
  const tabs = [['description', '상품 상세 설명'], ['background', '상품 제작 배경'], ['organization', '제작 기관 소개']]
  return (
    <section className="ud-lower shop-width">
      <div className="ud-tabs" role="tablist" aria-label="상품 정보">
        {tabs.map(([key, label]) => (
          <button key={key} type="button" role="tab" aria-selected={tab === key} className={tab === key ? 'active' : ''} onClick={() => setTab(key)}>
            {label}
          </button>
        ))}
        {/* 배송·환불 정보 API가 없어 비활성 (사용자 결정) */}
        <button type="button" role="tab" aria-selected={false} disabled title="준비 중인 기능입니다.">배송 · 환불 안내</button>
      </div>
      <div className="ud-tab-body">
        <div className="ud-tab-content" role="tabpanel">
          {tab === 'description' && (
            <>
              <h2>상품 상세 설명</h2>
              <p>{product?.description || auction.productContent || '상품 설명이 없습니다.'}</p>
              <div className="ud-detail-shots">
                {Array.from({ length: DETAIL_SHOTS }, (_, i) => (
                  <div key={i} className="ud-detail-shot"><ProductImage image={images[i]} alt="" /></div>
                ))}
              </div>
            </>
          )}
          {tab === 'background' && <><h2>상품 제작 배경</h2><p>{product?.background || '제작 배경이 없습니다.'}</p></>}
          {tab === 'organization' && <><h2>제작 기관 소개</h2><p>{product?.organizationDescription || '기관 소개가 없습니다.'}</p></>}
        </div>

        <aside className="ud-maker">
          <p className="ud-maker-label">제작 기관</p>
          <div className="ud-maker-org">
            <span className="ud-maker-logo"><ProductImage image={organizationFilePath(product?.organizationImage)} alt="" /></span>
            <div>
              <strong>{product?.organizationName || '-'}</strong>
              <small>협약일 {formatDate(product?.agreementDate)}  ·  등록 상품 {formatNumber(product?.registeredProductCount)}개</small>
            </div>
          </div>
          <p className="ud-maker-desc">{product?.organizationDescription || '기관 소개가 없습니다.'}</p>
          {product?.organizationId
            ? <Link className="ud-maker-link" to={`/products?organizationId=${product.organizationId}`}>기관이 등록한 다른 상품 보기 →</Link>
            : <span className="ud-maker-link">기관이 등록한 다른 상품 보기 →</span>}
        </aside>
      </div>
    </section>
  )
}

// 함께 보면 좋은 상품: GET /ieum/user/auction/product/{productId}/related (최대 4개)
// 응답에 지금 보고 있는 상품이 섞여 오면 화면에서 뺀다.
function RelatedSection({ productId, auctionId, organizationId }) {
  const { data, loading, error, reload } = useAsync(
    () => (productId ? userAuctionApi.getRelated(productId) : Promise.resolve([])),
    [productId],
  )
  const products = (Array.isArray(data) ? data : []).filter((p) => p.auctionId !== auctionId).slice(0, RELATED_CARDS)
  return (
    <section className="ud-related">
      <div className="shop-width">
        <div className="shop-section-heading">
          <div>
            <h2>함께 보면 좋은 상품</h2>
            <p>같은 카테고리이거나 같은 기관에서 등록한 상품입니다.</p>
          </div>
          {organizationId ? <Link to={`/products?organizationId=${organizationId}`}>전체보기 →</Link> : null}
        </div>
        <CatalogState loading={loading || !productId} error={error} empty={!products.length} onRetry={reload}
          emptyTitle="함께 보여 드릴 상품이 아직 없어요." emptyText="같은 기관이나 카테고리에 새 상품이 등록되면 이곳에 표시됩니다.">
          <ProductGrid products={products} />
        </CatalogState>
      </div>
    </section>
  )
}

function AuctionContent({ auctionId, preview }) {
  const pendingBid = useRef(null)
  const waitTimer = useRef(null)
  const receivedEventCount = useRef(0)
  const latestPriceEvent = useRef(null)
  const now = useNow()
  const [updatedAt, setUpdatedAt] = useState(() => Date.now())
  const [enteredAmount, setEnteredAmount] = useState(null)
  const [modalOpen, setModalOpen] = useState(false)
  const [bidBusy, setBidBusy] = useState(false)
  const [awaitingEvent, setAwaitingEvent] = useState(false)
  const [bidMessage, setBidMessage] = useState(null) // { tone: 'ok' | 'error', text, login? }
  const [shareMessage, setShareMessage] = useState('')

  const onPriceUpdate = useCallback(({ price, bidderName }) => {
    receivedEventCount.current += 1
    latestPriceEvent.current = { price, bidderName }
    setUpdatedAt(Date.now())
    const pending = pendingBid.current
    if (pending && pending.price === price && pending.memberId === Number(bidderName)) {
      pending.sseReceived = true
      if (pending.accepted === true) {
        pendingBid.current = null
        clearTimeout(waitTimer.current)
        setAwaitingEvent(false)
        setBidMessage({ tone: 'ok', text: '입찰이 정상적으로 접수되었습니다.' })
      }
    }
  }, [])

  const { auction, product, images, bids, loading, error, connection, streamError, events } =
    useAuctionLive(auctionId, onPriceUpdate)

  useEffect(() => () => clearTimeout(waitTimer.current), [])

  if (loading && !auction) {
    return <main className="shop-width ud-page"><div className="shop-state" role="status">경매 정보를 불러오고 있어요…</div></main>
  }
  if (!auction) {
    return <main className="shop-width ud-page"><div className="shop-state" role="alert"><strong>{error || '경매 상품을 찾지 못했습니다.'}</strong></div></main>
  }

  const waiting = auction.status === '대기'
  const live = auction.status === '진행'
  const ended = auction.status === '완료' || (live && auction.endTime && new Date(auction.endTime).getTime() <= now)
  const badge = ended ? STATUS_BADGE.완료 : STATUS_BADGE[auction.status] ?? { label: auction.status || '상태 확인 중', tone: 'waiting' }
  const currentPrice = Number(auction.topPrice ?? auction.startPrice ?? 0)
  const minimumBid = waiting ? Number(auction.startPrice) : currentPrice + BID_UNIT
  const amount = enteredAmount === null ? minimumBid : Number(enteredAmount)
  const validBid = (waiting || live) && !ended && enteredAmount !== '' && Number.isSafeInteger(amount) && amount >= minimumBid
  const title = product?.productName || auction.productName || `경매 #${auctionId}`
  const organizationName = product?.organizationName || '제작 기관'

  async function confirmBid() {
    setBidBusy(true)
    setBidMessage(null)
    try {
      const member = await authApi.me().catch(() => null)
      if (!member?.memberId) {
        setModalOpen(false)
        setBidMessage({ tone: 'error', text: '입찰하려면 먼저 로그인해 주세요.', login: true })
        return
      }
      const pending = { price: amount, memberId: Number(member.memberId), accepted: null, sseReceived: false, eventCountAtSubmit: receivedEventCount.current }
      pendingBid.current = pending
      // * 회원 ID는 내 SSE 이벤트 확인에만 사용하며, 입찰자는 서버가 쿠키로 확정한다.
      const accepted = await userAuctionApi.placeBid({ auctionId, price: amount })
      setModalOpen(false)
      pending.accepted = accepted
      if (accepted === false) {
        pendingBid.current = null
        setBidMessage({ tone: 'error', text: '입찰이 거절되었습니다. 현재 최고가와 종료 시각을 확인해 주세요.' })
      } else if (pending.sseReceived) {
        pendingBid.current = null
        setEnteredAmount(null)
        setBidMessage({ tone: 'ok', text: '입찰이 정상적으로 접수되었습니다.' })
      } else {
        setEnteredAmount(null)
        setAwaitingEvent(true)
        setBidMessage({ tone: 'ok', text: '입찰이 접수되었습니다. 최신 가격을 확인하고 있어요.' })
        clearTimeout(waitTimer.current)
        waitTimer.current = setTimeout(() => {
          if (pendingBid.current !== pending) return
          pendingBid.current = null
          setAwaitingEvent(false)
          setBidMessage(receivedEventCount.current === pending.eventCountAtSubmit
            ? { tone: 'ok', text: '입찰은 접수되었지만 실시간 가격을 받지 못했습니다. 새로고침해 확인해 주세요.' }
            : { tone: 'ok', text: '입찰은 접수되었고, 그 사이 다른 입찰이 들어왔습니다. 현재 최고가를 확인해 주세요.' })
        }, 8000)
      }
    } catch (caught) {
      pendingBid.current = null
      clearTimeout(waitTimer.current)
      setAwaitingEvent(false)
      setModalOpen(false)
      setBidMessage({ tone: 'error', text: caught.message || '입찰 요청에 실패했습니다.' })
    } finally {
      setBidBusy(false)
    }
  }

  async function share() {
    try {
      await navigator.clipboard.writeText(window.location.href)
      setShareMessage('상품 링크를 복사했습니다.')
    } catch {
      setShareMessage('링크를 복사하지 못했습니다. 주소창의 링크를 복사해 주세요.')
    }
  }

  const bidPanel = (
    <section className={`ud-panel ${waiting ? 'pre' : 'live'}`}>
      <div className="ud-panel-head">
        <span>{waiting ? '시작가' : ended ? '최종 입찰가' : '현재 최고 입찰가'}</span>
        <small>{waiting ? `입찰 ${formatNumber(auction.bidCount ?? 0)}회  ·  경매 시작 전` : `총 ${formatNumber(auction.bidCount ?? 0)}회 입찰  ·  입찰자 ${formatNumber(auction.bidMemberCount ?? 0)}명`}</small>
      </div>
      <strong className="ud-panel-price">{formatPrice(currentPrice)}</strong>

      {waiting && (
        <div className="ud-notice">
          <strong>아직 경매가 시작되지 않았습니다.</strong>
          <p>첫 입찰이 등록되는 순간부터 {AUCTION_DURATION_LABEL} 동안 경매가 진행됩니다.<br />지금 입찰하시면 회원님이 이 상품의 첫 입찰자가 됩니다.</p>
        </div>
      )}

      <dl className="ud-stats">
        <div><dt>시작가</dt><dd>{formatPrice(auction.startPrice)}</dd></div>
        <div><dt>최소 입찰 단위</dt><dd>{formatPrice(BID_UNIT)}</dd></div>
        {waiting
          ? <div><dt>경매 기간</dt><dd>첫 입찰 후 {AUCTION_DURATION_LABEL}</dd></div>
          : <div><dt>남은 시간</dt><dd className="accent">{ended ? '종료' : remainingClock(auction.endTime, now)}</dd></div>}
      </dl>

      {!waiting && (
        <p className="ud-panel-note">
          {auction.endTime ? `${formatDateTime(auction.endTime)} ${ended ? '종료' : '종료 예정'}` : '종료 시각 미정'}  ·  마지막 갱신 {relativeTime(new Date(updatedAt).toISOString(), now)}
        </p>
      )}

      <div className="ud-bid-row">
        <label className="ud-bid-input">
          <span className="ud-sr-only">입찰 금액</span>
          <input type="text" inputMode="numeric" value={enteredAmount === '' ? '' : formatNumber(amount)} disabled={ended}
            onChange={(event) => setEnteredAmount(event.target.value.replace(/\D/g, ''))} />
          <span>원</span>
        </label>
        <button type="button" className="shop-button" disabled={!validBid || bidBusy || awaitingEvent} onClick={() => setModalOpen(true)}>
          {waiting ? '첫 입찰하기' : '입찰하기'}
        </button>
      </div>
      {ended ? (
        <p className="ud-bid-error">경매가 종료되어 더 이상 입찰할 수 없습니다.</p>
      ) : (
        <p className="ud-bid-rule">
          {formatPrice(minimumBid)} 이상부터 입찰할 수 있습니다.
          {waiting ? ' (첫 입찰은 시작가로 가능합니다.)' : ` (현재가 ${formatPrice(currentPrice)} + 최소 입찰 단위 ${formatPrice(BID_UNIT)})`}
        </p>
      )}
      {!ended && enteredAmount !== null && !validBid && <p className="ud-bid-error">{formatPrice(minimumBid)} 이상 입력해 주세요.</p>}
      {bidMessage && (
        <p className={`ud-bid-message ${bidMessage.tone}`} role="status">
          {bidMessage.text}{bidMessage.login && <> <Link to="/login">로그인하기 →</Link></>}
        </p>
      )}
      <p className="ud-panel-foot">입찰 후에는 취소할 수 없습니다.</p>
    </section>
  )

  const related = <RelatedSection productId={product?.productId} auctionId={auctionId} organizationId={product?.organizationId} />
  const lower = <DetailTabs product={product} auction={auction} images={images} />

  return (
    <>
      <main className="ud-page">
        <div className="shop-width">
          {preview && <PreviewControls auctionId={auctionId} connection={connection} streamError={streamError} events={events} />}
          {/* 상세 API에 카테고리가 없어 '-' (사용자 결정) */}
          <p className="ud-breadcrumb"><Link to="/">홈</Link> › - › {title}</p>

          <div className="ud-main">
            <Gallery images={images} title={title} />

            <div className="ud-column">
              <div className="ud-status-row">
                <span className={`ud-badge ${badge.tone}`}>{badge.label}</span>
                <span>◦ {organizationName}  ·  협약 기관</span>
              </div>
              <h1>{title}</h1>
              <p className="ud-summary">{product?.description || auction.productContent || '상품 설명을 준비 중입니다.'}</p>
              <hr />
              {bidPanel}
              <BidHistory bids={bids} now={now} connection={connection} />
              <div className="ud-sub-actions">
                <button type="button" className="shop-button secondary" disabled title="준비 중인 기능입니다.">♡  관심 상품 담기</button>
                <button type="button" className="shop-button secondary" onClick={share}>공유하기</button>
              </div>
              {shareMessage && <p className="ud-share-message" role="status">{shareMessage}</p>}
            </div>
          </div>
        </div>

        {/* Figma: 대기 화면은 추천 상품이 탭보다 위, 진행 화면은 아래 */}
        {waiting ? <>{related}{lower}</> : <>{lower}{related}</>}
      </main>

      {modalOpen && (
        <BidConfirmModal title={title} organizationName={organizationName} image={images[0]} currentPrice={currentPrice}
          currentLabel={waiting ? '시작가' : '현재 최고 입찰가'} amount={amount} busy={bidBusy}
          onClose={() => setModalOpen(false)} onConfirm={confirmBid} />
      )}
    </>
  )
}

export default function AuctionDetailPage({ auctionId, preview = false }) {
  const id = Number(auctionId)
  if (!Number.isSafeInteger(id) || id < 1) return <p role="alert">올바른 경매 ID를 입력해 주세요.</p>
  return <div className="shop"><UserHeader /><AuctionContent key={id} auctionId={id} preview={preview} /><UserFooter /></div>
}

export function AuctionRoutePage() {
  const { auctionId } = useParams()
  return <AuctionDetailPage auctionId={auctionId} />
}
