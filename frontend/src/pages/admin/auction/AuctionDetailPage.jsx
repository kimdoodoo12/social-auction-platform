import { useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import AdminPage from '../../../components/admin/AdminPage'
import DataTable from '../../../components/admin/DataTable'
import Pagination from '../../../components/admin/Pagination'
import { AsyncBoundary, Badge, Card, DetailList } from '../../../components/admin/ui'
import { BASE_URL } from '../../../api/client'
import { fetchAuctionDetail } from '../../../api/auction'
import { fetchBidResult, fetchBids } from '../../../api/bid'
import { useAsync } from '../../../hooks/useAsync'
import { formatDate, formatDateTime, formatNumber, formatPrice } from '../../../utils/format'
import { getAuctionStatus } from './auctionStatus'
import './auction.css'

// [ADMIN] 14 경매 상세
const BID_PAGE_SIZE = 7

// ImageDto.image는 경로 문자열이다. 상대 경로면 백엔드 주소를 붙인다.
function imageUrl(path) {
  if (!path) return null
  if (/^https?:\/\//.test(path)) return path
  return `${BASE_URL}${path.startsWith('/') ? '' : '/'}${path}`
}

function AuctionImage({ image, alt }) {
  const [failed, setFailed] = useState(false)
  const src = imageUrl(image)
  return (
    <div className="auction-images__item">
      {src && !failed ? <img src={src} alt={alt} onError={() => setFailed(true)} /> : '이미지 없음'}
    </div>
  )
}

function Stat({ label, value, highlight }) {
  return (
    <div className="admin-card auction-stat">
      <span className="auction-stat__label">{label}</span>
      <span className={`auction-stat__value${highlight ? ' highlight' : ''}`}>{value}</span>
    </div>
  )
}

export default function AuctionDetailPage() {
  const { auctionId } = useParams()
  const [now] = useState(() => Date.now())
  const [bidPage, setBidPage] = useState(0)

  const detail = useAsync(() => fetchAuctionDetail(auctionId), [auctionId])
  const bids = useAsync(() => fetchBids({ auctionId, page: bidPage, size: BID_PAGE_SIZE }), [auctionId, bidPage])
  const result = useAsync(() => fetchBidResult(auctionId), [auctionId])

  const data = detail.data
  const auction = data?.auctionFindAllDto
  const status = auction ? getAuctionStatus(auction, now) : null
  const hasBid = Boolean(auction?.bidCount)

  return (
    <AdminPage title="경매 상세" back="/admin/auctions">
      <AsyncBoundary loading={detail.loading && !data} error={data ? null : detail.error}>
        {auction && (
          <>
            <div className="auction-stats">
              <Stat label="상태" value={<Badge tone={status.tone}>{status.label}</Badge>} />
              <Stat label="시작가" value={formatPrice(auction.startPrice)} />
              <Stat label="현재 최고가" value={hasBid ? formatPrice(auction.topPrice) : '-'} highlight />
              <Stat label="입찰 수" value={`${formatNumber(auction.bidCount ?? 0)}건`} />
            </div>

            <div className="auction-detail-grid">
              <Card title="경매 정보">
                <DetailList
                  items={[
                    { label: '경매번호', value: auction.auctionId },
                    { label: '상품명', value: auction.productName },
                    { label: '카테고리', value: data.categoryName },
                    { label: '제작기관', value: auction.organizationName },
                    { label: '기관 담당자', value: data.manager },
                    { label: '협약일', value: formatDate(data.agreementDate) },
                    { label: '시작 일시', value: formatDateTime(auction.startTime) },
                    { label: '종료 일시', value: formatDateTime(auction.endTime) },
                    { label: '최고 입찰자', value: data.userName },
                  ]}
                />
              </Card>

              <div className="auction-detail-side">
                <Card title="상품 이미지">
                  {data.imageList?.length ? (
                    <div className="auction-images">
                      {data.imageList.map((img) => (
                        <AuctionImage key={img.imageId} image={img.image} alt={auction.productName} />
                      ))}
                    </div>
                  ) : (
                    <span className="auction-empty">등록된 이미지가 없습니다.</span>
                  )}
                </Card>

                <Card title="낙찰 정보">
                  {!status.ended ? (
                    <span className="auction-empty">경매가 종료되면 낙찰 정보가 표시됩니다.</span>
                  ) : (
                    <AsyncBoundary loading={result.loading} error={result.error}>
                      {result.data ? (
                        <DetailList
                          items={[
                            { label: '낙찰자', value: result.data.name },
                            { label: '연락처', value: result.data.phone },
                            { label: '낙찰가', value: formatPrice(result.data.bidPrice) },
                            { label: '종료 일시', value: formatDateTime(result.data.endTime) },
                            {
                              label: '결제 상태',
                              value: (
                                <Badge tone={result.data.paymentStatus ? 'soft' : 'outline'}>
                                  {result.data.paymentStatus ? '결제 완료' : '결제 대기'}
                                </Badge>
                              ),
                            },
                          ]}
                        />
                      ) : (
                        <span className="auction-empty">입찰 없이 종료된 경매입니다.</span>
                      )}
                    </AsyncBoundary>
                  )}
                </Card>
              </div>
            </div>
          </>
        )}
      </AsyncBoundary>

      {auction && (
        <Card title={`입찰 기록 (${formatNumber(bids.data?.totalElements ?? 0)}건)`} noBody>
          <DataTable
            rowKey="bidId"
            rows={bids.data?.content}
            loading={bids.loading && !bids.data}
            error={bids.error}
            emptyText="입찰 기록이 없습니다."
            columns={[
              { key: 'bidId', header: '입찰번호', width: 100 },
              {
                key: 'name',
                header: '입찰자',
                className: 'strong',
                render: (r) => (r.memberId ? <Link to={`/admin/members/${r.memberId}`}>{r.name ?? '-'}</Link> : (r.name ?? '-')),
              },
              { key: 'bidPrice', header: '입찰가', align: 'right', className: 'strong', render: (r) => formatPrice(r.bidPrice) },
              { key: 'bidTime', header: '입찰 일시', align: 'right', render: (r) => formatDateTime(r.bidTime) },
            ]}
          />
          {bids.data && <Pagination page={bids.data.page} totalPages={bids.data.totalPages} onChange={setBidPage} />}
        </Card>
      )}
    </AdminPage>
  )
}
