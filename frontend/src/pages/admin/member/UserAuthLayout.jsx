import { Outlet } from 'react-router-dom'
import './userAuth.css'
import UserHeader from '../../user/UserHeader'
import UserFooter from '../../user/UserFooter'

// 로그인·회원가입·마이페이지도 메인·상품 화면과 같은 헤더/푸터를 쓴다.
export default function UserAuthLayout() {
  return (
    <div className="shop">
      <UserHeader />
      {/* 현재 URL에 해당하는 자식 라우트의 화면을 렌더링한다. */}
      <main className="ua-main">
        <Outlet />
      </main>
      <UserFooter />
    </div>
  )
}
