import { api, BASE_URL, toPage } from './client'

// 상품 API (backend products/admin/controller/ProductController.java)
// 상품 API 주소는 백엔드 관리자 상품 컨트롤러 매핑과 맞춘다.
export const PRODUCT_API = {
  list: '/ieum/admin/product/main', // GET  Pageable(size 8, createdAt·productId DESC) → Page<ProductListResponse>
  detail: '/ieum/admin/product/detail', // GET  /{productId} → ProductDto
  stop: '/ieum/admin/product/stop', // POST ?productId → boolean (판매 중지)
  create: '/ieum/admin/product/add', // POST body ProductDto → boolean
  update: '/ieum/admin/product/update', // PUT  body ProductDto → boolean
  search: '/ieum/admin/product/search', // GET  ?productName&organizationName&auctionStatus&categoryName&page → Page<ProductManageDto>
}

// 폼/필터 선택지용 (읽기 전용)
const CATEGORY_API = '/ieum/admin/category'
const ORGANIZATION_API = '/ieum/admin/organization'

// auction.auction_status 에 저장되는 값 (sql/sample.sql, ProductService.stopSelling 기준)
export const AUCTION_STATUS_OPTIONS = ['대기', '진행', '완료', '판매 중지']
// 경매가 없는 상품 (ProductService.findAll 이 경매가 없을 때 내려주는 값)
export const NO_AUCTION_STATUS = '경매 대기'
export const STOPPED_STATUS = '판매 중지'

// 백엔드 제약 (ProductService.saveProduct / updateProduct)
export const PRODUCT_LIMITS = { name: 30, text: 255, images: 5 }

// 목록(main)과 검색(search)의 응답 필드명이 달라 한 가지 형태로 맞춘다.
function fromListResponse(row, index) {
  return {
    key: `${row.productId}-${index}`,
    productId: row.productId,
    image: row.image,
    productName: row.productName,
    organizationName: row.organizationName,
    startPrice: row.startPrice,
    currentPrice: row.currentPrice,
    status: row.status,
    createdAt: row.createdAt,
  }
}

function fromManageDto(row, index) {
  return {
    // 검색 쿼리는 경매 상태별로 group by 해서 같은 상품이 여러 줄 나올 수 있다
    key: `${row.productId}-${index}`,
    productId: row.productId,
    image: row.image,
    productName: row.productName,
    organizationName: row.organizationName,
    startPrice: row.startPrice,
    currentPrice: row.bidPrice,
    // 경매가 없으면 auction_status 가 null 이다. 목록(main)과 같은 표기로 맞춘다.
    status: row.auctionStatus ?? NO_AUCTION_STATUS,
    createdAt: row.createdAt,
  }
}

export async function fetchProducts({ page }) {
  const result = toPage(await api.get(PRODUCT_API.list, { page }))
  return { ...result, content: result.content.map(fromListResponse) }
}

export async function searchProducts({ productName, organizationName, auctionStatus, categoryName, page }) {
  const result = toPage(
    await api.get(PRODUCT_API.search, { productName, organizationName, auctionStatus, categoryName, page }),
  )
  return { ...result, content: result.content.map(fromManageDto) }
}

export function fetchProduct(productId) {
  return api.get(`${PRODUCT_API.detail}/${productId}`)
}

// 성공 시 백엔드는 true 를 준다. false 가 오면 실패로 본다.
function ensureTrue(result, message) {
  if (result !== true) throw new Error(message)
  return result
}

export async function stopSelling(productId) {
  return ensureTrue(await api.post(PRODUCT_API.stop, undefined, { productId }), '판매 중지에 실패했습니다.')
}

function productFormData(productDto) {
  const body = new FormData()
  for (const key of ['productId', 'name', 'organizationId', 'categoryId', 'startPrice', 'description', 'background']) {
    if (key !== 'productId' || productDto[key] != null) body.append(key, productDto[key] ?? '')
  }
  productDto.images.filter((image) => image.file).forEach((image, index) => {
    if (image.imageId != null) body.append(`images[${index}].imageId`, String(image.imageId))
    body.append(`images[${index}].file`, image.file)
    body.append(`images[${index}].sortOrder`, String(image.sortOrder))
  })
  return body
}

export async function createProduct(productDto) {
  return ensureTrue(await api.post(PRODUCT_API.create, productFormData(productDto)), '상품 등록에 실패했습니다.')
}

export async function updateProduct(productDto) {
  return ensureTrue(await api.put(PRODUCT_API.update, productFormData(productDto)), '상품 수정에 실패했습니다.')
}

// GET /ieum/admin/category → List<CategoryResponse{categoryId, name, productCount}>
export async function fetchCategoryOptions() {
  const list = (await api.get(CATEGORY_API)) ?? []
  return list.map((c) => ({ id: c.categoryId, name: c.name }))
}

// GET /ieum/admin/organization → Page<OrganizationListResponse> (기본 size 4라 크게 요청한다)
export async function fetchOrganizationOptions() {
  const { content } = toPage(await api.get(ORGANIZATION_API, { size: 1000 }))
  return content.map((o) => ({ id: o.organizationId, name: o.name }))
}

// 상품 파일명을 조회 URL로 변환한다. 업로드 미리보기와 기존 경로도 지원한다.
export function resolveImageUrl(path) {
  if (!path) return null
  try {
    const imagePath = /^(?:https?:|blob:|data:|\/)/i.test(path)
      ? path
      : `/images/${encodeURIComponent(path)}`
    return new URL(imagePath, BASE_URL).href
  } catch {
    return null
  }
}
