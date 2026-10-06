import { useState } from 'react'
import { authApi } from '../../pages/admin/member/authApi'
import { NavLink, useNavigate } from 'react-router-dom'

const MENUS = [
  { to: '/admin', label: 'Dashboard', end: true },
  { to: '/admin/products', label: '상품 관리' },
  { to: '/admin/categories', label: '카테고리 관리' },
  { to: '/admin/organizations', label: '기관 관리' },
  { to: '/admin/members', label: '회원 관리' },
  { to: '/admin/auctions', label: '경매 관리' },
]

export default function Sidebar() {
  const navigate = useNavigate()
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  async function logout() {
    if (busy) return
    setBusy(true)
    setError('')
    try {
      if (await authApi.logout() !== true) throw new Error('Logout failed')
      navigate('/admin/login', { replace: true })
    } catch {
      setError('로그아웃하지 못했습니다. 다시 시도해 주세요.')
    } finally { setBusy(false) }
  }

  return (
    <aside className="admin-sidebar">
      <div className="admin-sidebar__brand">
        <span className="admin-logo-mark" />
        이음옥션
        <span className="admin-sidebar__badge">ADMIN</span>
      </div>
      <nav className="admin-sidebar__nav">
        {MENUS.map((menu) => (
          <NavLink
            key={menu.to}
            to={menu.to}
            end={menu.end}
            className={({ isActive }) => `admin-sidebar__link${isActive ? ' active' : ''}`}
          >
            {menu.label}
          </NavLink>
        ))}
      </nav>
      <div className="admin-sidebar__footer">
        {error && <p role="alert">{error}</p>}
        <button type="button" className="admin-sidebar__logout" onClick={logout} disabled={busy}>
          로그아웃
        </button>
      </div>
    </aside>
  )
}
