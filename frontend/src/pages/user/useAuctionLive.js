import { useEffect, useState } from 'react'
import { userAuctionApi } from './auctionApi'

function readPriceUpdate(data) {
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

export function useAuctionLive(auctionId, onPriceUpdate) {
  const [auction, setAuction] = useState(null)
  const [product, setProduct] = useState(null)
  const [images, setImages] = useState([])
  const [imageError, setImageError] = useState('')
  const [bids, setBids] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [connection, setConnection] = useState('connecting')
  const [streamError, setStreamError] = useState('')
  const [events, setEvents] = useState([])
  useEffect(() => {
    const controller = new AbortController()
    let active = true
    let latestSnapshot = null
    let latestAppliedPrice = -1

    async function loadInitialData() {
      const [auctionResult, productResult, bidsResult, imagesResult] = await Promise.allSettled([
        userAuctionApi.getAuction(auctionId, controller.signal),
        userAuctionApi.getProduct(auctionId, controller.signal),
        userAuctionApi.getBids(auctionId, controller.signal),
        userAuctionApi.getImages(auctionId, controller.signal),
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
      if (imagesResult.status === 'fulfilled' && Array.isArray(imagesResult.value)) setImages(imagesResult.value)
      else setImageError('상품 이미지를 불러오지 못했습니다.')
      setLoading(false)
    }

    void loadInitialData()

    const source = userAuctionApi.subscribe(auctionId)
    source.onopen = () => {
      if (active) {
        setConnection('connected')
        setStreamError('')
      }
    }
    source.onerror = () => {
      if (active) {
        setConnection('reconnecting')
        setStreamError('SSE 연결 오류 또는 만료로 재연결 중입니다.')
      }
    }

    function receivePrice(event) {
      if (!active) return
      try {
        const snapshot = readPriceUpdate(event.data)
        const { price, bidderName } = snapshot

        setConnection('connected')
        setStreamError('')
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

    return () => {
      active = false
      controller.abort()
      source.removeEventListener('priceUpdate', receivePrice)
      source.close()
    }
  }, [auctionId, onPriceUpdate])

  return {
    auction,
    product,
    images,
    imageError,
    bids,
    loading,
    error,
    connection,
    streamError,
    events,
  }
}
