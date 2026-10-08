import { redirect } from 'react-router-dom'
import { authApi } from './authApi'
import { FORBIDDEN_PATH } from './forbidden'

// 로그인 여부 확인
export async function requireAuth({ request }) {
  const url = new URL(request.url)
  const returnTo = encodeURIComponent(url.pathname + url.search)

  let member

  try {
    member = await authApi.me()
  } catch (error) {
    if (error.status === 403) {
      throw redirect(FORBIDDEN_PATH)
    }

    throw redirect(`/login?returnTo=${returnTo}&unavailable=1`)
  }

  if (!member) {
    throw redirect(`/login?returnTo=${returnTo}`)
  }

  return member
}

// localhost:5173 최초 접속 시 이동할 화면 결정
export async function redirectHome() {
  let member

  try {
    member = await authApi.me()
  } catch (error) {
    if (error.status === 403) {
      throw redirect(FORBIDDEN_PATH)
    }

    throw redirect('/login?unavailable=1')
  }

  if (!member) {
    throw redirect('/login')
  }

  throw redirect(member.status === true ? '/admin' : '/mypage')
}

// 관리자 페이지 진입 검사
export async function requireAdmin(args) {
  const member = await requireAuth(args)

  if (member.status !== true) {
    throw redirect(FORBIDDEN_PATH)
  }

  return member
}