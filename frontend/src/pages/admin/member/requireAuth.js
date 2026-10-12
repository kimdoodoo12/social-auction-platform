import { redirect } from 'react-router-dom'
import { authApi } from './authApi'
import { FORBIDDEN_PATH } from './forbidden'

// 로그인·인증 확인 + 관리자 경로 접근 시 관리자 여부 확인
export async function requireAuth({ request }) {
  const url = new URL(request.url)

  // 로그인 후 돌아올 경로를 쿼리 문자열까지 보관한다.
  const returnTo = encodeURIComponent(url.pathname + url.search)

  let member

  try {
    // 서버에서 현재 로그인한 회원 정보를 확인한다.
    member = await authApi.me()
  } catch (error) {
    // 서버에서 접근 권한이 없다고 응답하면 접근 제한 화면으로 이동한다.
    if (error.status === 403) {
      throw redirect(FORBIDDEN_PATH)
    }

    // 인증 확인 요청에 실패하면 로그인 화면으로 이동한다.
    throw redirect(`/login?returnTo=${returnTo}&unavailable=1`)
  }

  // 로그인하지 않은 사용자는 로그인 화면으로 이동한다.
  if (!member) {
    throw redirect(`/login?returnTo=${returnTo}`)
  }

  // /admin과 그 하위 경로에 접근하는지 확인한다.
  const isAdminPath =
    url.pathname === '/admin' || url.pathname.startsWith('/admin/')

  // 인증된 사용자라도 관리자가 아니면 관리자 화면 진입을 막는다.
  // /mypage 같은 일반 회원 경로에는 이 검사를 적용하지 않는다.
  if (isAdminPath && member.status !== true) {
    throw redirect(FORBIDDEN_PATH)
  }

  return member
}

// 루트 경로(/) 접속 시 로그인 상태와 관리자 여부에 따라 이동한다.
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

  // 로그인하지 않았다면 로그인 화면으로 이동한다.
  if (!member) {
    throw redirect('/login')
  }

  // 관리자는 관리자 화면, 일반 회원은 마이페이지로 이동한다.
  throw redirect(member.status === true ? '/admin' : '/mypage')
}

// 경로 이름과 관계없이 관리자 전용 라우트에 연결할 수 있는 검사 함수
export async function requireAdmin(args) {
  // 먼저 기존 로그인·인증 검사를 수행한다.
  const member = await requireAuth(args)

  // 인증된 회원의 관리자 여부를 확인한다.
  if (member.status !== true) {
    throw redirect(FORBIDDEN_PATH)
  }

  return member
}