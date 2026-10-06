import { redirect } from 'react-router-dom'
import { authApi } from './authApi'

// 라우터가 보호된 화면을 렌더링하기 전에 서버 쿠키를 검증한다.
export async function requireAuth({ request }) {
  try {
    const member = await authApi.me()
    if (member) return member
  } catch {
    // 서버 연결이 실패한 경우에도 보호된 화면은 열지 않는다.
    const url = new URL(request.url)
    throw redirect(`/login?returnTo=${encodeURIComponent(url.pathname + url.search)}&unavailable=1`)
  }
  const url = new URL(request.url)
  throw redirect(`/login?returnTo=${encodeURIComponent(url.pathname + url.search)}`)
}
