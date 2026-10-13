import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { resolveImageUrl } from '../../api/product'
import { formatPrice, formatNumber } from '../../utils/format'
import { remainingLabel } from './catalogHelpers'

export function ProductImage({ image, alt = '', className = '' }) {
  const [failed, setFailed] = useState(null)
  const src = resolveImageUrl(image)
  return src && src !== failed
    ? <img className={className} src={src} alt={alt} loading="lazy" onError={() => setFailed(src)} />
    : <span className={`shop-no-image ${className}`} role="img" aria-label={`${alt} 이미지 없음`}>이미지 없음</span>
}

export function ProductGrid({ products }) {
  const [now, setNow] = useState(() => Date.now())
  useEffect(() => { const timer = setInterval(() => setNow(Date.now()), 30000); return () => clearInterval(timer) }, [])
  return <div className="shop-grid">{products.map((product) => <article className="shop-product" key={product.auctionId}>
    <Link to={`/user/auction/${product.auctionId}`} className="shop-product-link">
      <div className="shop-product-image"><ProductImage image={product.image} alt={product.productName} /><span className={`shop-badge ${product.auctionStatus === '진행' ? 'live' : ''}`}>{product.auctionStatus === '대기' ? '첫 입찰' : product.auctionStatus}</span></div>
      <div className="shop-product-copy"><p className="shop-maker">{product.organizationName}</p><h3>{product.productName}</h3><p className="shop-price"><small>{product.auctionStatus === '대기' ? '시작가' : '현재가'}</small> {formatPrice(product.currentPrice)}</p><div className="shop-product-bottom"><span>{remainingLabel(product.endTime, product.auctionStatus, now)}</span><span>입찰 {formatNumber(product.bidCount ?? 0)}회</span></div></div>
    </Link>
  </article>)}</div>
}

export function CatalogState({ loading, error, empty, onRetry, children }) {
  if (loading) return <div className="shop-state" role="status">상품을 불러오고 있어요…</div>
  if (error) return <div className="shop-state" role="alert"><strong>정보를 불러오지 못했어요.</strong><p>잠시 후 다시 시도해 주세요.</p><button className="shop-button secondary" onClick={onRetry}>다시 시도</button></div>
  if (empty) return <div className="shop-state"><strong>아직 표시할 상품이 없어요.</strong><p>다른 카테고리나 검색 조건을 살펴보세요.</p></div>
  return children
}
