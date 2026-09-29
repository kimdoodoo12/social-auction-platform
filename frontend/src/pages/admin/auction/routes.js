import AuctionListPage from './AuctionListPage'
import AuctionDetailPage from './AuctionDetailPage'

// /admin 아래 상대 경로. 이 도메인 담당자만 수정한다.
// 화면 컴포넌트는 별도 파일로 만들고 Component에 연결한다(이 파일에는 JSX를 쓰지 않는다).
export default [
  { path: 'auctions', Component: AuctionListPage, handle: { title: '경매 관리' } },
  { path: 'auctions/:auctionId', Component: AuctionDetailPage, handle: { title: '경매 상세' } },
]
