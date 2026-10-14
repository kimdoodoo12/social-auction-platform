import { api, toPage } from '../../api/client'
import { sortProducts } from './catalogHelpers'

const root = '/ieum/user/product'

// 백엔드 목록 API는 카테고리·경매 상태를 하나씩만 받는다.
// 여러 개를 고르면 조합마다 한 번씩 받아 합친 뒤 화면에서 정렬·페이지 나누기를 한다.
const MERGE_FETCH_SIZE = 1000

async function list(params) {
  const { categoryIds = [], auctionStatuses = [], page = 0, size = 9, ...rest } = params
  const base = {
    productName: rest.productName,
    organizationId: rest.organizationId,
    lowprice: rest.lowprice,
    highprice: rest.highprice,
    sortType: rest.sortType,
  }
  if (categoryIds.length <= 1 && auctionStatuses.length <= 1) {
    return toPage(
      await api.get(`${root}/list`, { ...base, categoryId: categoryIds[0], auctionStatus: auctionStatuses[0], page, size }),
    )
  }
  const cats = categoryIds.length ? categoryIds : [undefined]
  const stats = auctionStatuses.length ? auctionStatuses : [undefined]
  const pages = await Promise.all(
    cats.flatMap((categoryId) =>
      stats.map((auctionStatus) =>
        api.get(`${root}/list`, { ...base, categoryId, auctionStatus, page: 0, size: MERGE_FETCH_SIZE }),
      ),
    ),
  )
  const seen = new Set()
  const merged = []
  pages.forEach((p) =>
    toPage(p).content.forEach((item) => {
      const key = item.auctionId ?? item.productId
      if (seen.has(key)) return
      seen.add(key)
      merged.push(item)
    }),
  )
  const sorted = sortProducts(merged, base.sortType)
  const totalPages = Math.ceil(sorted.length / size)
  return {
    content: sorted.slice(page * size, page * size + size),
    page,
    size,
    totalElements: sorted.length,
    totalPages,
  }
}

// GET /organization → { organizationCount, organzationList: [{ organizationId, organizationName, organizationImageFileName }] }
// (백엔드 필드명 철자 그대로. organizationList로 바르게 고쳐져도 동작)
async function organizations() {
  const data = await api.get(`${root}/organization`)
  const list = (data?.organizationList ?? data?.organzationList ?? []).map((o) => ({
    organizationId: o.organizationId,
    name: o.organizationName,
    image: o.organizationImageFileName,
  }))
  return { count: data?.organizationCount ?? null, list }
}

export const catalogApi = {
  categories: () => api.get(`${root}/categories`),
  organizations,
  section: (type) => api.get(`${root}/${type}`),
  list,
}
