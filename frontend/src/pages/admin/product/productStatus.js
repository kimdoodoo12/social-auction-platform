// 상세 응답(TotalDto)에는 경매 상태 문자열이 없어 경매 시각으로 계산한다.
// 목록에서 넘어온 상태값이 있으면 화면에서 그것을 우선한다.
export function deriveStatus(auctionInfo) {
  if (!auctionInfo || !auctionInfo.startTime) return '대기'
  if (auctionInfo.endTime && new Date(auctionInfo.endTime) <= new Date()) return '완료'
  return '진행'
}
