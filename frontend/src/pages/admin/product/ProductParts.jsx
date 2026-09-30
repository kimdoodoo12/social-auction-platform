import ImageBox from '../../../components/admin/ImageBox'
import { Badge } from '../../../components/admin/ui'

// 상품 화면들이 함께 쓰는 작은 표시 컴포넌트

// DB 상태값(auction.auction_status) → Figma 표기와 배지 색
const STATUS = {
  대기: { label: '경매 대기', tone: 'outline' },
  진행: { label: '경매 진행 중', tone: 'primary' },
  완료: { label: '경매 종료', tone: 'dark' },
  '판매 중지': { label: '판매 중지', tone: 'muted' },
}

export function ProductStatusBadge({ status }) {
  if (!status) return '-'
  const s = STATUS[status] ?? { label: status, tone: 'outline' }
  return <Badge tone={s.tone}>{s.label}</Badge>
}


// 목록 썸네일 (Figma 34px 정사각형)
export function ProductThumb({ path, alt }) {
  return <ImageBox path={path} alt={alt} className="product-thumb" label="" />
}
