import { useLocation, useNavigate, useParams } from 'react-router-dom'
import { fetchProduct, updateProduct } from '../../../api/product'
import AdminPage from '../../../components/admin/AdminPage'
import { AsyncBoundary } from '../../../components/admin/ui'
import { useAsync } from '../../../hooks/useAsync'
import ProductForm from './ProductForm'
import './product.css'

// [ADMIN] 06 상품 수정 — GET /product/bb 로 불러와 PUT /product/dd 로 저장
// ProductDto 응답에 시작가가 없어 목록에서 넘어온 값(location.state)이 있으면 채우고, 없으면 다시 입력받는다.

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

  const handleSubmit = async (productDto) => {
    await updateProduct(productDto)
    // 목록 상태값은 그대로 두고, 바뀐 시작가만 반영해서 상세로 돌아간다
    navigate(detailPath, { state: { ...listState, startPrice: productDto.startPrice } })
  }

  return (
    <AdminPage title="상품 수정" back={detailPath}>
      <AsyncBoundary loading={detail.loading && !detail.data} error={detail.error}>
        {detail.data && (
          <ProductForm
            key={detail.data.productId}
            initial={toFormValues(detail.data, listState.startPrice)}
            submitLabel="수정 저장"
            onSubmit={handleSubmit}
            onCancel={() => navigate(detailPath, { state: listState })}
            startPriceHint={
              listState.startPrice == null ? '상세 조회 응답에 시작가가 없어 다시 입력해야 합니다.' : undefined
            }
          />
        )}
      </AsyncBoundary>
    </AdminPage>
  )
}
