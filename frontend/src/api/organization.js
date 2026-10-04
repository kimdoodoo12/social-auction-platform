import { api, BASE_URL, toPage } from './client'

// 기관 관리 API (backend: organization/controller/admin/OrganizationController.java)
const BASE = '/ieum/admin/organization'

// 업로드 파일은 백엔드 static/organization/ 아래에 "UUID_원본파일명"으로 저장된다 (FileService).
const FILE_DIR = '/organization/'

// 목록. 검색 조건이 하나라도 있으면 /search, 없으면 전체 목록을 호출한다.
// page는 0부터 시작한다. 응답: Page<OrganizationListResponse>
export async function fetchOrganizations({ name, agreementStatus, page = 0, size = 10 } = {}) {
  const hasFilter = Boolean(name) || agreementStatus === true || agreementStatus === false
  const path = hasFilter ? `${BASE}/search` : BASE
  const data = await api.get(path, { name, agreementStatus, page, size })
  return toPage(data)
}

// 상세. 응답: { organizationInfoResponse, organizationProductResponse: [] }
// organizationInfoResponse의 파일 필드: organizationImageFileName, agreementFileName
export async function fetchOrganizationDetail(id) {
  const data = await api.get(`${BASE}/detail/${id}`)
  return {
    info: data?.organizationInfoResponse ?? null,
    products: data?.organizationProductResponse ?? [],
  }
}

// 등록/수정은 @ModelAttribute OrganizationInfoRequest라 multipart/form-data로 보낸다.
// fields: 문자열 필드(빈 값은 보내지 않음), files: { organizationImageFile, agreementFile } (File | null)
// 두 API 모두 boolean을 반환한다.
function toFormData(fields, files = {}) {
  const form = new FormData()
  Object.entries(fields).forEach(([key, value]) => {
    if (value === null || value === undefined || value === '') return
    form.append(key, String(value))
  })
  if (files.organizationImageFile) form.append('organizationImageFile', files.organizationImageFile)
  if (files.agreementFile) form.append('agreementFile', files.agreementFile)
  return form
}

export function createOrganization(fields, files) {
  return api.post(BASE, toFormData(fields, files))
}

export function updateOrganization(id, fields, files) {
  return api.put(`${BASE}/detail/${id}`, toFormData(fields, files))
}

// 저장된 파일명 → 이미지 경로 (ImageBox가 백엔드 주소를 붙인다)
export function organizationFilePath(fileName) {
  return fileName ? `${FILE_DIR}${encodeURIComponent(fileName)}` : null
}

// "UUID_원본파일명" → "원본파일명"
export function originalFileName(fileName) {
  if (!fileName) return null
  const i = fileName.indexOf('_')
  return i >= 0 ? fileName.slice(i + 1) : fileName
}

// 협약서 다운로드 - GET /detail/{id}/download (Content-Disposition: attachment)
export function agreementDownloadUrl(id) {
  return `${BASE_URL}${BASE}/detail/${id}/download`
}
