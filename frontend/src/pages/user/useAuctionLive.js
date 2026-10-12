import { useEffect, useState } from 'react'
import { userAuctionApi } from './auctionApi'

// * SSE 문자열 데이터를 파싱하고 화면에서 사용할 가격 변경 정보인지 검증한다.
function readPriceUpdate(data) {
  // ! 서버 이벤트의 형식을 확인한 뒤에만 화면 상태에 반영한다.
  const payload = JSON.parse(data)
  const price = Number(payload.price)
  const bidCount = Number(payload.bidCount)
  const bidMemberCount = Number(payload.bidMemberCount)
  if (!Number.isSafeInteger(price) || price < 0 ||
      !Number.isSafeInteger(bidCount) || bidCount < 0 ||
      !Number.isSafeInteger(bidMemberCount) || bidMemberCount < 0 ||
      typeof payload.status !== 'string' ||
      (payload.endTime !== null && typeof payload.endTime !== 'string') ||
      !Array.isArray(payload.recentBids)) {
    throw new Error('Invalid priceUpdate payload')
  }
  return { price, bidCount, bidMemberCount, status: payload.status,
    endTime: payload.endTime, recentBids: payload.recentBids,
    bidderName: payload.bidderName }
}

// * 기존 경매 정보에 SSE 최신 값을 덮어써 화면용 경매 객체를 만든다.
function applySnapshot(auction, snapshot) {
  return {
    ...auction,
    topPrice: snapshot.price,
    bidCount: snapshot.bidCount,
    bidMemberCount: snapshot.bidMemberCount,
    status: snapshot.status,
    endTime: snapshot.endTime,
  }
}

// * 경매 상세 화면에 초기 경매 정보를 불러오고, SSE 입찰 이벤트로 가격과 입찰 내역을 실시간 갱신한다.
export function useAuctionLive(auctionId, onPriceUpdate) {
  const [auction, setAuction] = useState(null)
  const [product, setProduct] = useState(null)
  const [bids, setBids] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [connection, setConnection] = useState('connecting')
  const [streamError, setStreamError] = useState('')
  const [events, setEvents] = useState([])
  useEffect(() => {
    const controller = new AbortController()
    let active = true
    // * 초기 조회와 SSE가 동시에 진행될 때 더 최신인 가격을 유지한다.
    let latestSnapshot = null
    let latestAppliedPrice = -1

    // * 경매, 상품, 입찰 내역을 병렬로 불러와 화면의 첫 상태를 준비한다.
    async function loadInitialData() {
      const [auctionResult, productResult, bidsResult] = await Promise.allSettled([
        userAuctionApi.getAuction(auctionId, controller.signal),
        userAuctionApi.getProduct(auctionId, controller.signal),
        userAuctionApi.getBids(auctionId, controller.signal),
      ])
      if (!active) return

      if (auctionResult.status === 'fulfilled' && auctionResult.value) {
        const fetched = auctionResult.value
        const fetchedPrice = Number(fetched.topPrice ?? fetched.startPrice ?? 0)
        // * 초기 조회보다 SSE가 먼저 도착해도 새 가격·입찰 내역을 덮어쓰지 않는다.
        const useEventSnapshot = latestSnapshot && latestSnapshot.price >= fetchedPrice
        latestAppliedPrice = Math.max(latestAppliedPrice, fetchedPrice)
        setAuction(useEventSnapshot ? applySnapshot(fetched, latestSnapshot) : fetched)
        if (useEventSnapshot) setBids(latestSnapshot.recentBids)
        setError('')
      } else {
        setError('경매 정보를 불러오지 못했습니다. 경매 ID와 백엔드 연결을 확인해 주세요.')
      }
      if (productResult.status === 'fulfilled') setProduct(productResult.value)
      if (bidsResult.status === 'fulfilled' && Array.isArray(bidsResult.value) &&
          (!latestSnapshot || latestSnapshot.price < Number(auctionResult.value?.topPrice ?? auctionResult.value?.startPrice ?? 0))) {
        setBids(bidsResult.value)
      }
      setLoading(false)
    }

    void loadInitialData()

    // * 경매별 EventSource 연결을 열고 서버의 가격 변경 이벤트를 기다린다.
    const source = userAuctionApi.subscribe(auctionId)
    // * SSE 연결이 열리면 화면의 연결 상태와 오류 문구를 초기화한다.
    source.onopen = () => {
      if (active) {
        setConnection('connected')
        setStreamError('')
      }
    }
    // * SSE 오류 또는 타임아웃을 표시한다. EventSource는 자동 재연결을 시도한다.
    source.onerror = () => {
      if (active) {
        // * EventSource가 재연결을 시도하는 동안 연결 상태를 표시한다.
        setConnection('reconnecting')
        setStreamError('SSE 연결 오류 또는 만료로 재연결 중입니다.')
      }
    }

    // * 서버의 priceUpdate 이벤트를 검증하고 최신 경매 정보와 입찰 내역을 반영한다.
    function receivePrice(event) {
      if (!active) return
      try {
        const snapshot = readPriceUpdate(event.data)
        const { price, bidderName } = snapshot

        setConnection('connected')
        setStreamError('')
        // * 늦게 도착한 낮은 가격으로 현재 경매 정보가 덮어써지지 않게 한다.
        if (price >= latestAppliedPrice) {
          latestSnapshot = snapshot
          latestAppliedPrice = price
          setAuction((previous) => previous ? applySnapshot(previous, snapshot) : previous)
          setBids(snapshot.recentBids)
        }
        setEvents((previous) => [{
          price,
          bidderName,
          receivedAt: new Date().toLocaleTimeString('ko-KR'),
        }, ...previous].slice(0, 5))
        onPriceUpdate?.({ price, bidderName })
      } catch {
        setStreamError('priceUpdate 이벤트의 가격·입찰 횟수·내역 형식이 올바르지 않습니다.')
      }
    }

    source.addEventListener('priceUpdate', receivePrice)

    // * 컴포넌트가 사라지거나 경매 ID가 바뀔 때 실행되는 정리 함수다.
    return () => {
      // * 경매 변경 또는 화면 이탈 시 요청과 구독을 정리해 중복 갱신을 막는다.
      active = false
      controller.abort()
      source.removeEventListener('priceUpdate', receivePrice)
      source.close()
    }
  }, [auctionId, onPriceUpdate])

  // * 화면에서 사용할 데이터와 로딩·오류·연결 상태를 한 객체로 제공한다.
  return {
    auction,
    product,
    bids,
    loading,
    error,
    connection,
    streamError,
    events,
  }
}
