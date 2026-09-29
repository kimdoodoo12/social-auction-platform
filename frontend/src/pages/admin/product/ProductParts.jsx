import { useState } from 'react'
import { resolveImageUrl, STOPPED_STATUS } from '../../../api/product'
import { Badge } from '../../../components/admin/ui'

// 상품 화면들이 함께 쓰는 작은 표시 컴포넌트

const STATUS_TONE = {
  진행: 'primary',
  완료: 'dark',
  [STOPPED_STATUS]: 'muted',
}

// DB 상태값(대기/진행/완료)을 Figma 표기(경매 대기/경매 진행 중/경매 종료)로 보여준다
const STATUS_LABEL = {
  대기: '경매 대기',
  진행: '경매 진행 중',
  완료: '경매 종료',
}

export function ProductStatusBadge({ status }) {
  if (!status) return '-'
  return <Badge tone={STATUS_TONE[status] ?? 'outline'}>{STATUS_LABEL[status] ?? status}</Badge>
}

// 이미지 경로를 표시한다. 불러오지 못하면 빈 칸으로 둔다.
export function ProductImage({ path, alt = '', className = 'product-thumb' }) {
  const [failedPath, setFailedPath] = useState(null)
  const src = resolveImageUrl(path)
  if (!src || failedPath === path) {
    return <span className={`${className} product-thumb--empty`} aria-label="이미지 없음" />
  }
  return <img className={className} src={src} alt={alt} onError={() => setFailedPath(path)} />
}
