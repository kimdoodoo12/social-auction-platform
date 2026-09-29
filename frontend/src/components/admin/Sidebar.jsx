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
        {/* 백엔드에 관리자 로그인/로그아웃 API가 없어 로그인 화면으로 이동만 한다 */}
        <button type="button" className="admin-sidebar__logout" onClick={() => navigate('/admin/login')}>
          로그아웃
        </button>
      </div>
    </aside>
  )
}
