import { BASE_URL } from '../../../api/client'

// 인증 전용 요청 함수: 본문이 없으면 GET, 있으면 JSON POST로 호출한다.
// HttpOnly JWT 쿠키는 브라우저가 관리하므로 localStorage 등에 저장하지 않는다.
async function request(path, body) {
  const response = await fetch(`${BASE_URL}${path}`, {
    method: body === undefined ? 'GET' : 'POST',
    credentials: 'include', // 다른 포트의 백엔드에도 인증 쿠키를 전송하고 응답 쿠키를 수신한다.
    headers: body === undefined ? {} : { 'Content-Type': 'application/json' },
    body: body === undefined ? undefined : JSON.stringify(body),
  })
  // 실패 시 빈 본문이 올 수 있으므로 먼저 문자열로 읽고 JSON 변환을 시도한다.
  const text = await response.text()
  let data = null
  try { data = text ? JSON.parse(text) : null } catch { /* JSON이 아닌 서버 오류 본문은 사용자에게 노출하지 않는다. */ }
  if (!response.ok) throw new Error('요청을 처리하지 못했습니다. 잠시 후 다시 시도해 주세요.')
  return data
}

// null이나 단순 성공 값 대신 회원 번호가 있는 DTO가 반환되었는지 검사한다.
export const isMember = (value) => value && typeof value === 'object' && Number(value.memberId) > 0

// MemberController의 엔드포인트와 UserDto 필드에 맞춘 회원 API 모음이다.
export const authApi = {
  login: (loginId, password) => request('/user/login', { loginId, password }),
  signup: (values) => request('/user/signup', values),
  logout: () => request('/user/logout', {}),
  async me() {
    // 정상 응답에 회원 정보가 없으면 refreshToken 쿠키로 재발급을 한 번 시도한다.
    // HTTP 오류는 request의 예외로 전달되며 여기서 재시도하지 않는다.
    const member = await request('/user/me')
    if (isMember(member)) return member
    const renewed = await request('/reissue', {})
    return isMember(renewed) ? renewed : null
  },
}

export function safeReturnPath(value) {
  // 외부 주소, 역슬래시, 공백이 있는 값은 기본 목적지로 대체한다.
  if (typeof value !== 'string' || !value.startsWith('/') || value.startsWith('//') || /[\\\s]/.test(value)) return '/mypage'
  const pathname = value.split(/[?#]/)[0]
  // 인증 화면으로 순환 이동하거나 관리자 경로로 이동하지 않도록 제한한다.
  if (/^\/(?:admin|login|signup|user\/(?:login|signup))(?:\/|$)/.test(pathname)) return '/mypage'
  return value
}
