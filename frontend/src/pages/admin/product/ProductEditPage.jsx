import { useLocation, useNavigate, useParams } from 'react-router-dom'
import { fetchProduct, updateProduct } from '../../../api/product'
import AdminPage from '../../../components/admin/AdminPage'
import { AsyncBoundary, BackLink } from '../../../components/admin/ui'
import { useAsync } from '../../../hooks/useAsync'
import ProductForm from './ProductForm'
import './product.css'

// [ADMIN] 06 상품 수정 (Figma 73:3397) — GET /admin/product/bb/{productId} 로 불러와 PUT /admin/product/dd 로 저장
// 시작가는 productAuctionInfo.startPrice 를 쓴다(productDto.startPrice 는 비어서 온다).

function toFormValues(product, startPrice) {
  return {
    productId: product.productId,
    name: product.name ?? '',
    organizationId: product.organizationId != null ? String(product.organizationId) : '',
    categoryId: product.categoryId != null ? String(product.categoryId) : '',
    startPrice: startPrice != null ? String(startPrice) : '',
    description: product.description ?? '',
    background: product.background ?? '',
    images: (product.images ?? []).map((img) => ({ imageId: img.imageId, image: img.image ?? '' })),
  }
}

export default function ProductEditPage() {
  const { productId } = useParams()
  const navigate = useNavigate()
  const listState = useLocation().state ?? {}
  const detailPath = `/admin/products/${productId}`

  const detail = useAsync(() => fetchProduct(productId), [productId])
  const product = detail.data?.productDto
  const startPrice = detail.data?.productAuctionInfo?.startPrice ?? product?.startPrice

  const handleSubmit = async (productDto) => {
    await updateProduct(productDto)
    navigate(detailPath, { state: listState })
  }

  return (
    <AdminPage title="상품 수정">
      <BackLink to={detailPath}>상품 상세로 돌아가기</BackLink>
      <AsyncBoundary loading={detail.loading && !product} error={detail.error}>
        {product && (
          <ProductForm
            key={product.productId}
            mode="edit"
            initial={toFormValues(product, startPrice)}
            onSubmit={handleSubmit}
            onCancel={() => navigate(detailPath, { state: listState })}
          />
        )}
      </AsyncBoundary>
    </AdminPage>
  )
}
