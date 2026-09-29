import { Link } from 'react-router-dom'
import AdminPage from '../../components/admin/AdminPage'
import DataTable from '../../components/admin/DataTable'
import { Badge, Card, MockNotice } from '../../components/admin/ui'
import { formatPrice } from '../../utils/format'
import './pages.css'

// [ADMIN] 02 Dashboard
// 백엔드에 대시보드(통계) API가 없어 Figma 기준 mock 데이터를 표시한다.
const STATS = [
  { label: '전체 상품', value: 412, unit: '개' },
  { label: '경매 대기', value: 57, unit: '개' },
  { label: '진행 중인 경매', value: 38, unit: '건', highlight: true },
  { label: '종료된 경매', value: 317, unit: '건' },
  { label: '전체 회원', value: 2840, unit: '명' },
  { label: '협약 기관', value: 27, unit: '곳' },
]

const RECENT_PRODUCTS = [
  { id: 1, name: '편백나무 우드 디퓨저', org: '늘품공방', price: 15000, status: '경매 대기' },
  { id: 2, name: '수제 대추청 500ml', org: '씨앗농장', price: 18000, status: '경매 대기' },
  { id: 3, name: '업사이클 캔버스 에코백', org: '온새미로', price: 21000, status: '경매 대기' },
  { id: 4, name: '손바느질 소가죽 카드지갑', org: '참빛작업장', price: 29000, status: '경매 대기' },
  { id: 5, name: '라탄 소품 바구니 (중)', org: '소리작업장', price: 25000, status: '경매 진행 중' },
]

const LIVE_AUCTIONS = [
  { id: 1, name: '손뜨개 울 머플러 (차콜)', price: 46000, remain: '09:31:12' },
  { id: 2, name: '수제 곶감 정과 선물세트', price: 38000, remain: '02:05:41' },
  { id: 3, name: '물레 성형 도자 머그 2P', price: 35000, remain: '15:44:02' },
  { id: 4, name: '참죽나무 원목 도마 (대)', price: 24000, remain: '01:20:55' },
  { id: 5, name: '리넨 파우치 3종 세트', price: 17000, remain: '06:19:38' },
]

const RECENT_WINS = [
  { id: 'A-1042', name: '원목 티스푼 4P 세트', org: '마루공방', winner: '김**', price: 21000, date: '2026-09-16', pay: '결제 완료' },
  { id: 'A-1041', name: '천연염색 손수건 2P', org: '하랑공방', winner: '이**', price: 19500, date: '2026-09-16', pay: '결제 대기' },
  { id: 'A-1039', name: '유기농 현미 강정 선물세트', org: '해뜨는집', winner: '박**', price: 31000, date: '2026-09-15', pay: '배송 준비' },
]

const statusTone = (status) => (status === '경매 진행 중' ? 'primary' : status === '경매 종료' ? 'dark' : 'outline')

export default function DashboardPage() {
  return (
    <AdminPage title="Dashboard">
      <MockNotice>대시보드 통계 API가 없어 예시 데이터를 표시합니다.</MockNotice>

      <div className="dash-stats">
        {STATS.map((s) => (
          <div key={s.label} className="admin-card dash-stat">
            <span className="dash-stat__label">{s.label}</span>
            <span className={`dash-stat__value${s.highlight ? ' highlight' : ''}`}>
              {s.value.toLocaleString()}
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
            rowKey="id"
            rows={RECENT_PRODUCTS}
            columns={[
              { key: 'name', header: '상품명', className: 'strong' },
              { key: 'org', header: '제작기관' },
              { key: 'price', header: '시작가', align: 'right', render: (r) => formatPrice(r.price) },
              { key: 'status', header: '상태', render: (r) => <Badge tone={statusTone(r.status)}>{r.status}</Badge> },
            ]}
          />
        </Card>
        <Card title="진행 중인 경매" actions={<span className="dash-more">전체 38건</span>} noBody>
          <DataTable
            rowKey="id"
            rows={LIVE_AUCTIONS}
            columns={[
              { key: 'name', header: '상품명', className: 'strong' },
              { key: 'price', header: '현재가', align: 'right', render: (r) => formatPrice(r.price) },
              { key: 'remain', header: '남은 시간', align: 'right' },
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
          rowKey="id"
          rows={RECENT_WINS}
          columns={[
            { key: 'id', header: '경매번호' },
            { key: 'name', header: '상품명', className: 'strong' },
            { key: 'org', header: '제작기관' },
            { key: 'winner', header: '낙찰자' },
            { key: 'price', header: '낙찰가', align: 'right', render: (r) => formatPrice(r.price) },
            { key: 'date', header: '낙찰일', align: 'right' },
            { key: 'pay', header: '결제 상태', align: 'right' },
          ]}
        />
      </Card>
    </AdminPage>
  )
}
