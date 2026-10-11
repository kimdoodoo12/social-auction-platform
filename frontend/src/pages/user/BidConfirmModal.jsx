import { useEffect, useRef } from 'react'
import { formatPrice } from '../../utils/format'

export default function BidConfirmModal({ title, currentPrice, amount, busy, onClose, onConfirm }) {
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
    <div
      className="ua-auction-modal-backdrop"
      onMouseDown={(event) => {
        if (event.target === event.currentTarget && !busy) onClose()
      }}
    >
      <section className="ua-auction-modal" role="dialog" aria-modal="true" aria-labelledby="ua-bid-title">
        <h2 id="ua-bid-title">입찰 확인</h2>
        <div className="ua-auction-modal-product">
          <span className="ua-auction-modal-thumb" aria-hidden="true">▧</span>
          <strong>{title}</strong>
        </div>
        <dl className="ua-auction-modal-prices">
          <div><dt>현재 최고 입찰가</dt><dd>{formatPrice(currentPrice)}</dd></div>
          <div className="ua-auction-modal-amount"><dt>내 입찰 금액</dt><dd>{formatPrice(amount)}</dd></div>
        </dl>
        <div className="ua-auction-modal-warning">
          <strong>입찰 후에는 취소할 수 없습니다.</strong>
          <p>경매 종료 시점에 최고 입찰자가 되면 낙찰 및 결제 의무가 발생합니다.</p>
        </div>
        <div className="ua-auction-modal-actions">
          <button type="button" ref={cancelRef} disabled={busy} onClick={onClose}>취소</button>
          <button type="button" className="primary" disabled={busy} onClick={onConfirm}>
            {busy ? '전송 중…' : '입찰 확정'}
          </button>
        </div>
      </section>
    </div>
  )
}
