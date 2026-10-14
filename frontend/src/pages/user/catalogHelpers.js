// [USER] 02 상품 목록 정렬 탭 (Figma: 인기순 · 마감 임박순 · 최신순 · 가격 낮은순 → 사용자 결정으로 가격 높은순)
export const SORT_OPTIONS = [
  ['popular', '인기순'],
  ['time', '마감 임박순'],
  ['new', '최신순'],
  ['highprice', '가격 높은순'],
]

export const STATUS_OPTIONS = [
  ['대기', '경매 대기'],
  ['진행', '경매 진행 중'],
  ['완료', '경매 종료'],
]

const MAX_INT = 2147483647
const isInt = (v) => v !== null && /^\d+$/.test(v) && Number(v) <= MAX_INT

// URL 쿼리 → 목록 조건. 카테고리·상태는 여러 개 선택할 수 있어 같은 키를 반복해서 쓴다.
export function catalogParams(search) {
  const params = new URLSearchParams(search)
  const integer = (key, fallback = '') => (isInt(params.get(key)) ? Number(params.get(key)) : fallback)
  const sortType = params.get('sortType')
  return {
    productName: params.get('productName') || '',
    organizationId: integer('organizationId'), // 상품 상세의 '기관이 등록한 다른 상품 보기'
    categoryIds: [...new Set(params.getAll('categoryId').filter(isInt).map(Number))],
    auctionStatuses: [...new Set(params.getAll('auctionStatus').filter((s) => STATUS_OPTIONS.some(([v]) => v === s)))],
    lowprice: integer('lowprice'),
    highprice: integer('highprice'),
    sortType: SORT_OPTIONS.some(([key]) => key === sortType) ? sortType : 'popular',
    page: integer('page', 0),
    size: 9,
  }
}

// 여러 결과를 합쳤을 때 화면에서 정렬한다. 백엔드 ProductRepository.findProductList의 ORDER BY와 같은 기준:
// - 마감 임박순: 진행 중 먼저 → 종료 시각 오름차순 (MySQL처럼 값이 없으면 앞)
// - 최신순: 응답에 등록일이 없어 상품번호가 큰 순서(등록 순)로 대신한다
// - 값이 같으면 상품번호 내림차순
const endMs = (p) => (p.endTime ? new Date(p.endTime).getTime() : -Infinity)
const SORTERS = {
  popular: (a, b) => (b.bidCount ?? 0) - (a.bidCount ?? 0),
  time: (a, b) => (a.auctionStatus === '진행' ? 0 : 1) - (b.auctionStatus === '진행' ? 0 : 1) || endMs(a) - endMs(b),
  new: (a, b) => (b.productId ?? 0) - (a.productId ?? 0),
  highprice: (a, b) => (b.currentPrice ?? 0) - (a.currentPrice ?? 0),
}

export function sortProducts(products, sortType) {
  const primary = SORTERS[sortType] ?? SORTERS.popular
  return [...products].sort((a, b) => primary(a, b) || (b.productId ?? 0) - (a.productId ?? 0))
}

// Figma 카드 표기: 종료까지 남은 시간 "HH:MM:SS" (24시간을 넘으면 시간이 그대로 커진다. 예: 30:15:04)
export function remainingClock(endTime, now) {
  const time = endTime ? new Date(endTime).getTime() : NaN
  if (!Number.isFinite(time)) return '--:--:--'
  const total = Math.max(0, Math.floor((time - now) / 1000))
  const pad = (n) => String(n).padStart(2, '0')
  return `${pad(Math.floor(total / 3600))}:${pad(Math.floor((total % 3600) / 60))}:${pad(total % 60)}`
}

// 배열에서 무작위로 count개를 뽑는다 (Fisher–Yates, 원본은 그대로 둔다)
export function pickRandom(items, count) {
  const copy = [...items]
  for (let i = copy.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1))
    ;[copy[i], copy[j]] = [copy[j], copy[i]]
  }
  return copy.slice(0, count)
}
