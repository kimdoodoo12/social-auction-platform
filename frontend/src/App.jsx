import { createBrowserRouter, Navigate, RouterProvider } from 'react-router-dom'
import AdminLayout from './components/admin/AdminLayout'
import LoginPage from './pages/admin/LoginPage'
import DashboardPage from './pages/admin/DashboardPage'
import productRoutes from './pages/admin/product/routes'
import categoryRoutes from './pages/admin/category/routes'
import organizationRoutes from './pages/admin/organization/routes'
import memberRoutes from './pages/admin/member/routes'
import auctionRoutes from './pages/admin/auction/routes'
// 일반 회원용 로그인·회원가입·마이페이지 라우트 묶음이다.
import userAuthRoutes from './pages/admin/member/userAuthRoutes'

// 도메인별 라우트는 각 도메인 폴더의 routes.jsx에서 관리한다.
const router = createBrowserRouter([
  // 사용자 인증 화면은 관리자 레이아웃 바깥에서 자체 헤더·푸터를 사용한다.
  userAuthRoutes,
  { path: '/', element: <Navigate to="/admin" replace /> },
  { path: '/admin/login', Component: LoginPage },
  {
    path: '/admin',
    Component: AdminLayout,
    children: [
      { index: true, Component: DashboardPage },
      ...productRoutes,
      ...categoryRoutes,
      ...organizationRoutes,
      ...memberRoutes,
      ...auctionRoutes,
    ],
  },
  { path: '*', element: <Navigate to="/admin" replace /> },
])

export default function App() {
  return <RouterProvider router={router} />
}
