// 관리자 화면 공통 UI 컴포넌트 (Figma 00 Foundation 기준)

export function Card({ title, actions, children, bodyClassName = 'admin-card__body', noBody = false }) {
  return (
    <section className="admin-card">
      {(title || actions) && (
        <div className="admin-card__header">
          <span>{title}</span>
          {actions && <div className="admin-actions">{actions}</div>}
        </div>
      )}
      {noBody ? children : <div className={bodyClassName}>{children}</div>}
    </section>
  )
}

// variant: primary | dark | secondary | danger,  size: md | sm
export function Button({ variant = 'secondary', size = 'md', className = '', type = 'button', ...props }) {
  const cls = ['admin-btn', `admin-btn--${variant}`, size === 'sm' ? 'admin-btn--sm' : '', className]
    .filter(Boolean)
    .join(' ')
  return <button type={type} className={cls} {...props} />
}

// 표 안의 텍스트형 버튼 (상세 · 정지 등)
export function LinkButton({ tone, className = '', ...props }) {
  const cls = ['admin-link-btn', tone ? `admin-link-btn--${tone}` : '', className].filter(Boolean).join(' ')
  return <button type="button" className={cls} {...props} />
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
