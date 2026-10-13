import { useState } from 'react'
import { Link } from 'react-router-dom'
import { resolveImageUrl } from '../../api/product'
import { formatPrice, formatNumber } from '../../utils/format'
import { remainingClock } from './catalogHelpers'
import { useNow } from './useNow'

export function ProductImage({ image, alt = '', className = '' }) {
  const [failed, setFailed] = useState(null)
  const src = resolveImageUrl(image)
  return src && src !== failed
    ? <img className={className} src={src} alt={alt} loading="lazy" onError={() => setFailed(src)} />
    : (
      <span className={`shop-no-image ${className}`} role="img" aria-label={`${alt} 이미지 없음`}>
        <svg width="40" height="32" viewBox="0 0 40 32" aria-hidden="true">
          <rect x="1" y="1" width="38" height="30" rx="3" fill="none" stroke="#cfcbc3" strokeWidth="2" />
          <circle cx="12" cy="11" r="3" fill="#cfcbc3" />
          <path d="M8 26l9-10 6 7 4-4 6 7z" fill="#cfcbc3" />
        </svg>
      </span>
    )
}

// 백엔드 경매 상태(대기/진행/완료) → Figma 배지
const BADGE = {
  대기: { label: '경매 대기', tone: 'waiting' },
  진행: { label: '경매 진행 중', tone: 'live' },
  완료: { label: '경매 종료', tone: 'ended' },
}

// Figma 00 Foundation · Card / Product (282×343)
export function ProductCard({ product, now }) {
  const status = product.auctionStatus
  const badge = BADGE[status] ?? { label: status || '상태 확인 중', tone: 'waiting' }
  const waiting = status === '대기'
  const ended = status === '완료'
  let meta
  if (waiting) meta = <span className="muted">첫 입찰을 기다리는 중</span>
  else if (status === '진행') meta = <span className="accent">남은 시간 {remainingClock(product.endTime, now)}</span>
  else meta = <span className="muted">경매 종료됨</span>
  const priceLabel = waiting ? '시작가' : ended ? '낙찰가' : '현재가'

  return (
    <article className={`shop-product${ended ? ' is-ended' : ''}`}>
      <Link to={`/user/auction/${product.auctionId}`} className="shop-product-link">
        <div className="shop-product-image">
          <ProductImage image={product.image} alt={product.productName} />
          <span className={`shop-badge ${badge.tone}`}>{badge.label}</span>
        </div>
        <div className="shop-product-copy">
          <p className="shop-maker">◦ {product.organizationName}</p>
          <h3>{product.productName}</h3>
          <div className="shop-price">
            <small>{priceLabel}</small>
            <strong>{formatPrice(product.currentPrice)}</strong>
          </div>
          <div className="shop-product-bottom">
            {meta}
            <span className="muted">입찰 {formatNumber(product.bidCount ?? 0)}</span>
          </div>
        </div>
      </Link>
      {/* 찜하기: 백엔드 API가 없어 비활성 */}
      <button type="button" className="shop-like" disabled title="준비 중인 기능입니다." aria-label="찜하기 (준비 중)" />
    </article>
  )
}

export function ProductGrid({ products }) {
  const now = useNow()
  return (
    <div className="shop-grid">
      {products.map((product) => (
        <ProductCard key={product.auctionId ?? product.productId} product={product} now={now} />
      ))}
    </div>
  )
}

export function CatalogState({ loading, error, empty, onRetry, children }) {
  if (loading) return <div className="shop-state" role="status">상품을 불러오고 있어요…</div>
  if (error) return <div className="shop-state" role="alert"><strong>정보를 불러오지 못했어요.</strong><p>잠시 후 다시 시도해 주세요.</p><button className="shop-button secondary" onClick={onRetry}>다시 시도</button></div>
  if (empty) return <div className="shop-state"><strong>아직 표시할 상품이 없어요.</strong><p>다른 카테고리나 검색 조건을 살펴보세요.</p></div>
  return children
}
