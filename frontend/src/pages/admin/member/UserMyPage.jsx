import { useState } from 'react'
import { Link, useLoaderData, useNavigate } from 'react-router-dom'
import { authApi } from './authApi'

// 쿠키로 현재 회원 정보를 조회하고 서버의 쿠키 삭제 API로 로그아웃하는 화면이다.
export default function UserMyPage() {
  const navigate = useNavigate()
  const member = useLoaderData()
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  async function logout() {
    // HttpOnly 쿠키는 프론트에서 삭제할 수 없으므로 서버의 성공 응답을 확인한다.
    setBusy(true)
    setError('')
    try {
      if (await authApi.logout() !== true) throw new Error('logout failed')
      navigate('/login', { replace: true })
    } catch { setError('로그아웃하지 못했습니다. 다시 시도해 주세요.') }
    finally { setBusy(false) }
  }
  // 조회 중에는 로딩 안내, 성공하면 회원 정보, 실패하면 재로그인 안내를 보여 준다.
  return <section className="ua-card ua-profile"><h1>마이페이지</h1>
    {member ? <><p className="ua-subtitle">{member.name}님, 반갑습니다.</p><dl><dt>이메일</dt><dd>{member.email}</dd><dt>연락처</dt><dd>{member.phone}</dd></dl><button className="ua-primary" onClick={logout} disabled={busy}>{busy ? '로그아웃 중…' : '로그아웃'}</button></> : !error && <p role="status">회원 정보를 확인하고 있습니다…</p>}
    {error && <p className="ua-error" role="alert">{error} <Link to="/login">로그인으로 이동</Link></p>}
  </section>
}
