import { Outlet } from 'react-router-dom'
import './userAuth.css'
import UserHeader from '../../user/UserHeader'
import UserFooter from '../../user/UserFooter'

// 로그인·회원가입·마이페이지에서 공유하는 사용자용 헤더와 푸터다.
export default function UserAuthLayout() {
  return (
    <div className="user-auth">
      <UserHeader />
      {/* 현재 URL에 해당하는 자식 라우트의 화면을 공통 레이아웃 안에 렌더링한다. */}
      <main className="ua-main"><Outlet /></main>
      {/* 카테고리와 푸터 안내 문구는 표시용이며 별도 페이지로 연결되어 있지 않다. */}
      <UserFooter />
    </div>
  )
}
