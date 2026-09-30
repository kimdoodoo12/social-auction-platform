import { useState } from 'react'
import { useLocation, useNavigate, useParams } from 'react-router-dom'
import { fetchCategoryOptions, fetchOrganizationOptions, fetchProduct, stopSelling } from '../../../api/product'
import AdminPage from '../../../components/admin/AdminPage'
import ImageBox from '../../../components/admin/ImageBox'
import {
  AsyncBoundary,
  BackLink,
  Button,
  Card,
  ConfirmModal,
  InfoRows,
} from '../../../components/admin/ui'
import { useAsync } from '../../../hooks/useAsync'
import { formatDate, formatDateTime, formatNumber, formatPrice } from '../../../utils/format'
import { ProductStatusBadge } from './ProductParts'
import { deriveStatus } from './productStatus'
import './product.css'

// [ADMIN] 04 상품 상세 (Figma 73:261) — GET /admin/product/bb/{productId} → TotalDto
// TotalDto = { productDto, productAuctionInfo, recentBid[] }
// Figma 항목 중 백엔드에 없는 공개 여부·최소 입찰 단위·등록자/수정자는 숨겼다(사용자 결정).

const nameOf = (options, id) => options?.find((o) => o.id === id)?.name

const IMAGE_SLOTS = 5 // 대표 1 + 추가 4

// Figma "상태 이력": 등록일·경매 시작·종료 시각으로 만든다
function buildHistory(product, info, status) {
  const now = new Date()
  const items = [{ title: '상품 등록 · 경매 대기', time: formatDateTime(product.createdAt), done: true }]
  if (info?.startTime) {
    items.push({ title: '첫 입찰 접수 · 경매 시작', time: formatDateTime(info.startTime), done: true })
  }
  if (status === '진행') {
    items.push({ title: '경매 진행 중', time: `입찰 ${formatNumber(info?.bidCount ?? 0)}회`, done: true })
  }
  if (status === '판매 중지') {
    items.push({ title: '판매 중지', time: '', done: true })
  } else if (info?.endTime) {
    const ended = new Date(info.endTime) <= now
    items.push({
      title: '경매 종료 · 낙찰 확정',
      time: `${formatDateTime(info.endTime)}${ended ? '' : ' 예정'}`,
      done: ended,
    })
  } else {
    items.push({ title: '경매 종료 · 낙찰 확정', time: '첫 입찰 후 24시간 뒤', done: false })
  }
  return items
}

function DescBlock({ label, value }) {
  return (
    <div className="product-desc">
      <span className="product-desc__label">{label}</span>
      <p className={value ? 'product-desc__text' : 'product-desc__text product-desc__text--empty'}>
        {value || '내용이 없습니다.'}
      </p>
    </div>
  )
}

