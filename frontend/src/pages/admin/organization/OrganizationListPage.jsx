import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import AdminPage from '../../../components/admin/AdminPage'
import DataTable from '../../../components/admin/DataTable'
import Pagination from '../../../components/admin/Pagination'
import { Button, Card, FilterBar, LinkButton, Summary } from '../../../components/admin/ui'
import { fetchOrganizations } from '../../../api/organization'
import { useAsync } from '../../../hooks/useAsync'
import { formatDate, formatNumber } from '../../../utils/format'
import { AgreementBadge } from './agreement'

// [ADMIN] 07 기관 관리
const PAGE_SIZE = 10

// select 값('' | 'true' | 'false')을 백엔드 Boolean 파라미터로 변환
const toBool = (value) => (value === '' ? undefined : value === 'true')

export default function OrganizationListPage() {
  const navigate = useNavigate()
  const [draft, setDraft] = useState({ name: '', agreementStatus: '' })
  const [query, setQuery] = useState({ name: '', agreementStatus: '' })
  const [page, setPage] = useState(0)

  const { loading, error, data } = useAsync(
    () => fetchOrganizations({ name: query.name, agreementStatus: toBool(query.agreementStatus), page, size: PAGE_SIZE }),
    [query.name, query.agreementStatus, page],
  )

  const handleSearch = () => {
    setQuery({ name: draft.name.trim(), agreementStatus: draft.agreementStatus })
    setPage(0)
  }

  const goDetail = (row) => navigate(`/admin/organizations/${row.organizationId}`)

  const columns = [
    { key: 'name', header: '기관명', className: 'strong' },
    { key: 'manager', header: '담당자' },
    { key: 'managerPhone', header: '담당자 연락처' },
    { key: 'agreementDate', header: '협약일', render: (r) => formatDate(r.agreementDate) },
    { key: 'agreementStatus', header: '협약 상태', render: (r) => <AgreementBadge status={r.agreementStatus} /> },
    { key: 'productCount', header: '등록 상품', align: 'right', render: (r) => `${formatNumber(r.productCount)}개` },
    {
      key: 'actions',
      header: '관리',
      align: 'right',
      render: (r) => (
        <LinkButton
          tone="primary"
          onClick={(e) => {
            e.stopPropagation()
            goDetail(r)
          }}
        >
          상세
        </LinkButton>
      ),
    },
  ]

  return (
    <AdminPage
      title="기관 관리"
      actions={
        <Button variant="primary" onClick={() => navigate('/admin/organizations/new')}>
          + 기관 등록
        </Button>
      }
    >
      <FilterBar onSearch={handleSearch}>
        <input
          className="admin-input"
          placeholder="기관명을 입력하세요"
          value={draft.name}
          onChange={(e) => setDraft((d) => ({ ...d, name: e.target.value }))}
        />
        <select
          className="admin-select"
          value={draft.agreementStatus}
          onChange={(e) => setDraft((d) => ({ ...d, agreementStatus: e.target.value }))}
        >
          <option value="">협약 상태 전체</option>
          <option value="true">협약</option>
          <option value="false">미협약</option>
        </select>
        <Button variant="dark" type="submit">
          검색
        </Button>
      </FilterBar>

      <Summary label={`전체 ${formatNumber(data?.totalElements ?? 0)}곳`} />

      <Card noBody>
        <DataTable
          rowKey="organizationId"
          rows={data?.content}
          columns={columns}
          loading={loading && !data}
          error={error}
          emptyText="등록된 기관이 없습니다."
          onRowClick={goDetail}
        />
        <Pagination page={data?.page ?? page} totalPages={data?.totalPages ?? 0} onChange={setPage} />
      </Card>
    </AdminPage>
  )
}
