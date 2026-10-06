import { useRef, useState } from 'react'
import { Link, useLocation, useNavigate, useSearchParams } from 'react-router-dom'
import { authApi, isMember, safeReturnPath } from './authApi'

// 일반 회원 로그인 화면: 서버 쿠키 발급 후 검증한 목적지로 이동한다.
export default function UserLoginPage() {
  const navigate = useNavigate()
  const location = useLocation()
  const [params] = useSearchParams()
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)
  const pending = useRef(false) // 재렌더링 전에 발생하는 연속 제출도 즉시 차단한다.
  // 상품 화면 등에서 전달한 returnTo가 없거나 안전하지 않으면 마이페이지로 이동한다.
  const returnTo = safeReturnPath(params.get('returnTo') || (location.pathname === '/admin/login' ? '/admin' : '/mypage'))

  async function submit(event) {
    // 기본 폼 새로고침을 막고 현재 입력값으로 비동기 요청을 보낸다.
    event.preventDefault()
    if (pending.current) return
    const form = new FormData(event.currentTarget)
    pending.current = true
    setBusy(true)
    setError('')
    try {
      const loginId = form.get('loginId').trim()
      const exists = await authApi.exists(loginId)
      if (exists === false) {
        navigate(`/signup?returnTo=${encodeURIComponent(returnTo)}`, { state: { loginId, accountMissing: true } })
        return
      }
      if (exists !== true) throw new Error('Invalid account lookup response')
      // 아이디의 앞뒤 공백만 제거하고 비밀번호는 입력한 원문을 보낸다.
      const member = await authApi.login(loginId, form.get('password'))
      if (!isMember(member)) {
        setError('비밀번호를 확인해 주세요.')
        return
      }
      navigate(returnTo, { replace: true }) // 뒤로 가기 기록에서 제출한 로그인 화면을 교체한다.
    } catch {
      setError('로그인에 실패했습니다. 서버 연결을 확인하고 다시 시도해 주세요.')
    } finally {
      // 성공·실패와 관계없이 요청 잠금과 버튼의 로딩 상태를 해제한다.
      pending.current = false
      setBusy(false)
    }
  }

  return (
    <section className="ua-card ua-login" aria-labelledby="login-title">
      <h1 id="login-title">로그인</h1>
      {/* 회원가입에서 전달한 라우터 state로 완료 안내와 이메일 기본값을 표시한다. */}
      <p className="ua-notice">입찰에 참여하려면 로그인이 필요합니다. 로그인 후 보시던 상품으로 돌아갑니다.</p>
      {location.state?.signedUp && <p className="ua-success" role="status">회원가입이 완료되었습니다. 로그인해 주세요.</p>}
      {params.get('unavailable') && <p className="ua-error" role="alert">로그인 상태를 확인하지 못했습니다. 서버 연결을 확인한 뒤 다시 로그인해 주세요.</p>}
      <form onSubmit={submit} aria-busy={busy}>
        {/* name은 FormData 키이며 autoComplete는 브라우저의 계정 자동완성을 돕는다. */}
        <label className="ua-field">아이디<input name="loginId" autoComplete="username" placeholder="아이디를 입력해 주세요" defaultValue={location.state?.email || ''} required maxLength={30} /></label>
        <label className="ua-field">비밀번호<input name="password" type="password" autoComplete="current-password" placeholder="비밀번호를 입력해 주세요" required /></label>
        <p className="ua-hint ua-session-note">로그인 상태는 보안 쿠키로 유지됩니다.</p>
        {error && <p className="ua-error" role="alert">{error}</p>}
        <button className="ua-primary" disabled={busy}>{busy ? '로그인 중…' : '로그인'}</button>
      </form>
      {/* 회원가입을 거쳐도 처음 요청한 목적지가 유지되도록 쿼리를 전달한다. */}
      <p className="ua-switch">아직 회원이 아니신가요? <Link to={`/signup?returnTo=${encodeURIComponent(returnTo)}`}>회원가입 →</Link></p>
    </section>
  )
}
