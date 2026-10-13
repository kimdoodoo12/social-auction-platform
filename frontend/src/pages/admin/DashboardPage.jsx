import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { fetchDashboard } from '../../api/dashboard'
import AdminPage from '../../components/admin/AdminPage'
import DataTable from '../../components/admin/DataTable'
<<<<<<< HEAD
import { AsyncBoundary, Badge, Card } from '../../components/admin/ui'
import { useAsync } from '../../hooks/useAsync'
import { formatDate, formatNumber, formatPrice } from '../../utils/format'
import './pages.css'

// [ADMIN] 02 Dashboard (Figma 31:4) — GET /ieum/admin/dashboard

const STATUS = {
  대기: { label: '경매 대기', tone: 'outline' },
  진행: { label: '경매 진행 중', tone: 'primary' },
  완료: { label: '경매 종료', tone: 'dark' },
}

function StatusBadge({ status }) {
  if (!status) return '-'
  const s = STATUS[status] ?? { label: status, tone: 'outline' }
  return <Badge tone={s.tone}>{s.label}</Badge>
}

// Figma처럼 낙찰자 이름은 첫 글자만 보여준다 (김민지 → 김**)
const maskName = (name) => (name ? name[0] + '*'.repeat(Math.max(name.length - 1, 1)) : '-')

// 종료 시각까지 남은 시간 "HH:MM:SS"
function remaining(endTime, now) {
  if (!endTime) return '-'
  const ms = new Date(endTime).getTime() - now
  if (ms <= 0) return '종료'
  const total = Math.floor(ms / 1000)
  const pad = (n) => String(n).padStart(2, '0')
  return `${pad(Math.floor(total / 3600))}:${pad(Math.floor((total % 3600) / 60))}:${pad(total % 60)}`
}

// 남은 시간 표시용 1초 시계
function useNow() {
  const [now, setNow] = useState(() => Date.now())
  useEffect(() => {
    const id = setInterval(() => setNow(Date.now()), 1000)
    return () => clearInterval(id)
  }, [])
  return now
}

