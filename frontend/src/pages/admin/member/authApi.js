import { BASE_URL } from '../../../api/client'

// 인증 전용 요청 함수: 본문이 없으면 GET, 있으면 JSON POST로 호출한다.
// HttpOnly JWT 쿠키는 브라우저가 관리하므로 localStorage 등에 저장하지 않는다.
async function request(path, body) {
 const response = await fetch(`${BASE_URL}/ieum/admin/member${path}`, {
    method: body === undefined ? 'GET' : 'POST',
    credentials: 'include', // 다른 포트의 백엔드에도 인증 쿠키를 전송하고 응답 쿠키를 수신한다.
    headers: body === undefined ? {} : { 'Content-Type': 'application/json' },
    body: body === undefined ? undefined : JSON.stringify(body),
  })
  // 실패 시 빈 본문이 올 수 있으므로 먼저 문자열로 읽고 JSON 변환을 시도한다.
  const text = await response.text()
  let data = null
  try { data = text ? JSON.parse(text) : null } catch { /* JSON이 아닌 서버 오류 본문은 사용자에게 노출하지 않는다. */ }
  if (!response.ok) {
    const error = new Error('요청을 처리하지 못했습니다. 잠시 후 다시 시도해 주세요.')
    error.status = response.status
    throw error
  }
  return data
}

// null이나 단순 성공 값 대신 회원 번호가 있는 DTO가 반환되었는지 검사한다.
export const isMember = (value) => value && typeof value === 'object' && Number(value.memberId) > 0

let refreshing
export function refreshSession() {
  if (!refreshing) {
    refreshing = request('/reissue', {}).then((value) => isMember(value) ? value : null)
      .catch((error) => {
        if (error.status === 401) return null
        throw error
      }).finally(() => { refreshing = null })
  }
  return refreshing
}

// MemberController의 엔드포인트와 UserDto 필드에 맞춘 회원 API 모음이다.
export const authApi = {
  login: (loginId, password) => request('/user/login', { loginId, password }),
  signup: (values) => request('/user/signup', values),
  exists: (loginId) => request(`/user/signup/findid?newid=${encodeURIComponent(loginId)}`),
  logout: () => request('/user/logout', {}),
  async me() {
    try {
      const member = await request('/user/me')
      if (isMember(member)) return member
    } catch (error) {
      if (error.status !== 401) throw error
    }
    return refreshSession()
  },
}

export function safeReturnPath(value) {
  // 외부 주소, 역슬래시, 공백이 있는 값은 기본 목적지로 대체한다.
  if (typeof value !== 'string' || !value.startsWith('/') || value.startsWith('//') || /[\\\s]/.test(value)) return '/mypage'
  const pathname = value.split(/[?#]/)[0]
  // 인증 화면으로 다시 돌아오는 순환 이동을 막는다.
  if (/^\/(?:admin\/login|login|signup|user\/(?:login|signup))(?:\/|$)/.test(pathname)) return '/mypage'
  return value
}
