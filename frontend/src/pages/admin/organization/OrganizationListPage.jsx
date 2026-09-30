import { useState } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { fetchOrganizations } from '../../../api/organization'
import AdminPage from '../../../components/admin/AdminPage'
import DataTable from '../../../components/admin/DataTable'
import Pagination from '../../../components/admin/Pagination'
import { Button, Card, FilterBar, LinkButton, Summary } from '../../../components/admin/ui'
import { useAsync } from '../../../hooks/useAsync'
import { formatDate, formatNumber } from '../../../utils/format'
import { AgreementBadge } from './agreement'
import './organization.css'

// [ADMIN] 07 기관 관리 (Figma 50:75)
// 검색 조건이 없으면 GET /ieum/admin/organization, 있으면 /search.
// Figma의 "담당자로 검색"·지역 필터·상태별 개수는 백엔드가 지원하지 않거나 사용자 결정으로 뺐다.
// 협약해지/재협약 버튼은 API가 없어 비활성으로 둔다.
const PAGE_SIZE = 8

const toStatusParam = (v) => (v === 'true' ? true : v === 'false' ? false : undefined)

export default function OrganizationListPage() {
  const navigate = useNavigate()
  const [searchParams, setSearchParams] = useSearchParams()

  const page = Math.max(0, Number(searchParams.get('page')) || 0)
  const name = searchParams.get('name') ?? ''
  const status = searchParams.get('status') ?? ''
  const searching = Boolean(name || status)

  const [draft, setDraft] = useState({ name, status })

  const list = useAsync(
    () => fetchOrganizations({ name: name || undefined, agreementStatus: toStatusParam(status), page, size: PAGE_SIZE }),
    [name, status, page],
  )

  const pushQuery = (next) => {
    const params = {}
    if (next.name?.trim()) params.name = next.name.trim()
    if (next.status) params.status = next.status
    if (next.page) params.page = String(next.page)
    setSearchParams(params)
  }

  const data = list.data
  const goDetail = (row) => navigate(`/admin/organizations/${row.organizationId}`)

  return (
    <AdminPage title="기관 관리">
      <FilterBar onSearch={() => pushQuery({ ...draft, page: 0 })}>
        <input
          className="admin-input"
          placeholder="기관명으로 검색"
          value={draft.name}
          onChange={(e) => setDraft((d) => ({ ...d, name: e.target.value }))}
        />
        <select
          className="admin-select"
          value={draft.status}
          onChange={(e) => setDraft((d) => ({ ...d, status: e.target.value }))}
        >
          <option value="">전체 협약상태</option>
          <option value="true">협약 중</option>
          <option value="false">협약 종료</option>
        </select>
        <Button type="submit" size="lg" className="admin-search-btn">
          검색
        </Button>
      </FilterBar>

      <div className="admin-list-head">
        <Summary label={data ? `${searching ? '검색 결과' : '전체'} ${formatNumber(data.totalElements)}곳` : '전체'} />
        <Button variant="primary" size="lg" onClick={() => navigate('/admin/organizations/new')}>
          + 기관 등록
        </Button>
      </div>

      <Card noBody>
        <DataTable
          rowKey="organizationId"
          rows={data?.content}
          loading={list.loading && !data}
          error={list.error}
          emptyText={searching ? '검색 결과가 없습니다.' : '등록된 기관이 없습니다.'}
          onRowClick={goDetail}
          columns={[
            { key: 'organizationId', header: '기관번호', width: 76 },
            { key: 'name', header: '기관명', className: 'strong' },
            { key: 'manager', header: '담당자' },
            { key: 'managerPhone', header: '연락처' },
            { key: 'agreementDate', header: '협약일', align: 'right', render: (r) => formatDate(r.agreementDate) },
            { key: 'agreementStatus', header: '협약상태', render: (r) => <AgreementBadge status={r.agreementStatus} /> },
            { key: 'productCount', header: '등록 상품', align: 'right', render: (r) => `${formatNumber(r.productCount ?? 0)}개` },
            {
              key: 'actions',
              header: '관리',
              render: (r) => (
                <span className="admin-actions" onClick={(e) => e.stopPropagation()}>
                  <LinkButton onClick={() => goDetail(r)}>상세</LinkButton>
                  <span className="admin-actions__sep">·</span>
                  <LinkButton onClick={() => navigate(`/admin/organizations/${r.organizationId}/edit`)}>수정</LinkButton>
                  <span className="admin-actions__sep">·</span>
                  <LinkButton tone="muted" pending>
                    {r.agreementStatus === false ? '재협약' : '협약해지'}
                  </LinkButton>
                </span>
              ),
            },
          ]}
        />
        {data && (
          <Pagination
            page={data.page}
            totalPages={data.totalPages}
            onChange={(p) => pushQuery({ name, status, page: p })}
          />
        )}
      </Card>
    </AdminPage>
  )
}
