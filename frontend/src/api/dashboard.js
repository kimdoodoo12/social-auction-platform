import { api } from './client'

// 대시보드 API (backend dashboard/controller/DashboardController.java)
// GET /ieum/admin/dashboard → DashboardResponse
//   newProductDtos / endProductDtos: List<products.user.dto.ProductDto>
//     { auctionStatus, image, organizationName, productName, currentPrice, endTime, bidCount }
//     - newProductDtos: 입찰이 없는 최근 등록 상품 5개 (currentPrice = 시작가)
//     - endProductDtos: 진행 중인 경매 5개 (종료 시각 기준)
//   paymentListResponses: List<PaymentListResponse>
//     { auctionId, productName, organizationName, memeberName, paymentPrice, createdAt, paymentStatus }
//   productCount, auctionWaitngCount, auctionBiddingCount, auctionEndCount, memberCount, organizationCount
// 백엔드 필드명 오타·대소문자(auctionWaitngCount, memeberName, ProductName, OrganizationCount)는 여기서 한 번만 맞춘다.
const normalizeProduct = (p) => ({ ...p, productName: p.productName ?? p.ProductName })

export async function fetchDashboard() {
  const d = (await api.get('/ieum/admin/dashboard')) ?? {}
  return {
    counts: {
      products: d.productCount ?? 0,
      waiting: d.auctionWaitngCount ?? 0,
      bidding: d.auctionBiddingCount ?? 0,
      ended: d.auctionEndCount ?? 0,
      members: d.memberCount ?? 0,
      organizations: d.organizationCount ?? d.OrganizationCount ?? 0,
    },
    newProducts: (d.newProductDtos ?? []).map(normalizeProduct),
    liveAuctions: (d.endProductDtos ?? []).map(normalizeProduct),
    recentWins: (d.paymentListResponses ?? []).map((p) => ({ ...p, memberName: p.memberName ?? p.memeberName })),
  }
}
