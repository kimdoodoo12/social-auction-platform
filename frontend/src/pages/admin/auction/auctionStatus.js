// 경매 상태 표시.
// AuctionEntity.auctionStatus(대기/진행/완료)는 응답 DTO(AuctionFindAllDto)에 없어서
// startTime/endTime으로 계산한다. 대기 경매는 시작/종료 시각이 NULL이다(sample.sql 주석 기준).
export function getAuctionStatus(auction, now) {
  if (!auction?.startTime) return { label: '경매 대기', tone: 'outline', ended: false }
  if (auction.endTime && new Date(auction.endTime).getTime() <= now) {
    return { label: '경매 종료', tone: 'dark', ended: true }
  }
  return { label: '경매 진행 중', tone: 'primary', ended: false }
}
