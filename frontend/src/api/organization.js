import { api, toPage } from './client'

// 기관 관리 API (backend: organization/controller/admin/OrganizationController.java)
const BASE = '/ieum/admin/organization'

// 목록. 검색 조건이 하나라도 있으면 /search, 없으면 전체 목록을 호출한다.
// page는 0부터 시작한다. 응답: Page<OrganizationListResponse>
export async function fetchOrganizations({ name, agreementStatus, page = 0, size = 10 } = {}) {
  const hasFilter = Boolean(name) || agreementStatus === true || agreementStatus === false
  const path = hasFilter ? `${BASE}/search` : BASE
  const data = await api.get(path, { name, agreementStatus, page, size })
  return toPage(data)
}

// 상세. 응답: { organizationInfoResponse, organizationProductResponse: [] }
export async function fetchOrganizationDetail(id) {
  const data = await api.get(`${BASE}/detail/${id}`)
  return {
    info: data?.organizationInfoResponse ?? null,
    products: data?.organizationProductResponse ?? [],
  }
}

// 등록/수정 요청 본문은 OrganizationInfoRequest 필드와 같다. 두 API 모두 boolean을 반환한다.
export function createOrganization(body) {
  return api.post(BASE, body)
}

export function updateOrganization(id, body) {
  return api.put(`${BASE}/detail/${id}`, body)
}
