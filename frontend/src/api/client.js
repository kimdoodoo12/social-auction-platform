// 백엔드 API 공통 클라이언트
// 백엔드 컨트롤러에 @CrossOrigin("http://localhost:5173")이 붙어 있어 8080을 직접 호출한다.
export const BASE_URL = 'http://localhost:8080'

export class ApiError extends Error {
  constructor(status, message, body) {
    super(message)
    this.status = status
    this.body = body
  }
}

function buildUrl(path, params) {
  const url = new URL(path, BASE_URL)
  if (params) {
    Object.entries(params).forEach(([key, value]) => {
      // 빈 값은 보내지 않는다 (백엔드 required=false 파라미터는 null로 받는다)
      if (value === undefined || value === null || value === '') return
      url.searchParams.append(key, value)
    })
  }
  return url
}

// 응답 본문을 JSON → 텍스트 순으로 파싱한다. boolean/숫자 응답도 JSON으로 파싱된다.
async function parseBody(res) {
  const text = await res.text()
  if (!text) return null
  try {
    return JSON.parse(text)
  } catch {
    return text
  }
}

export async function request(method, path, { params, body } = {}) {
  const init = { method, headers: {} }
  if (body !== undefined) {
    init.headers['Content-Type'] = 'application/json'
    init.body = JSON.stringify(body)
  }
  const res = await fetch(buildUrl(path, params), init)
  const data = await parseBody(res)
  if (!res.ok) {
    const message = (data && data.message) || `요청 실패 (${res.status})`
    throw new ApiError(res.status, message, data)
  }
  return data
}

export const api = {
  get: (path, params) => request('GET', path, { params }),
  post: (path, body, params) => request('POST', path, { body, params }),
  put: (path, body, params) => request('PUT', path, { body, params }),
  delete: (path, params) => request('DELETE', path, { params }),
}

// Spring Data Page 응답을 화면에서 쓰기 쉬운 형태로 변환한다.
// Spring Boot 3.3+ 의 { content, page: { size, number, totalElements, totalPages } } 형식과
// 이전 형식 { content, number, totalElements, totalPages } 모두 지원한다.
export function toPage(data) {
  if (!data) return { content: [], page: 0, size: 0, totalElements: 0, totalPages: 0 }
  if (Array.isArray(data)) {
    return { content: data, page: 0, size: data.length, totalElements: data.length, totalPages: 1 }
  }
  const meta = data.page && typeof data.page === 'object' ? data.page : data
  return {
    content: data.content ?? [],
    page: meta.number ?? 0,
    size: meta.size ?? 0,
    totalElements: meta.totalElements ?? 0,
    totalPages: meta.totalPages ?? 0,
  }
}
