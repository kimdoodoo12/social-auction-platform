import { useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import AdminPage from '../../../components/admin/AdminPage'
import DataTable from '../../../components/admin/DataTable'
import ImageBox from '../../../components/admin/ImageBox'
import Pagination from '../../../components/admin/Pagination'
import { AsyncBoundary, BackLink, Card, InfoRows, StatCard } from '../../../components/admin/ui'
import { fetchAuctionDetail } from '../../../api/auction'
import { fetchBidResult, fetchBids } from '../../../api/bid'
import { useAsync } from '../../../hooks/useAsync'
import { formatDate, formatDateTime, formatNumber, formatPrice, maskPhone } from '../../../utils/format'
import './auction.css'

// [ADMIN] 14 경매 상세 (Figma 51:324)
// GET /auction/detail/{id}, GET /bid/detail?auctionId (입찰 기록), GET /bid/bidDetail/{id} (낙찰 결과)
// 응답에 없는 값(경매 상태, 상품번호, 회원번호, 참여 입찰자 수)은 '-'로 표시한다(사용자 결정).
const BID_PAGE_SIZE = 7

export default function AuctionDetailPage() {
  const { auctionId } = useParams()
  const navigate = useNavigate()
  const [bidPage, setBidPage] = useState(0)

  const detail = useAsync(() => fetchAuctionDetail(auctionId), [auctionId])
  const bids = useAsync(() => fetchBids({ auctionId, page: bidPage, size: BID_PAGE_SIZE }), [auctionId, bidPage])
  const result = useAsync(() => fetchBidResult(auctionId), [auctionId])

  const data = detail.data
  const auction = data?.auctionFindAllDto
  const hasBids = Boolean(auction?.bidCount)
  const ended = Boolean(auction?.endTime) && new Date(auction.endTime) <= new Date()
  const winner = result.data

  return (
    <AdminPage title={`경매 상세 · ${auctionId}`}>
      <BackLink to="/admin/auctions">경매 관리로 돌아가기</BackLink>

      <AsyncBoundary loading={detail.loading && !data} error={detail.error}>
        {auction && (
          <>
            <section className="admin-card admin-hero">
              <ImageBox path={data.imageList?.[0]?.image} alt={auction.productName} className="auction-hero-thumb" label="" />
              <div className="admin-hero__body">
                <div className="admin-hero__meta">- · {data.categoryName ?? '-'}</div>
                <div className="admin-hero__title">{auction.productName}</div>
                <div className="admin-hero__sub">
                  {auction.organizationName} · 담당자 {data.manager ?? '-'} · 협약일 {formatDate(data.agreementDate)}
                </div>
              </div>
              <div className="auction-hero-status">
                <span className="auction-hero-status__badge">-</span>
                <span>{auction.endTime ? `${formatDateTime(auction.endTime)} 종료` : '-'}</span>
              </div>
            </section>

            <div className="auction-stats">
              <StatCard label="시작가" value={formatPrice(auction.startPrice)} />
              <StatCard label="최종 낙찰가" value={hasBids ? formatPrice(auction.topPrice) : null} highlight />
              <StatCard label="최고 입찰자" value={data.userName ? `${data.userName} (-)` : null} />
              <StatCard label="전체 입찰 횟수" value={formatNumber(auction.bidCount ?? 0)} unit="회" />
              <StatCard label="참여 입찰자" value={null} />
            </div>

            <section className="admin-card auction-times">
              <div>
                <span>경매 시작 시각 (첫 입찰 시점)</span>
                <strong>{formatDateTime(auction.startTime)}</strong>
              </div>
              <div>
                <span>경매 종료 시각 (시작 + 24h)</span>
                <strong>{formatDateTime(auction.endTime)}</strong>
              </div>
              <div>
                <span>낙찰 확정 시각</span>
                <strong>{ended ? formatDateTime(auction.endTime) : '-'}</strong>
              </div>
            </section>

            <div className="admin-split auction-split">
              <Card
                title="입찰 기록"
                actions={<span className="auction-card-meta">총 {formatNumber(auction.bidCount ?? 0)}회 · 최신순</span>}
                noBody
              >
                <DataTable
                  rowKey="bidId"
                  rows={bids.data?.content}
                  loading={bids.loading && !bids.data}
                  error={bids.error}
                  emptyText="입찰 기록이 없습니다."
                  onRowClick={(r) => navigate(`/admin/members/${r.memberId}`)}
                  columns={[
                    { key: 'bidId', header: '#', width: 50, align: 'right' },
                    { key: 'name', header: '사용자', className: 'strong' },
                    { key: 'memberId', header: '회원번호' },
                    { key: 'bidPrice', header: '입찰 금액', align: 'right', render: (r) => formatPrice(r.bidPrice) },
                    { key: 'bidTime', header: '입찰 시간', align: 'right', render: (r) => formatDateTime(r.bidTime) },
                  ]}
                />
                {bids.data && bids.data.totalPages > 1 && (
                  <Pagination page={bids.data.page} totalPages={bids.data.totalPages} onChange={setBidPage} />
                )}
              </Card>

              <Card title="최종 낙찰 결과">
                <div className="admin-stack">
                  <InfoRows
                    items={[
                      { label: '낙찰자', value: winner?.name ? `${winner.name} (-)` : '-' },
                      { label: '연락처', value: maskPhone(winner?.phone) },
                      {
                        label: '최종 낙찰가',
                        value: winner?.bidPrice != null ? formatPrice(winner.bidPrice) : '-',
                        className: winner?.bidPrice != null ? 'highlight' : '',
                      },
                      { label: '낙찰일시', value: formatDateTime(winner?.endTime) },
                      {
                        label: '결제 상태',
                        value: winner ? (winner.paymentStatus ? '결제 완료' : '결제 대기') : '-',
                      },
                    ]}
                  />
                  {result.error && <p className="auction-error">낙찰 결과를 불러오지 못했습니다: {result.error.message}</p>}
                  <p className="auction-note">
                    낙찰은 경매 종료 시점에 시스템이 자동 확정합니다. 관리자가 수동으로 변경할 수 없습니다.
                  </p>
                </div>
              </Card>
            </div>
          </>
        )}
      </AsyncBoundary>
    </AdminPage>
  )
}