export default function DashboardPage() {
  const { loading, error, data } = useAsync(fetchDashboard, [])
  const now = useNow()
  const counts = data?.counts

  const stats = counts
    ? [
        { label: '전체 상품', value: counts.products, unit: '개' },
        { label: '경매 대기', value: counts.waiting, unit: '개' },
        { label: '진행 중인 경매', value: counts.bidding, unit: '건', highlight: true },
        { label: '종료된 경매', value: counts.ended, unit: '건' },
        { label: '전체 회원', value: counts.members, unit: '명' },
        { label: '협약 기관', value: counts.organizations, unit: '곳' },
      ]
    : []

  return (
    <AdminPage title="Dashboard">
      <AsyncBoundary loading={loading && !data} error={error}>
        {data && (
          <>
            <div className="dash-stats">
              {stats.map((s) => (
                <div key={s.label} className="admin-card dash-stat">
                  <span className="dash-stat__label">{s.label}</span>
                  <span className={`dash-stat__value${s.highlight ? ' highlight' : ''}`}>
                    {formatNumber(s.value)}
                    <small>{s.unit}</small>
                  </span>
                </div>
              ))}
            </div>

            <div className="dash-row">
              <Card
                title="최근 등록 상품"
                actions={<Link to="/admin/products" className="dash-more">상품 관리로 이동 →</Link>}
                noBody
              >
                <DataTable
                  rowKey={(r) => `${r.productName}-${r.organizationName}`}
                  rows={data.newProducts}
                  emptyText="최근 등록된 상품이 없습니다."
                  columns={[
                    { key: 'productName', header: '상품명', className: 'strong' },
                    { key: 'organizationName', header: '제작기관' },
                    // 입찰이 없는 상품만 내려오므로 currentPrice가 곧 시작가다
                    { key: 'currentPrice', header: '시작가', align: 'right', render: (r) => formatPrice(r.currentPrice) },
                    { key: 'auctionStatus', header: '상태', render: (r) => <StatusBadge status={r.auctionStatus} /> },
                  ]}
                />
              </Card>
              <Card
                title="진행 중인 경매"
                actions={<span className="dash-more">전체 {formatNumber(counts.bidding)}건</span>}
                noBody
              >
                <DataTable
                  rowKey={(r) => `${r.productName}-${r.endTime}`}
                  rows={data.liveAuctions}
                  emptyText="진행 중인 경매가 없습니다."
                  columns={[
                    { key: 'productName', header: '상품명', className: 'strong' },
                    { key: 'currentPrice', header: '현재가', align: 'right', render: (r) => formatPrice(r.currentPrice) },
                    { key: 'endTime', header: '남은 시간', align: 'right', render: (r) => remaining(r.endTime, now) },
                  ]}
                />
              </Card>
            </div>

            <Card
              title="최근 낙찰 내역"
              actions={<Link to="/admin/auctions" className="dash-more">경매 관리로 이동 →</Link>}
              noBody
            >
              <DataTable
                rowKey={(r) => `${r.auctionId}-${r.createdAt}`}
                rows={data.recentWins}
                emptyText="최근 낙찰 내역이 없습니다."
                columns={[
                  { key: 'auctionId', header: '경매번호' },
                  { key: 'productName', header: '상품명', className: 'strong' },
                  { key: 'organizationName', header: '제작기관' },
                  { key: 'memberName', header: '낙찰자', render: (r) => maskName(r.memberName) },
                  { key: 'paymentPrice', header: '낙찰가', align: 'right', render: (r) => formatPrice(r.paymentPrice) },
                  { key: 'createdAt', header: '낙찰일', align: 'right', render: (r) => formatDate(r.createdAt) },
                  {
                    key: 'paymentStatus',
                    header: '결제 상태',
                    align: 'right',
                    render: (r) => (r.paymentStatus ? '결제 완료' : '결제 대기'),
                  },
                ]}
              />
            </Card>
          </>
        )}
      </AsyncBoundary>
    </AdminPage>
  )
=======
import { Card, AsyncBoundary } from '../../components/admin/ui'
import { ProductStatusBadge } from './product/ProductParts'
import { formatDateTime, formatPrice } from '../../utils/format'
import { api } from '../../api/client'
import { useAsync } from '../../hooks/useAsync'
import './pages.css'

export default function DashboardPage() {
  const { data, loading, error, reload } = useAsync(() => api.get('/ieum/admin/dashboard'))
  const stats = [
    ['전체 상품', data?.productCount, '개'],
    ['경매 대기', data?.auctionWaitngCount, '개'],
    ['진행 중인 경매', data?.auctionBiddingCount, '건'],
    ['종료된 경매', data?.auctionEndCount, '건'],
    ['전체 회원', data?.memberCount, '명'],
    ['협약 기관', data?.organizationCount ?? data?.OrganizationCount, '곳'],
  ]
  return <AdminPage title="대시보드" description="상품과 경매 현황을 한눈에 확인하세요.">
    {error && <button type="button" className="admin-btn" onClick={reload}>다시 시도</button>}
    <AsyncBoundary loading={loading} error={error}>
      <div className="dash-stats">{stats.map(([label,value,unit]) => <div key={label} className="admin-card dash-stat"><span className="dash-stat__label">{label}</span><span className={'dash-stat__value' + (label === '진행 중인 경매' ? ' highlight' : '')}>{value?.toLocaleString() ?? '—'}<small>{unit}</small></span></div>)}</div>
      <div className="dash-row">
        <Card title="첫 입찰 대기 상품" actions={<Link to="/admin/products" className="dash-more">상품 관리 →</Link>} noBody>
          <DataTable rowKey="auctionId" rows={data?.newProductDtos || []} columns={[
            {key:'productName',header:'상품명',render:r=><Link to={'/admin/products/'+r.productId}>{r.productName}</Link>},
            {key:'organizationName',header:'제작기관'},
            {key:'currentPrice',header:'시작가',align:'right',render:r=>formatPrice(r.currentPrice)},
            {key:'auctionStatus',header:'상태',render:r=><ProductStatusBadge status={r.auctionStatus} />},
          ]} />
        </Card>
        <Card title="진행 중인 경매" actions={<Link to="/admin/auctions" className="dash-more">경매 관리 →</Link>} noBody>
          <DataTable rowKey="auctionId" rows={data?.endProductDtos || []} columns={[
            {key:'productName',header:'상품명',render:r=><Link to={'/admin/auctions/'+r.auctionId}>{r.productName}</Link>},
            {key:'currentPrice',header:'현재가',align:'right',render:r=>formatPrice(r.currentPrice)},
            {key:'endTime',header:'종료 시각',align:'right',render:r=>formatDateTime(r.endTime)},
          ]} />
        </Card>
      </div>
      <Card title="최근 낙찰 내역" actions={<Link to="/admin/auctions" className="dash-more">경매 관리 →</Link>} noBody>
        <DataTable rowKey="auctionId" rows={data?.paymentListResponses || []} columns={[
          {key:'auctionId',header:'경매번호',render:r=><Link to={'/admin/auctions/'+r.auctionId}>#{r.auctionId}</Link>},
          {key:'productName',header:'상품명'}, {key:'organizationName',header:'제작기관'},
          {key:'memeberName',header:'낙찰자',render:r=>r.memberName ?? r.memeberName ?? '—'},
          {key:'paymentPrice',header:'낙찰가',align:'right',render:r=>formatPrice(r.paymentPrice)},
          {key:'createdAt',header:'낙찰일',render:r=>formatDateTime(r.createdAt)},
          {key:'paymentStatus',header:'결제 상태',render:r=>r.paymentStatus ? '결제 완료' : '결제 대기'},
        ]} />
      </Card>
    </AsyncBoundary>
  </AdminPage>
>>>>>>> 3772045bfec684956433f1d1ec8f36509ac3cae3
}
