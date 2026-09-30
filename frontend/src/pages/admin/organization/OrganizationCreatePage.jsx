import { useNavigate } from 'react-router-dom'
import AdminPage from '../../../components/admin/AdminPage'
import { BackLink } from '../../../components/admin/ui'
import { createOrganization } from '../../../api/organization'
import OrganizationForm from './OrganizationForm'

// [ADMIN] 09 기관 등록 (Figma 74:325)
// 등록 API는 boolean만 반환해 새 기관 id를 알 수 없으므로 성공 시 목록으로 이동한다.
export default function OrganizationCreatePage() {
  const navigate = useNavigate()
  const goList = () => navigate('/admin/organizations')

  return (
    <AdminPage title="기관 등록">
      <BackLink to="/admin/organizations">기관 관리로 돌아가기</BackLink>
      <OrganizationForm onSubmit={createOrganization} onSuccess={goList} onCancel={goList} />
    </AdminPage>
  )
}
