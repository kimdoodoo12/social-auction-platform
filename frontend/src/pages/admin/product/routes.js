import ProductListPage from './ProductListPage'
import ProductCreatePage from './ProductCreatePage'
import ProductDetailPage from './ProductDetailPage'
import ProductEditPage from './ProductEditPage'

// /admin 아래 상대 경로. 이 도메인 담당자만 수정한다.
// 화면 컴포넌트는 별도 파일로 만들고 Component에 연결한다(이 파일에는 JSX를 쓰지 않는다).
export default [
  { path: 'products', Component: ProductListPage, handle: { title: '상품 관리' } },
  { path: 'products/new', Component: ProductCreatePage, handle: { title: '상품 등록' } },
  { path: 'products/:productId', Component: ProductDetailPage, handle: { title: '상품 상세' } },
  { path: 'products/:productId/edit', Component: ProductEditPage, handle: { title: '상품 수정' } },
]
