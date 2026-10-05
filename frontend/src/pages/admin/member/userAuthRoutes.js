import UserAuthLayout from './UserAuthLayout'
import UserLoginPage from './UserLoginPage'
import UserSignupPage from './UserSignupPage'
import UserMyPage from './UserMyPage'

// App.jsx에서 최상위 라우트로 등록한다. 파일 위치와 달리 /admin 하위 경로가 아니다.
// 경로 없는 부모가 공통 레이아웃을 제공하고 자식 화면은 Outlet에 표시된다.
export default {
  Component: UserAuthLayout,
  children: [
    { path: '/login', Component: UserLoginPage },
    { path: '/signup', Component: UserSignupPage },
    // /user 접두어로 접속해도 같은 로그인·회원가입 화면을 제공한다.
    { path: '/user/login', Component: UserLoginPage },
    { path: '/user/signup', Component: UserSignupPage },
    // 인증 여부는 마이페이지 컴포넌트에서 서버에 조회한다.
    { path: '/mypage', Component: UserMyPage },
  ],
}
