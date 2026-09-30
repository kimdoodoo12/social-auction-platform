import { Link } from 'react-router-dom'

// 관리자 화면 공통 UI 컴포넌트 (Figma 00 Foundation 기준)

// 백엔드 API가 없는 동작 버튼에 붙이는 안내
export const PENDING_TITLE = '준비 중인 기능입니다.'

// subtitle: 제목 아래 작은 설명 (Figma 카드 헤더의 회색 보조 문구)
export function Card({
  title,
  subtitle,
  actions,
  children,
  bodyClassName = 'admin-card__body',
  noBody = false,
  className = '',
}) {
  return (
    <section className={`admin-card ${className}`.trim()}>
      {(title || actions) && (
        <div className="admin-card__header">
          <div className="admin-card__titles">
            <span>{title}</span>
            {subtitle && <span className="admin-card__subtitle">{subtitle}</span>}
          </div>
          {actions && <div className="admin-actions">{actions}</div>}
        </div>
      )}
      {noBody ? children : <div className={bodyClassName}>{children}</div>}
    </section>
  )
}

// variant: primary | dark | secondary | danger,  size: md | sm | lg
// pending: API가 없는 기능 — 비활성화하고 "준비 중" 안내를 띄운다
export function Button({ variant = 'secondary', size = 'md', className = '', type = 'button', pending, ...props }) {
  const cls = ['admin-btn', `admin-btn--${variant}`, size !== 'md' ? `admin-btn--${size}` : '', className]
    .filter(Boolean)
    .join(' ')
  const pendingProps = pending ? { disabled: true, title: PENDING_TITLE } : {}
  return <button type={type} className={cls} {...props} {...pendingProps} />
}

// 표 안의 텍스트형 버튼 (상세 · 정지 등)
export function LinkButton({ tone, className = '', pending, ...props }) {
  const cls = ['admin-link-btn', tone ? `admin-link-btn--${tone}` : '', className].filter(Boolean).join(' ')
  const pendingProps = pending ? { disabled: true, title: PENDING_TITLE } : {}
  return <button type="button" className={cls} {...props} {...pendingProps} />
}

// "← 상품 관리로 돌아가기"
export function BackLink({ to, children }) {
  return (
    <Link to={to} className="admin-back-link">
      ← {children}
    </Link>
  )
}

// 라벨은 왼쪽, 값은 오른쪽에 두는 정보 행 목록 (Figma 상세 카드). items: [{ label, value, className }]
export function InfoRows({ items }) {
  return (
    <div className="admin-info-rows">
      {items.map((item) => (
        <div key={item.label} className="admin-info-row">
          <span className="admin-info-row__label">{item.label}</span>
          <span className={`admin-info-row__value ${item.className ?? ''}`.trim()}>{item.value ?? '-'}</span>
        </div>
      ))}
    </div>
  )
}

// 폼 하단 액션 바: 왼쪽 안내 문구(또는 에러) + 오른쪽 버튼들
export function FormActionBar({ note, error, children }) {
  return (
    <section className="admin-card admin-action-bar">
      <div className="admin-action-bar__text">
        {error ? <span className="admin-action-bar__error">{error}</span> : <span>{note}</span>}
      </div>
      <div className="admin-actions">{children}</div>
    </section>
  )
}

// 번호가 매겨진 단계 안내 (Figma "등록 후 흐름")
export function StepList({ steps }) {
  return (
    <ol className="admin-steps">
      {steps.map((step, i) => (
        <li key={step}>
          <span className="admin-steps__num">{i + 1}</span>
          {step}
        </li>
      ))}
    </ol>
  )
}

// 강조 안내 박스 (Figma 주황 배경 안내)
export function Callout({ title, children }) {
  return (
    <div className="admin-callout">
      {title && <strong>{title}</strong>}
      <p>{children}</p>
    </div>
  )
}

// 요약 수치 카드. highlight: 주황 테두리/글자 (Figma 강조 카드)
export function StatCard({ label, value, unit, highlight }) {
  return (
    <div className={`admin-card admin-stat${highlight ? ' admin-stat--highlight' : ''}`}>
      <span className="admin-stat__label">{label}</span>
      <span className="admin-stat__value">
        {value ?? '-'}
        {unit && value != null && <small>{unit}</small>}
      </span>
    </div>
  )
}

// tone: primary(경매 진행 중) | soft(정상) | dark(정지/종료) | muted | outline(경매 대기)
export function Badge({ tone = 'muted', children }) {
  return <span className={`admin-badge admin-badge--${tone}`}>{children}</span>
}

// 검색 영역: submit 시 onSearch 호출
export function FilterBar({ onSearch, children }) {
  const handleSubmit = (e) => {
    e.preventDefault()
    onSearch?.()
  }
  return (
    <section className="admin-card">
      <form className="admin-filter-bar" onSubmit={handleSubmit}>
        {children}
      </form>
    </section>
  )
}

export function Field({ label, required, hint, className = '', children }) {
  return (
    <label className={`admin-field ${className}`}>
      {label && (
        <span className="admin-field__label">
          {label}
          {required && <span className="required">*</span>}
        </span>
      )}
      {children}
      {hint && <span className="admin-field__hint">{hint}</span>}
    </label>
  )
}

// "전체 2,840명 · 정상 2,806 · 정지 34" 형태의 요약 줄
export function Summary({ label, children }) {
  return (
    <div className="admin-summary">
      <strong>{label}</strong>
      {children}
    </div>
  )
}

// 로딩/에러 표시. 둘 다 아니면 children을 그린다.
export function AsyncBoundary({ loading, error, children }) {
  if (loading) return <div className="admin-state">불러오는 중...</div>
  if (error) return <div className="admin-state admin-state--error">{error.message || '요청에 실패했습니다.'}</div>
  return children
}

// 백엔드 API가 없어 mock 데이터를 보여줄 때 표시
export function MockNotice({ children }) {
  return <div className="admin-mock-notice">{children}</div>
}

// key/value 상세 목록. items: [{ label, value }]
export function DetailList({ items }) {
  return (
    <dl className="admin-dl">
      {items.map((item) => (
        <div key={item.label} style={{ display: 'contents' }}>
          <dt>{item.label}</dt>
          <dd>{item.value ?? '-'}</dd>
        </div>
      ))}
    </dl>
  )
}

export function ConfirmModal({ open, title, message, confirmText = '확인', danger, onConfirm, onCancel }) {
  if (!open) return null
  return (
    <div className="admin-modal-dim" onClick={onCancel}>
      <div className="admin-modal" role="dialog" aria-modal="true" onClick={(e) => e.stopPropagation()}>
        <div className="admin-modal__title">{title}</div>
        {message && <p>{message}</p>}
        <div className="admin-actions admin-actions--end">
          <Button onClick={onCancel}>취소</Button>
          <Button variant={danger ? 'dark' : 'primary'} onClick={onConfirm}>
            {confirmText}
          </Button>
        </div>
      </div>
    </div>
  )
}
