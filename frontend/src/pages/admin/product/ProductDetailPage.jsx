import { useState } from 'react'
import { useLocation, useNavigate, useParams } from 'react-router-dom'
import {
  fetchCategoryOptions,
  fetchOrganizationOptions,
  fetchProduct,
  NO_AUCTION_STATUS,
  stopSelling,
  STOPPED_STATUS,
} from '../../../api/product'
import AdminPage from '../../../components/admin/AdminPage'
import { AsyncBoundary, Button, Card, ConfirmModal, DetailList } from '../../../components/admin/ui'
import { useAsync } from '../../../hooks/useAsync'
import { formatDateTime, formatPrice } from '../../../utils/format'
import { ProductImage, ProductStatusBadge } from './ProductParts'
import './product.css'

// [ADMIN] 04 상품 상세 — GET /ieum/admin/product/detail/{productId}
// 상품은 productDto, 시작가는 productAuctionInfo에서 읽고 경매 상태는 목록에서 전달받는다.

const nameOf = (options, id) => options?.find((o) => o.id === id)?.name

function Text({ value }) {
  if (!value) return <p className="product-text product-text--empty">내용이 없습니다.</p>
  return <p className="product-text">{value}</p>
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
  const auctionInfo = detail.data?.productAuctionInfo
  const startPrice = auctionInfo?.startPrice ?? listState.startPrice
  const status = stopResult?.ok ? STOPPED_STATUS : listState.status
  // 목록을 거치지 않고 들어오면 상태를 알 수 없으므로 버튼을 열어두고 서버 응답으로 판단한다
  const stopDisabled = status === NO_AUCTION_STATUS || status === STOPPED_STATUS

  const handleStop = async () => {
    setConfirmOpen(false)
    try {
      await stopSelling(productId)
      setStopResult({ ok: true, message: '판매 중지되었습니다.' })
    } catch (error) {
      setStopResult({ ok: false, message: `판매 중지 실패: ${error.message}` })
    }
  }

  const goEdit = () => navigate(`/admin/products/${productId}/edit`, { state: listState })

  return (
    <AdminPage
      title="상품 상세"
      back="/admin/products"
      actions={
        product && (
          <>
            <Button variant="danger" disabled={stopDisabled} onClick={() => setConfirmOpen(true)}>
              판매 중지
            </Button>
            <Button variant="dark" onClick={goEdit}>
              상품 수정
            </Button>
          </>
        )
      }
    >
      {stopResult && (
        <div className={stopResult.ok ? 'admin-mock-notice' : 'admin-state admin-state--error'}>{stopResult.message}</div>
      )}

      <AsyncBoundary loading={detail.loading && !product} error={detail.error}>
        {product && (
          <div className="product-detail">
            <Card title="기본 정보">
              <DetailList
                items={[
                  { label: '상품번호', value: product.productId },
                  { label: '상품명', value: product.name },
                  {
                    label: '제작기관',
                    value: nameOf(organizations.data, product.organizationId) ?? `기관 #${product.organizationId}`,
                  },
                  {
                    label: '카테고리',
                    value: nameOf(categories.data, product.categoryId) ?? `카테고리 #${product.categoryId}`,
                  },
                  { label: '시작가', value: startPrice != null ? formatPrice(startPrice) : '-' },
                  { label: '경매 상태', value: status ? <ProductStatusBadge status={status} /> : '-' },
                  { label: '등록일', value: formatDateTime(product.createdAt) },
                  { label: '수정일', value: formatDateTime(product.updatedAt) },
                ]}
              />
            </Card>

            <div className="product-detail__side">
              <Card title="상품 설명">
                <Text value={product.description} />
              </Card>
              <Card title="제작 배경">
                <Text value={product.background} />
              </Card>
            </div>
          </div>
        )}

        {product && (
          <Card title={`상품 이미지 (${product.images?.length ?? 0})`}>
            {product.images?.length ? (
              <div className="product-gallery">
                {product.images.map((img) => (
                  <ProductImage key={img.imageId} path={img.image} alt={product.name} />
                ))}
              </div>
            ) : (
              <p className="product-text product-text--empty">등록된 이미지가 없습니다.</p>
            )}
          </Card>
        )}
      </AsyncBoundary>

      <ConfirmModal
        open={confirmOpen}
        title="판매 중지"
        message={product ? `'${product.name}' 상품의 경매를 판매 중지할까요?` : ''}
        confirmText="판매 중지"
        danger
        onConfirm={handleStop}
        onCancel={() => setConfirmOpen(false)}
      />
    </AdminPage>
  )
}
