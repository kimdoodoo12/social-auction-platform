import { useNavigate } from 'react-router-dom'
import AdminPage from '../../../components/admin/AdminPage'
import { createOrganization } from '../../../api/organization'
import OrganizationForm from './OrganizationForm'

// [ADMIN] 09 기관 등록
// 등록 API는 boolean만 반환해 새 기관 id를 알 수 없으므로 성공 시 목록으로 이동한다.
export default function OrganizationCreatePage() {
  const navigate = useNavigate()
  const goList = () => navigate('/admin/organizations')

  return (
    <AdminPage title="기관 등록" back="/admin/organizations">
      <OrganizationForm submitLabel="등록" onSubmit={createOrganization} onSuccess={goList} onCancel={goList} />
    </AdminPage>
  )
}
