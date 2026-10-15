// [USER] 03·04 상품 상세 / 05 입찰 확인 모달 표시용 함수

// * 첫 입찰은 시작가 이상, 진행 중에는 현재 최고가보다 1,000원 이상 높아야 한다.
export const BID_UNIT = 1000

// Figma 문구. 백엔드 firstBid는 아직 임시로 3분이다("나중에 시간 바꾸기" 주석).
export const AUCTION_DURATION_LABEL = '24시간'

export const STATUS_BADGE = {
  대기: { label: '경매 대기', tone: 'waiting' },
  진행: { label: '경매 진행 중', tone: 'live' },
  완료: { label: '경매 종료', tone: 'ended' },
}

// "김서윤" → "김**"
export function maskedName(name) {
  if (!name) return '회원'
  return `${name.slice(0, 1)}${'*'.repeat(Math.max(2, name.length - 1))}`
}

// 입찰 시각 → "방금 전" / "N분 전" / "N시간 전" / "MM-DD HH:MM"
export function relativeTime(value, now) {
  const time = value ? new Date(value).getTime() : NaN
  if (!Number.isFinite(time)) return '-'
  const seconds = Math.max(0, Math.floor((now - time) / 1000))
  if (seconds < 60) return '방금 전'
  if (seconds < 3600) return `${Math.floor(seconds / 60)}분 전`
  if (seconds < 86400) return `${Math.floor(seconds / 3600)}시간 전`
  return String(value).replace('T', ' ').slice(5, 16)
}
