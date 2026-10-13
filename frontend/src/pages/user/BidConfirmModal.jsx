import { useEffect, useRef } from 'react'
import { formatPrice } from '../../utils/format'
import { ProductImage } from './CatalogParts'

// [USER] 05 입찰 확인 모달 (Figma 25:2673)
export default function BidConfirmModal({ title, organizationName, image, currentPrice, currentLabel = '현재 최고 입찰가', amount, busy, onClose, onConfirm }) {
  const cancelRef = useRef(null)

  useEffect(() => {
    cancelRef.current?.focus()
    const onKeyDown = (event) => {
      if (event.key === 'Escape' && !busy) onClose()
    }
    document.addEventListener('keydown', onKeyDown)
    return () => document.removeEventListener('keydown', onKeyDown)
  }, [busy, onClose])

  return (
    <div className="ud-modal-backdrop" onMouseDown={(event) => { if (event.target === event.currentTarget && !busy) onClose() }}>
      <section className="ud-modal" role="dialog" aria-modal="true" aria-labelledby="ud-bid-title">
        <h2 id="ud-bid-title">입찰 확인</h2>
        <div className="ud-modal-item">
          <span className="ud-modal-thumb"><ProductImage image={image} alt="" /></span>
          <div>
            <small>◦ {organizationName}</small>
            <strong>{title}</strong>
          </div>
        </div>
        <dl className="ud-modal-amounts">
          <div><dt>{currentLabel}</dt><dd>{formatPrice(currentPrice)}</dd></div>
          <div className="mine"><dt>내 입찰 금액</dt><dd>{formatPrice(amount)}</dd></div>
        </dl>
        <div className="ud-modal-warning">
          <strong>입찰 후에는 취소할 수 없습니다.</strong>
          <p>경매 종료 시점에 최고 입찰자가 되면 자동으로 낙찰되며, 결제 의무가 발생합니다.</p>
        </div>
        <div className="ud-modal-actions">
          <button type="button" className="shop-button secondary" ref={cancelRef} disabled={busy} onClick={onClose}>취소</button>
          <button type="button" className="shop-button" disabled={busy} onClick={onConfirm}>{busy ? '전송 중…' : '입찰 확정'}</button>
        </div>
        <p className="ud-modal-foot">확정하면 마이페이지 · 입찰 내역에 추가됩니다.</p>
      </section>
    </div>
  )
}
