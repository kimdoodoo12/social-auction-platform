import { useNavigate } from 'react-router-dom'
import { Button, Field, MockNotice } from '../../components/admin/ui'
import '../../components/admin/admin.css'
import './pages.css'

// [ADMIN] 01 로그인
// 백엔드에 관리자 로그인 API가 없어 입력값을 검증하지 않고 Dashboard로 이동한다.
export default function LoginPage() {
  const navigate = useNavigate()

  const handleSubmit = (e) => {
    e.preventDefault()
    navigate('/admin')
  }

  return (
    <div className="login-page">
      <form className="login-card" onSubmit={handleSubmit}>
        <div className="login-card__brand">
          <span className="admin-logo-mark" />
          이음옥션 관리자
        </div>
        <p className="login-card__desc">협약 기관 상품 등록과 경매 운영을 위한 내부 시스템입니다.</p>
        <Field label="관리자 이메일">
          <input className="admin-input" type="email" placeholder="admin@ieum-auction.kr" />
        </Field>
        <Field label="비밀번호">
          <input className="admin-input" type="password" placeholder="비밀번호" />
        </Field>
        <Button type="submit" variant="primary">
          로그인
        </Button>
        <p className="login-card__foot">계정 발급 및 비밀번호 초기화는 시스템 관리자에게 문의하세요.</p>
        <MockNotice>관리자 로그인 API가 없어 인증 없이 이동합니다.</MockNotice>
      </form>
    </div>
  )
}
