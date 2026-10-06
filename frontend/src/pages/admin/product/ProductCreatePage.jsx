import { useNavigate } from 'react-router-dom'
import { createProduct } from '../../../api/product'
import AdminPage from '../../../components/admin/AdminPage'
import { BackLink } from '../../../components/admin/ui'
import ProductForm from './ProductForm'
import './product.css'

// [ADMIN] 05 상품 등록 — POST /ieum/admin/product/add
// 응답이 boolean 이라 새 상품번호를 알 수 없어, 등록 후 목록으로 돌아간다.
const EMPTY_PRODUCT = {
  productId: null,
  name: '',
  organizationId: '',
  categoryId: '',
  startPrice: '',
  description: '',
  background: '',
  images: [],
}

export default function ProductCreatePage() {
  const navigate = useNavigate()

  const handleSubmit = async (productDto) => {
    await createProduct(productDto)
    navigate('/admin/products')
  }

  return (
    <AdminPage title="상품 등록">
      <BackLink to="/admin/products">상품 관리로 돌아가기</BackLink>
      <ProductForm initial={EMPTY_PRODUCT} onSubmit={handleSubmit} onCancel={() => navigate('/admin/products')} />
    </AdminPage>
  )
}
