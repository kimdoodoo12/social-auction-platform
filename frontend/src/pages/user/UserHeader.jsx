import { Link, useLocation } from 'react-router-dom'
import { useAsync } from '../../hooks/useAsync'
import { authApi } from '../admin/member/authApi'
import { catalogApi } from './catalogApi'
import BrandLogo from '../../components/BrandLogo'
import './catalog.css'

// Figma 00 Foundation · Header / User (1440×135: 상단 84 + 카테고리 51)
export default function UserHeader() {
  const location = useLocation()
  const { data: member } = useAsync(() => authApi.me(), [location.pathname])
  const { data: categories } = useAsync(catalogApi.categories)
  const params = new URLSearchParams(location.search)
  const active = params.get('categoryId') || ''
  const loginTo = `/login?returnTo=${encodeURIComponent(location.pathname + location.search)}`

  return (
    <header className="shop-header">
      <div className="shop-header-main shop-width">
        <Link className="shop-brand" to="/" aria-label="이음옥션 홈">
          <BrandLogo size={34} />
          이음옥션
        </Link>
        <form action="/products" className="shop-search" role="search" key={location.search}>
          <label className="shop-sr-only" htmlFor="shop-search">상품명 검색</label>
          <input
            id="shop-search"
            name="productName"
            defaultValue={params.get('productName') || ''}
            placeholder="상품명 또는 제작 기관을 검색해 보세요"
            maxLength={100}
          />
          <button type="submit" aria-label="검색">
            <svg width="16" height="16" viewBox="0 0 16 16" aria-hidden="true">
              <circle cx="7" cy="7" r="5.5" fill="none" stroke="currentColor" strokeWidth="1.5" />
              <path d="M11 11l3.5 3.5" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" />
            </svg>
          </button>
        </form>
        <nav className="shop-member-nav" aria-label="회원 메뉴">
          {member ? (
            <>
              <Link to="/mypage" className="strong">마이페이지</Link>
              {member.status === true && <Link to="/admin">관리자</Link>}
            </>
          ) : (
            <>
              <Link to={loginTo} className="strong">로그인</Link>
              <Link to="/signup">회원가입</Link>
              <Link to="/mypage">마이페이지</Link>
            </>
          )}
        </nav>
      </div>
      <nav className="shop-category-nav" aria-label="상품 카테고리">
        <div className="shop-width">
          <Link to="/products" className={!active ? 'active' : ''}>전체 카테고리</Link>
          {(categories || []).map((category) => (
            <Link
              key={category.categoryId}
              className={active === String(category.categoryId) ? 'active' : ''}
              to={`/products?categoryId=${category.categoryId}`}
            >
              {category.name}
            </Link>
          ))}
        </div>
      </nav>
    </header>
  )
}
