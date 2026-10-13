import { Link } from 'react-router-dom'
import AdminPage from '../../components/admin/AdminPage'
import DataTable from '../../components/admin/DataTable'
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
}
