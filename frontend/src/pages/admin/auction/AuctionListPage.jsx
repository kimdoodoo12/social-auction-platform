import { useState } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import AdminPage from '../../../components/admin/AdminPage'
import DataTable from '../../../components/admin/DataTable'
import Pagination from '../../../components/admin/Pagination'
import { Button, Card, FilterBar, LinkButton, Summary } from '../../../components/admin/ui'
import { fetchAuctions, searchAuctions } from '../../../api/auction'
import { fetchOrganizationOptions } from '../../../api/product'
import { useAsync } from '../../../hooks/useAsync'
import { formatNumber, formatPrice, formatShortDateTime } from '../../../utils/format'
import './auction.css'

// [ADMIN] 13 경매 관리 (Figma 51:134)
// 조건이 없으면 GET /auction, 있으면 GET /auction/search.
// 목록 응답에 경매 상태가 없어 상태 칸은 '-'로 둔다. 상태별 개수는 표시하지 않는다(사용자 결정).
const PAGE_SIZE = 8
const FILTER_KEYS = ['keyword', 'status', 'organization', 'startDate', 'endDate']
const STATUS_OPTIONS = [
  { value: '대기', label: '경매 대기' },
  { value: '진행', label: '경매 진행 중' },
  { value: '완료', label: '경매 종료' },
]

function readFilters(params) {
  return Object.fromEntries(FILTER_KEYS.map((key) => [key, params.get(key) ?? '']))
}

export default function AuctionListPage() {
  const navigate = useNavigate()
  const [params, setParams] = useSearchParams()
  const filters = readFilters(params)
  const page = Number(params.get('page')) || 0
  const hasFilter = FILTER_KEYS.some((key) => filters[key])

  const [draft, setDraft] = useState(filters)

  const list = useAsync(
    () =>
      hasFilter
        ? searchAuctions({
            keyword: filters.keyword.trim() || undefined,
            status: filters.status || undefined,
            organization: filters.organization || undefined,
            startDate: filters.startDate || undefined,
            endDate: filters.endDate || undefined,
            page,
            size: PAGE_SIZE,
          })
        : fetchAuctions({ page, size: PAGE_SIZE }),
    [filters.keyword, filters.status, filters.organization, filters.startDate, filters.endDate, page],
  )
  const organizations = useAsync(fetchOrganizationOptions, [])

  const changeDraft = (key) => (e) => setDraft((prev) => ({ ...prev, [key]: e.target.value }))

  const applyFilters = (next, nextPage = 0) => {
    const search = {}
    FILTER_KEYS.forEach((key) => {
      const v = String(next[key] ?? '').trim()
      if (v) search[key] = v
    })
    if (nextPage > 0) search.page = String(nextPage)
    setParams(search)
  }

  const pageData = list.data
  const goDetail = (row) => navigate(`/admin/auctions/${row.auctionId}`)

  const columns = [
    { key: 'auctionId', header: '경매번호', width: 76 },
    { key: 'productName', header: '상품명', className: 'strong' },
    { key: 'organizationName', header: '제작기관' },
    { key: 'startPrice', header: '시작가', align: 'right', render: (r) => formatPrice(r.startPrice) },
    {
      key: 'topPrice',
      header: '현재 최고가',
      align: 'right',
      render: (r) => (r.bidCount ? formatPrice(r.topPrice) : '—'),
    },
    { key: 'bidCount', header: '입찰', align: 'right', render: (r) => formatNumber(r.bidCount ?? 0) },
    { key: 'startTime', header: '시작시간', align: 'right', render: (r) => formatShortDateTime(r.startTime) },
    { key: 'endTime', header: '종료시간', align: 'right', render: (r) => formatShortDateTime(r.endTime) },
    { key: 'status', header: '상태', render: (r) => r.status ?? '-' },
    {
      key: 'actions',
      header: '관리',
      render: (r) => (
        <span onClick={(e) => e.stopPropagation()}>
          <LinkButton onClick={() => goDetail(r)}>상세</LinkButton>
        </span>
      ),
    },
  ]

  return (
    <AdminPage title="경매 관리">
      <FilterBar onSearch={() => applyFilters(draft)}>
        <input
          className="admin-input"
          placeholder="경매번호 또는 상품명으로 검색"
          value={draft.keyword}
          onChange={changeDraft('keyword')}
        />
        <select className="admin-select" value={draft.status} onChange={changeDraft('status')} aria-label="상태">
          <option value="">전체 상태</option>
          {STATUS_OPTIONS.map((s) => (
            <option key={s.value} value={s.value}>
              {s.label}
            </option>
          ))}
        </select>
        <select className="admin-select" value={draft.organization} onChange={changeDraft('organization')} aria-label="기관">
          <option value="">전체 기관</option>
          {(organizations.data ?? []).map((o) => (
            <option key={o.id} value={o.name}>
              {o.name}
            </option>
          ))}
        </select>
        <div className="auction-filter-date">
          <input type="date" className="admin-input" value={draft.startDate} onChange={changeDraft('startDate')} aria-label="기간 시작" />
          ~
          <input type="date" className="admin-input" value={draft.endDate} onChange={changeDraft('endDate')} aria-label="기간 종료" />
        </div>
        <Button type="submit" size="lg" className="admin-search-btn">
          검색
        </Button>
      </FilterBar>

      <Summary label={`${hasFilter ? '검색 결과' : '전체'} ${formatNumber(pageData?.totalElements ?? 0)}건`}>
        <span className="auction-summary-note">경매 시작 시각은 첫 입찰 시점에 자동 기록됩니다.</span>
      </Summary>

      <Card noBody>
        <DataTable
          rowKey="auctionId"
          columns={columns}
          rows={pageData?.content}
          loading={list.loading && !pageData}
          error={list.error}
          emptyText={hasFilter ? '검색 조건에 맞는 경매가 없습니다.' : '등록된 경매가 없습니다.'}
          onRowClick={goDetail}
        />
        {pageData && <Pagination page={pageData.page} totalPages={pageData.totalPages} onChange={(p) => applyFilters(filters, p)} />}
      </Card>
    </AdminPage>
  )
}
