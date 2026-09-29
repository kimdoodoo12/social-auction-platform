import { useState } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import AdminPage from '../../../components/admin/AdminPage'
import DataTable from '../../../components/admin/DataTable'
import Pagination from '../../../components/admin/Pagination'
import { Badge, Card, LinkButton, Summary } from '../../../components/admin/ui'
import { fetchAuctions } from '../../../api/auction'
import { useAsync } from '../../../hooks/useAsync'
import { formatDateTime, formatNumber, formatPrice } from '../../../utils/format'
import { getAuctionStatus } from './auctionStatus'
import './auction.css'

// [ADMIN] 13 경매 관리
// 백엔드 경매 목록 API(GET /auction)는 검색/상태 필터 파라미터가 없어 페이지 조회만 한다.
const PAGE_SIZE = 10

export default function AuctionListPage() {
  const navigate = useNavigate()
  const [params, setParams] = useSearchParams()
  const page = Number(params.get('page')) || 0
  // 상태 계산 기준 시각 (렌더마다 바뀌지 않도록 마운트 시점에 고정)
  const [now] = useState(() => Date.now())

  const list = useAsync(() => fetchAuctions({ page, size: PAGE_SIZE }), [page])
  const pageData = list.data

  const goDetail = (id) => navigate(`/admin/auctions/${id}`)

  const columns = [
    { key: 'auctionId', header: '경매번호', width: 90 },
    { key: 'productName', header: '상품명', className: 'strong' },
    { key: 'organizationName', header: '제작기관' },
    { key: 'startPrice', header: '시작가', align: 'right', render: (r) => formatPrice(r.startPrice) },
    {
      key: 'topPrice',
      header: '현재가',
      align: 'right',
      className: 'strong',
      render: (r) => (r.bidCount ? formatPrice(r.topPrice) : '-'),
    },
    { key: 'bidCount', header: '입찰 수', align: 'right', render: (r) => formatNumber(r.bidCount ?? 0) },
    { key: 'startTime', header: '시작 일시', render: (r) => formatDateTime(r.startTime) },
    { key: 'endTime', header: '종료 일시', render: (r) => formatDateTime(r.endTime) },
    {
      key: 'status',
      header: '상태',
      render: (r) => {
        const status = getAuctionStatus(r, now)
        return <Badge tone={status.tone}>{status.label}</Badge>
      },
    },
    {
      key: 'actions',
      header: '관리',
      align: 'right',
      render: (r) => (
        <LinkButton
          onClick={(e) => {
            e.stopPropagation()
            goDetail(r.auctionId)
          }}
        >
          상세
        </LinkButton>
      ),
    },
  ]

  return (
    <AdminPage title="경매 관리">
      <Card noBody>
        <div className="auction-list-head">
          <Summary label={`전체 ${formatNumber(pageData?.totalElements ?? 0)}건`} />
          <span className="auction-list-head__hint">상태는 시작·종료 일시 기준으로 표시됩니다.</span>
        </div>
        <DataTable
          rowKey="auctionId"
          columns={columns}
          rows={pageData?.content}
          loading={list.loading && !pageData}
          error={list.error}
          emptyText="등록된 경매가 없습니다."
          onRowClick={(r) => goDetail(r.auctionId)}
        />
        {pageData && (
          <Pagination
            page={pageData.page}
            totalPages={pageData.totalPages}
            onChange={(p) => setParams(p > 0 ? { page: String(p) } : {})}
          />
        )}
      </Card>
    </AdminPage>
  )
}
