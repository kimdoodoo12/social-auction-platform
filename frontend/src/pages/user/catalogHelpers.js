export const SORT_OPTIONS = [
  ['popular', '인기순'], ['new', '최근 등록순'], ['time', '마감 임박순'],
  ['lowprice', '낮은 가격순'], ['highprice', '높은 가격순'],
]

export function catalogParams(search) {
  const params = new URLSearchParams(search)
  const integer = (key, fallback = '') => {
    const value = params.get(key)
    return value !== null && /^\d+$/.test(value) && Number(value) <= 2147483647 ? Number(value) : fallback
  }
  return {
    productName: params.get('productName') || '',
    categoryId: integer('categoryId'),
    auctionStatus: ['대기', '진행', '완료'].includes(params.get('auctionStatus')) ? params.get('auctionStatus') : '',
    lowprice: integer('lowprice'), highprice: integer('highprice'),
    sortType: SORT_OPTIONS.some(([key]) => key === params.get('sortType')) ? params.get('sortType') : 'popular',
    page: integer('page', 0), size: 9,
  }
}

export function remainingLabel(endTime, status, now) {
  if (status === '대기') return '첫 입찰을 기다리고 있어요'
  if (status !== '진행') return status === '완료' ? '경매 종료' : status || '상태 확인 중'
  if (!endTime) return '진행 중'
  const time = new Date(endTime).getTime()
  if (!Number.isFinite(time)) return '진행 중'
  const minutes = Math.ceil((time - now) / 60000)
  if (minutes <= 0) return '마감 시간 도달'
  if (minutes < 60) return `${minutes}분 남음`
  if (minutes < 1440) return `${Math.floor(minutes / 60)}시간 ${minutes % 60}분 남음`
  return `${Math.floor(minutes / 1440)}일 ${Math.floor(minutes % 1440 / 60)}시간 남음`
}