export default function ProductDetailPage() {
  const { productId } = useParams()
  const navigate = useNavigate()
  const listState = useLocation().state ?? {}

  const [confirmOpen, setConfirmOpen] = useState(false)
  const [stopResult, setStopResult] = useState(null) // { ok, message }

  const detail = useAsync(() => fetchProduct(productId), [productId])
  const categories = useAsync(fetchCategoryOptions, [])
  const organizations = useAsync(fetchOrganizationOptions, [])

  const product = detail.data?.productDto
  const info = detail.data?.productAuctionInfo
  const recentBids = detail.data?.recentBid ?? []

  const status = stopResult?.ok ? '판매 중지' : (listState.status ?? (detail.data ? deriveStatus(info) : null))
  const canStop = status === '진행' || status === '대기'

  const handleStop = async () => {
    setConfirmOpen(false)
    try {
      await stopSelling(productId)
      setStopResult({ ok: true, message: '판매 중지되었습니다.' })
    } catch (error) {
      setStopResult({ ok: false, message: `판매 중지 실패: ${error.message}` })
    }
  }

  const categoryName = product && (nameOf(categories.data, product.categoryId) ?? '-')
  const organizationName = product && (nameOf(organizations.data, product.organizationId) ?? '-')
  const images = product?.images ?? []

  return (
    <AdminPage title={`상품 상세 · ${productId}`}>
      <BackLink to="/admin/products">상품 관리로 돌아가기</BackLink>

      {stopResult && (
        <div className={stopResult.ok ? 'admin-mock-notice' : 'admin-state admin-state--error'}>{stopResult.message}</div>
      )}

      <AsyncBoundary loading={detail.loading && !product} error={detail.error}>
        {product && (
          <>
            <section className="admin-card admin-hero">
              <ImageBox path={images[0]?.image} alt={product.name} className="admin-hero__thumb" label="" />
              <div className="admin-hero__body">
                <div className="admin-hero__meta">
                  <span>
                    {product.productId} · {categoryName}
                  </span>
                  <ProductStatusBadge status={status} />
                </div>
                <div className="admin-hero__title">{product.name}</div>
                <div className="admin-hero__sub">
                  {organizationName} · 등록 {formatDate(product.createdAt)}
                </div>
              </div>
              <div className="admin-actions">
                <Button size="lg" onClick={() => navigate(`/admin/products/${productId}/edit`, { state: { status } })}>
                  수정
                </Button>
                <Button size="lg" pending>
                  사용자 화면에서 보기
                </Button>
              </div>
            </section>

            <div className="admin-split product-split">
              <div className="admin-stack">
                <Card title="기본 정보">
                  <InfoRows
                    items={[
                      { label: '상품번호', value: product.productId },
                      { label: '카테고리', value: categoryName },
                      { label: '제작 기관', value: organizationName },
                      { label: '등록일시', value: formatDateTime(product.createdAt) },
                      { label: '최종 수정', value: formatDateTime(product.updatedAt) },
                    ]}
                  />
                </Card>

                <Card title="가격 · 경매 정보" subtitle="경매 시작 시각은 첫 입찰 시점에 자동 기록됩니다.">
                  <InfoRows
                    items={[
                      { label: '시작가', value: formatPrice(info?.startPrice ?? product.startPrice) },
                      {
                        label: '현재 최고가',
                        value: info?.bidCount ? formatPrice(info.currentPrice) : '—',
                        className: info?.bidCount ? 'highlight' : '',
                      },
                      { label: '총 입찰 횟수', value: `${formatNumber(info?.bidCount ?? 0)}회` },
                      { label: '참여 입찰자', value: `${formatNumber(info?.bidderCount ?? 0)}명` },
                      { label: '경매 시작 시각', value: formatDateTime(info?.startTime) },
                      { label: '경매 종료 예정', value: formatDateTime(info?.endTime) },
                      { label: '경매번호', value: info?.auctionId ?? '-' },
                    ]}
                  />
                </Card>

                <Card title="상품 설명">
                  <div className="admin-stack product-desc-list">
                    <DescBlock label="상품 설명" value={product.description} />
                    <DescBlock label="상품 제작 배경" value={product.background} />
                  </div>
                </Card>

                <Card
                  title="최근 입찰 내역"
                  subtitle={`총 ${formatNumber(recentBids[0]?.totalCount ?? 0)}회 · 최신 ${recentBids.length}건`}
                  noBody
                >
                  {recentBids.length === 0 ? (
                    <div className="admin-state">아직 입찰이 없습니다.</div>
                  ) : (
                    <ul className="product-bids">
                      {recentBids.map((bid, i) => (
                        <li key={bid.bidNumber} className={i === 0 ? 'top' : undefined}>
                          <span className="product-bids__no">{bid.bidNumber}</span>
                          <span className="product-bids__name">
                            {bid.memberName} ({bid.memberId})
                          </span>
                          <span className="product-bids__price">{formatPrice(bid.bidPrice)}</span>
                          <span className="product-bids__time">{formatDateTime(bid.bidTime)}</span>
                        </li>
                      ))}
                    </ul>
                  )}
                </Card>
              </div>

              <div className="admin-stack">
                <Card title="상품 이미지">
                  <div className="product-gallery">
                    <ImageBox path={images[0]?.image} alt={product.name} className="product-gallery__main" />
                    <div className="product-gallery__thumbs">
                      {Array.from({ length: IMAGE_SLOTS - 1 }, (_, i) => (
                        <ImageBox key={i} path={images[i + 1]?.image} alt={product.name} className="product-gallery__thumb" label="" />
                      ))}
                    </div>
                  </div>
                </Card>

                <Card title="상태 이력">
                  <ul className="product-history">
                    {buildHistory(product, info, status).map((h) => (
                      <li key={h.title} className={h.done ? 'done' : undefined}>
                        <strong>{h.title}</strong>
                        {h.time && <span>{h.time}</span>}
                      </li>
                    ))}
                  </ul>
                </Card>

                <Card title="판매 중지 · 삭제">
                  <div className="admin-stack product-danger">
                    <div>
                      <strong>판매 중지</strong>
                      <p>사용자 화면에서만 숨깁니다. 상품·입찰·낙찰 데이터는 모두 남습니다.</p>
                      <Button
                        size="lg"
                        className="admin-btn--block"
                        disabled={!canStop}
                        title={canStop ? undefined : '판매 중지할 수 있는 경매가 없습니다.'}
                        onClick={() => setConfirmOpen(true)}
                      >
                        판매 중지
                      </Button>
                    </div>
                    <div>
                      <strong>삭제</strong>
                      <p>데이터를 영구 삭제합니다. 입찰 이력이 하나라도 있으면 삭제할 수 없습니다.</p>
                      <Button size="lg" className="admin-btn--block" pending>
                        삭제 (비활성)
                      </Button>
                    </div>
                  </div>
                </Card>
              </div>
            </div>
          </>
        )}
      </AsyncBoundary>

      <ConfirmModal
        open={confirmOpen}
        title="판매 중지"
        message={product ? `'${product.name}' 상품을 판매 중지할까요?` : ''}
        confirmText="판매 중지"
        danger
        onConfirm={handleStop}
        onCancel={() => setConfirmOpen(false)}
      />
    </AdminPage>
  )
}
