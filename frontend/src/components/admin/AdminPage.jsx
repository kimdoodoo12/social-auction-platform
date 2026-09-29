import { useNavigate } from 'react-router-dom'

// 관리자 화면 한 페이지: 상단 Topbar(제목, 뒤로가기, 우측 액션) + 본문
// back: true면 history back, 문자열이면 해당 경로로 이동
export default function AdminPage({ title, back, actions, children }) {
  const navigate = useNavigate()

  const handleBack = () => {
    if (typeof back === 'string') navigate(back)
    else navigate(-1)
  }

  return (
    <>
      <header className="admin-topbar">
        <h1 className="admin-topbar__title">
          {back && (
            <button type="button" className="admin-topbar__back" onClick={handleBack} aria-label="뒤로가기">
              ‹
            </button>
          )}
          {title}
        </h1>
        <div className="admin-topbar__right">
          {actions}
          <div className="admin-topbar__user">
            <span className="admin-topbar__avatar" />
            {/* 관리자 인증 API가 없어 고정 표시 */}
            <span>관리자</span>
          </div>
        </div>
      </header>
      <main className="admin-content">{children}</main>
    </>
  )
}
