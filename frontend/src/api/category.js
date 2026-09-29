import { api, ApiError, BASE_URL } from './client'

// 카테고리 관리 API (backend: category/controller/CategoryController.java)
const BASE = '/ieum/admin/category'

// 응답: List<CategoryResponse> { categoryId, name, productCount }
export async function fetchCategories() {
  const data = await api.get(BASE)
  return Array.isArray(data) ? data : []
}

// 백엔드가 @RequestBody String name 으로 받으므로 JSON이 아닌 원문 텍스트로 보내야 한다.
// (api.post는 JSON.stringify를 하므로 따옴표까지 이름에 저장된다.) 응답: boolean
export async function createCategory(name) {
  const res = await fetch(new URL(BASE, BASE_URL), {
    method: 'POST',
    headers: { 'Content-Type': 'text/plain;charset=UTF-8' },
    body: name,
  })
  const text = await res.text()
  if (!res.ok) throw new ApiError(res.status, `요청 실패 (${res.status})`, text)
  return text === 'true'
}

// 요청 본문: CategoryEntity { categoryId, name }. 응답: boolean
export function updateCategory(categoryId, name) {
  return api.put(BASE, { categoryId, name })
}

// 응답 본문 없음
export function deleteCategory(categoryId) {
  return api.delete(`${BASE}/${categoryId}`)
}
