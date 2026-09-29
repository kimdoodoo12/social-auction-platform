import { api, toPage } from './client'

// 경매 API (backend auction/controller/AuctionController.java)

// 경매 목록 (auctionId 오름차순) - GET /auction?page&size
// 항목: AuctionFindAllDto { auctionId, productName, organizationName, startPrice, topPrice, bidCount, startTime, endTime }
export async function fetchAuctions({ page = 0, size = 10 } = {}) {
  return toPage(await api.get('/auction', { page, size }))
}

// 경매 상세 - GET /auction/detail/{id}
// 응답: AuctionDetailDto { auctionFindAllDto, categoryId, categoryName, imageList, manager, agreementDate, userName }
export function fetchAuctionDetail(auctionId) {
  return api.get(`/auction/detail/${auctionId}`)
}
