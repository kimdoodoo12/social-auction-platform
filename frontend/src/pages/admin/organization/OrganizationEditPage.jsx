import { useNavigate, useParams } from 'react-router-dom'
import AdminPage from '../../../components/admin/AdminPage'
import { AsyncBoundary, BackLink } from '../../../components/admin/ui'
import { fetchOrganizationDetail, updateOrganization } from '../../../api/organization'
import { useAsync } from '../../../hooks/useAsync'
import OrganizationForm from './OrganizationForm'

// [ADMIN] 10 기관 수정 (Figma 74:496) — 등록과 같은 폼, 기존 값은 상세 API에서 불러온다.
export default function OrganizationEditPage() {
  const { organizationId } = useParams()
  const navigate = useNavigate()
  const { loading, error, data } = useAsync(() => fetchOrganizationDetail(organizationId), [organizationId])

  const detailPath = `/admin/organizations/${organizationId}`
  const goDetail = () => navigate(detailPath)

  return (
    <AdminPage title="기관 수정">
      <BackLink to={detailPath}>기관 상세로 돌아가기</BackLink>
      <AsyncBoundary loading={loading && !data} error={error}>
        {data?.info && (
          <OrganizationForm
            key={organizationId}
            mode="edit"
            initial={data.info}
            onSubmit={(body) => updateOrganization(organizationId, body)}
            onSuccess={goDetail}
            onCancel={goDetail}
          />
        )}
      </AsyncBoundary>
    </AdminPage>
  )
}
