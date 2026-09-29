import { useNavigate, useParams } from 'react-router-dom'
import AdminPage from '../../../components/admin/AdminPage'
import { AsyncBoundary } from '../../../components/admin/ui'
import { fetchOrganizationDetail, updateOrganization } from '../../../api/organization'
import { useAsync } from '../../../hooks/useAsync'
import OrganizationForm from './OrganizationForm'

// [ADMIN] 10 기관 수정
// 기존 값은 상세 API(organizationInfoResponse)에서 불러온다.
export default function OrganizationEditPage() {
  const { organizationId } = useParams()
  const navigate = useNavigate()
  const { loading, error, data } = useAsync(() => fetchOrganizationDetail(organizationId), [organizationId])

  const detailPath = `/admin/organizations/${organizationId}`
  const goDetail = () => navigate(detailPath)

  return (
    <AdminPage title="기관 수정" back={detailPath}>
      <AsyncBoundary loading={loading} error={error}>
        {data?.info && (
          <OrganizationForm
            key={organizationId}
            initial={data.info}
            submitLabel="수정 완료"
            onSubmit={(body) => updateOrganization(organizationId, body)}
            onSuccess={goDetail}
            onCancel={goDetail}
          />
        )}
      </AsyncBoundary>
    </AdminPage>
  )
}
