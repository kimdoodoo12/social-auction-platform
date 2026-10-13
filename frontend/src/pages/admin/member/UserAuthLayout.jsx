import { useState } from 'react'
import { Link, Outlet, useLoaderData, useNavigate } from 'react-router-dom'
import { authApi } from './authApi'
import './userAuth.css'
import UserFooter from '../../user/UserFooter'

export default function UserAuthLayout() {
  const { member, authError } = useLoaderData()
  const navigate = useNavigate()
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')

  async function handleLogout() {
    if (busy) return

    setBusy(true)
    setError('')

    try {
      if ((await authApi.logout()) !== true) {
        throw new Error('Logout failed')
      }

      navigate('/login', { replace: true })
    } catch {
      setError('로그아웃하지 못했습니다. 다시 시도해 주세요.')
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="user-auth">
      <header className="ua-header">
        <div className="ua-header-main">
          <Link className="ua-brand" to="/login">
            <span aria-hidden="true" />
            이음옥션
          </Link>

          {/* 검색 기능은 아직 연결하지 않은 시안 표시 영역이다. */}
          <div className="ua-search" aria-label="상품 검색 준비 중">
            상품명 또는 제작 기관을 검색해 보세요
            <span aria-hidden="true">⌕</span>
          </div>

          <nav aria-label="회원 메뉴">
            {authError ? (
              <span role="status">로그인 상태 확인 실패</span>
            ) : member ? (
              <button
                type="button"
                className="ua-header-logout"
                onClick={handleLogout}
                disabled={busy}
              >
                {busy ? '로그아웃 중…' : '로그아웃'}
              </button>
            ) : (
              <>
                <Link to="/login">로그인</Link>
                <Link to="/signup">회원가입</Link>
              </>
            )}

            <Link to="/mypage">마이페이지</Link>
          </nav>
        </div>

        {/* 카테고리는 표시용이며 별도 페이지로 연결되어 있지 않다. */}
        <div className="ua-categories" aria-label="상품 카테고리">
          <strong>전체 카테고리</strong>
          <span>수공예</span>
          <span>식품·가공</span>
          <span>원예</span>
          <span>생활잡화</span>
          <span>의류·패브릭</span>
        </div>
      </header>

      {/* 현재 URL에 해당하는 자식 라우트의 화면을 렌더링한다. */}
      <main className="ua-main">
        {error && (
          <p className="ua-error" role="alert">
            {error}
          </p>
        )}
        <Outlet />
      </main>

      <UserFooter />
    </div>
  )
}