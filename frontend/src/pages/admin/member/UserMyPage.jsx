import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { authApi } from './authApi'

// 쿠키로 현재 회원 정보를 조회하고 서버의 쿠키 삭제 API로 로그아웃하는 화면이다.
export default function UserMyPage() {
  const navigate = useNavigate()
  const [member, setMember] = useState(null)
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)
  useEffect(() => {
    // 화면을 떠난 뒤 늦게 도착한 응답이 상태나 이동 경로를 변경하지 못하게 한다.
    let active = true
    authApi.me().then((value) => {
      if (!active) return
      // 유효한 회원이 없으면 로그인 후 이 화면으로 돌아오도록 목적지를 전달한다.
      if (!value) navigate('/login?returnTo=%2Fmypage', { replace: true })
      else setMember(value)
    }).catch(() => { if (active) setError('회원 정보를 불러오지 못했습니다. 다시 로그인해 주세요.') })
    return () => { active = false }
  }, [navigate])

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
