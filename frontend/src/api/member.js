import { api, toPage } from './client'

// 회원 API (backend member/controller/MemberController.java)

// 전체 회원 목록 (memberId 내림차순) - GET /admin/user/manage?page&size
export async function fetchMembers({ page = 0, size = 10 } = {}) {
  return toPage(await api.get('/admin/user/manage', { page, size }))
}

// 조건 검색 - GET /admin/user/search?name&email&role&startDate&endDate&page&size
// name/email/role은 완전 일치 비교다. startDate/endDate는 MemberSearchDto의 LocalDateTime 필드에
// @ModelAttribute로 바인딩되므로 ISO 날짜시간(yyyy-MM-ddTHH:mm:ss) 문자열로 보낸다.
// page/size는 백엔드에서 필수 파라미터다.
export async function searchMembers({ name, email, role, startDate, endDate, page = 0, size = 10 } = {}) {
  return toPage(
    await api.get('/admin/user/search', {
      name,
      email,
      role,
      startDate: startDate ? `${startDate}T00:00:00` : undefined,
      endDate: endDate ? `${endDate}T23:59:59` : undefined,
      page,
      size,
    }),
  )
}

// 회원 상세 + 입찰 내역(bidDtos) + 결제 내역(payDtos) - GET /user/detail/info/{userid}
export function fetchMemberDetail(userId) {
  return api.get(`/user/detail/info/${userId}`)
}

// 회원 정지 (role을 '정지'로 변경) - PUT /user/stop/{userid}, 응답: boolean
export function suspendMember(userId) {
  return api.put(`/user/stop/${userId}`)
}
