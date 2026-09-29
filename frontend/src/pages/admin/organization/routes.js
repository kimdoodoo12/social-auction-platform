import OrganizationListPage from './OrganizationListPage'
import OrganizationCreatePage from './OrganizationCreatePage'
import OrganizationDetailPage from './OrganizationDetailPage'
import OrganizationEditPage from './OrganizationEditPage'

// /admin 아래 상대 경로. 이 도메인 담당자만 수정한다.
// 화면 컴포넌트는 별도 파일로 만들고 Component에 연결한다(이 파일에는 JSX를 쓰지 않는다).
export default [
  { path: 'organizations', Component: OrganizationListPage, handle: { title: '기관 관리' } },
  { path: 'organizations/new', Component: OrganizationCreatePage, handle: { title: '기관 등록' } },
  { path: 'organizations/:organizationId', Component: OrganizationDetailPage, handle: { title: '기관 상세' } },
  { path: 'organizations/:organizationId/edit', Component: OrganizationEditPage, handle: { title: '기관 수정' } },
]
