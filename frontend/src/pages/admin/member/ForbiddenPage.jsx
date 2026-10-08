import { Link } from 'react-router-dom'

export default function ForbiddenPage() {
  return (
    <section className="ua-card ua-forbidden" aria-labelledby="forbidden-title">
      <p className="ua-forbidden-code">403 · 접근 권한 없음</p>
      <h1 id="forbidden-title">관리자가 아닙니다</h1>
      <p className="ua-forbidden-description">
        이 페이지는 관리자만 이용할 수 있습니다.<br />
        관리자 계정으로 로그인해 주세요.
      </p>
      <Link className="ua-primary ua-forbidden-link" to="/admin/login">
        관리자 계정으로 로그인
      </Link>
    </section>
  )
}
