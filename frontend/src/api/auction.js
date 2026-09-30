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

// 경매 검색 - GET /auction/search?keyword&status&organization&startDate&endDate&page&size
// keyword는 상품명·경매번호 부분 일치, status는 DB 값(대기/진행/완료), organization은 기관명 완전 일치.
// startDate/endDate는 AuctionSearchDto의 LocalDateTime에 바인딩되므로 ISO 날짜시간으로 보낸다. page/size 필수.
export async function searchAuctions({ keyword, status, organization, startDate, endDate, page = 0, size = 10 } = {}) {
  return toPage(
    await api.get('/auction/search', {
      keyword,
      status,
      organization,
      startDate: startDate ? `${startDate}T00:00:00` : undefined,
      endDate: endDate ? `${endDate}T23:59:59` : undefined,
      page,
      size,
    }),
  )
}
