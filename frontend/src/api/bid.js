import { api, toPage } from './client'

// 입찰 API (backend bid/controller/BidController.java)

// 경매별 입찰 기록 (bidTime 내림차순) - GET /bid/detail?auctionId&page&size
// 항목: BidDto { bidId, name, memberId, bidPrice, bidTime }
export async function fetchBids({ auctionId, page = 0, size = 7 }) {
  return toPage(await api.get('/bid/detail', { auctionId, page, size }))
}

// 입찰 결과(가장 최근 입찰 기준) - GET /bid/bidDetail/{id}
// 응답: BidResultDto { auctionId, name, phone, bidPrice, endTime, paymentStatus } 또는 입찰이 없으면 빈 본문(null)
export function fetchBidResult(auctionId) {
  return api.get(`/bid/bidDetail/${auctionId}`)
}
