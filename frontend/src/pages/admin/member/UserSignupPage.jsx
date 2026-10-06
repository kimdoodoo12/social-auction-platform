import { useRef, useState } from 'react'
import { Link, useLocation, useNavigate, useSearchParams } from 'react-router-dom'
import { authApi, safeReturnPath } from './authApi'

// 정식 약관 및 동의 이력 저장 API가 제공되면 서버 기반 약관으로 교체한다.
const agreements = ['이용약관', '개인정보 수집·이용', '입찰 및 낙찰 규정']

// 회원가입 화면: 입력 검증 후 UserDto를 전송하고 로그인 화면으로 안내한다.
export default function UserSignupPage() {
  const navigate = useNavigate()
  const location = useLocation()
  const [params] = useSearchParams()
  const [checks, setChecks] = useState([false, false, false]) // agreements와 같은 순서의 필수 확인 상태다.
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)
  const pending = useRef(false) // 연속 클릭으로 가입 요청이 중복 전송되는 것을 막는다.
  const returnTo = safeReturnPath(params.get('returnTo'))

  async function submit(event) {
    // 기본 제출을 막고 폼 값을 읽는다. 비밀번호에는 trim을 적용하지 않는다.
    event.preventDefault()
    if (pending.current) return
    const form = new FormData(event.currentTarget)
    const email = form.get('email').trim()
    const loginId = form.get('loginId').trim()
    const password = form.get('password')
    const name = form.get('name').trim()
    const phone = form.get('phone').replace(/[^0-9]/g, '') // 하이픈 등 표시 문자를 제거한다.
    // 이름, 비밀번호 조합·일치 여부, 휴대전화 형식, 필수 확인을 순서대로 검사한다.
    // BCrypt 입력 제한을 고려해 문자 수와 별도로 UTF-8 기준 72바이트도 확인한다.
    if (!name) return setError('이름을 입력해 주세요.')
    if (!loginId) return setError('아이디를 입력해 주세요.')
    if (!/^(?=.*[A-Za-z])(?=.*\d).{8,72}$/.test(password) || new TextEncoder().encode(password).length > 72) return setError('비밀번호는 영문과 숫자를 포함한 8자 이상, 72바이트 이하로 입력해 주세요.')
    if (password !== form.get('confirmation')) return setError('비밀번호가 일치하지 않습니다.')
    if (!/^01[016789]\d{7,8}$/.test(phone)) return setError('휴대전화 번호를 확인해 주세요.')
    if (!checks.every(Boolean)) return setError('필수 항목을 모두 확인해 주세요.')
    pending.current = true
    setBusy(true)
    setError('')
    try {
      // 중복 여부를 확인하고 가입 정보를 전송한다.
      const exists = await authApi.exists(loginId)
      if (exists === true) return setError('이미 등록된 아이디입니다. 다른 아이디를 입력하거나 로그인해 주세요.')
      if (exists !== false) throw new Error('Invalid account lookup response')
      const result = await authApi.signup({ loginId, email, password, name, phone })
      if (result !== true) return setError('가입하지 못했습니다. 입력 정보를 확인해 주세요.')
      // 가입 완료 안내와 이메일을 라우터 state로 넘기고 원래 목적지는 쿼리로 유지한다.
      navigate(`/login?returnTo=${encodeURIComponent(returnTo)}`, { replace: true, state: { signedUp: true, email: loginId } })
    } catch {
      setError('가입하지 못했습니다. 중복된 계정인지 또는 서버 연결 상태를 확인해 주세요.')
    } finally {
      // 실패한 경우에도 다시 제출할 수 있도록 요청 상태를 초기화한다.
      pending.current = false
      setBusy(false)
    }
  }

  return (
    <section className="ua-card ua-signup" aria-labelledby="signup-title">
      <h1 id="signup-title">회원가입</h1>
      {location.state?.accountMissing && <p className="ua-notice" role="status">등록되지 않은 아이디입니다. 회원가입을 완료한 뒤 로그인해 주세요.</p>}
      <p className="ua-subtitle">가입 후 바로 입찰에 참여할 수 있습니다.</p>
      <form onSubmit={submit} aria-busy={busy}>
        {/* required, type, minLength로 브라우저 기본 검증을 적용하고 submit에서 추가 검증한다. */}
        <label className="ua-field">아이디<input name="loginId" autoComplete="username" defaultValue={location.state?.loginId || ''} required maxLength={30} /></label>
        <label className="ua-field">이름<input name="name" autoComplete="name" placeholder="실명을 입력해 주세요" required maxLength={30} /></label>
        <label className="ua-field">이메일<input name="email" type="email" autoComplete="email" placeholder="example@email.com" required maxLength={255} /><span className="ua-hint">낙찰 안내 주소로 사용합니다.</span></label>
        <label className="ua-field">비밀번호<input name="password" type="password" autoComplete="new-password" placeholder="영문·숫자 조합 8자 이상" required minLength={8} maxLength={72} /></label>
        <label className="ua-field">비밀번호 확인<input name="confirmation" type="password" autoComplete="new-password" placeholder="비밀번호를 한 번 더 입력해 주세요" required /></label>
        <label className="ua-field">연락처<input name="phone" type="tel" autoComplete="tel" placeholder="010-0000-0000" required maxLength={13} /><span className="ua-hint">낙찰 시 제작 기관이 배송을 위해 사용합니다.</span></label>
        <fieldset className="ua-agreements">
          {/* 전체 확인은 모든 항목을 함께 변경하고, 개별 항목은 해당 인덱스만 변경한다. */}
          <legend className="ua-sr-only">약관 확인</legend>
          <label className="ua-all"><input type="checkbox" checked={checks.every(Boolean)} onChange={(e) => setChecks(checks.map(() => e.target.checked))} />전체 확인</label>
          {agreements.map((label, index) => <label key={label}><input type="checkbox" checked={checks[index]} onChange={(e) => setChecks(checks.map((value, i) => i === index ? e.target.checked : value))} required /><span><em>필수</em> {label} 확인</span></label>)}
        </fieldset>
        <p className="ua-notice ua-terms-note">입찰은 취소할 수 없으며, 낙찰 시 결제 의무가 발생합니다.</p>
        {/* 현재 체크 상태는 화면에서만 사용하며 서버에 동의 이력이 저장되지는 않는다. */}
        <p className="ua-hint ua-session-note">현재 약관 전문과 동의 이력 저장은 준비 중입니다.</p>
        {error && <p className="ua-error" role="alert">{error}</p>}
        <button className="ua-primary" disabled={busy}>{busy ? '가입 중…' : '가입하기'}</button>
      </form>
      <p className="ua-switch">이미 계정이 있으신가요? <Link to={`/login?returnTo=${encodeURIComponent(returnTo)}`}>로그인 →</Link></p>
    </section>
  )
}
