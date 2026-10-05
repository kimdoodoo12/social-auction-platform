import { Link, Outlet } from 'react-router-dom'
import './userAuth.css'

// 로그인·회원가입·마이페이지에서 공유하는 사용자용 헤더와 푸터다.
export default function UserAuthLayout() {
  return (
    <div className="user-auth">
      <header className="ua-header">
        <div className="ua-header-main">
          <Link className="ua-brand" to="/login"><span aria-hidden="true" />이음옥션</Link>
          {/* 검색 기능은 아직 연결하지 않은 시안 표시 영역이다. */}
          <div className="ua-search" aria-label="상품 검색 준비 중">상품명 또는 제작 기관을 검색해 보세요 <span aria-hidden="true">⌕</span></div>
          <nav aria-label="회원 메뉴"><Link to="/login">로그인</Link><Link to="/signup">회원가입</Link><Link to="/mypage">마이페이지</Link></nav>
        </div>
        <div className="ua-categories" aria-label="상품 카테고리"><strong>전체 카테고리</strong><span>수공예</span><span>식품·가공</span><span>원예</span><span>생활잡화</span><span>의류·패브릭</span></div>
      </header>
      {/* 현재 URL에 해당하는 자식 라우트의 화면을 공통 레이아웃 안에 렌더링한다. */}
      <main className="ua-main"><Outlet /></main>
      {/* 카테고리와 푸터 안내 문구는 표시용이며 별도 페이지로 연결되어 있지 않다. */}
      <footer className="ua-footer">
        <div className="ua-footer-columns">
          <div><strong>이음옥션</strong><span>서비스 소개</span><span>협약 기관 안내</span><span>공지사항</span></div>
          <div><strong>고객지원</strong><span>자주 묻는 질문</span><span>경매 이용 안내</span><span>1:1 문의</span></div>
          <div><strong>정책</strong><span>이용약관</span><span>개인정보처리방침</span><span>입찰 및 낙찰 규정</span></div>
          <div><strong>사회적 가치</strong><p>장애인 및 사회적 약자가 만든 상품을<br />협약 기관과 함께 엄선해 소개합니다.</p></div>
        </div>
        <p className="ua-copyright">© {new Date().getFullYear()} 이음옥션</p>
      </footer>
    </div>
  )
}
