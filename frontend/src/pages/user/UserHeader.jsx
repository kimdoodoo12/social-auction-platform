import { Link, useLocation } from 'react-router-dom'
import { useAsync } from '../../hooks/useAsync'
import { authApi } from '../admin/member/authApi'
import { catalogApi } from './catalogApi'
import './catalog.css'

export default function UserHeader() {
  const location = useLocation()
  const { data: member } = useAsync(() => authApi.me(), [location.pathname])
  const { data: categories } = useAsync(catalogApi.categories)
  const params = new URLSearchParams(location.search)
  const active = params.get('categoryId') || ''
  return <header className="shop-header">
    <div className="shop-header-main shop-width">
      <Link className="shop-brand" to="/" aria-label="이음옥션 홈"><span aria-hidden="true">이음</span>이음옥션</Link>
      <form action="/products" className="shop-search" role="search" key={location.search}>
        <label className="shop-sr-only" htmlFor="shop-search">상품명 검색</label>
        <input id="shop-search" name="productName" defaultValue={params.get('productName') || ''} placeholder="어떤 상품을 찾고 계신가요?" maxLength={100} />
        <button type="submit" aria-label="검색">검색</button>
      </form>
      <nav className="shop-member-nav" aria-label="회원 메뉴">
        {member ? <><Link to="/mypage">마이페이지</Link>{member.status === true && <Link to="/admin">관리자</Link>}</> : <><Link to={`/login?returnTo=${encodeURIComponent(location.pathname + location.search)}`}>로그인</Link><Link to="/signup">회원가입</Link></>}
      </nav>
    </div>
    <nav className="shop-category-nav shop-width" aria-label="상품 카테고리">
      <Link to="/products" className={!active ? 'active' : ''}>전체 상품</Link>
      {(categories || []).map((category) => <Link key={category.categoryId} className={active === String(category.categoryId) ? 'active' : ''} to={`/products?categoryId=${category.categoryId}`}>{category.name}</Link>)}
    </nav>
  </header>
}
