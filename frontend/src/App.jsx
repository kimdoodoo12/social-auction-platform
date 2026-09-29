import { createBrowserRouter, Navigate, RouterProvider } from 'react-router-dom'
import AdminLayout from './components/admin/AdminLayout'
import LoginPage from './pages/admin/LoginPage'
import DashboardPage from './pages/admin/DashboardPage'
import productRoutes from './pages/admin/product/routes'
import categoryRoutes from './pages/admin/category/routes'
import organizationRoutes from './pages/admin/organization/routes'
import memberRoutes from './pages/admin/member/routes'
import auctionRoutes from './pages/admin/auction/routes'

// 도메인별 라우트는 각 도메인 폴더의 routes.jsx에서 관리한다.
const router = createBrowserRouter([
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
