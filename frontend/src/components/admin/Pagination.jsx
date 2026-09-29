// 페이지네이션. page는 Spring과 같은 0부터 시작하는 번호다. 화면에는 1부터 표시한다.
export default function Pagination({ page, totalPages, onChange, windowSize = 5 }) {
  if (!totalPages || totalPages < 1) return null

  const start = Math.max(0, Math.min(page - Math.floor(windowSize / 2), totalPages - windowSize))
  const end = Math.min(totalPages, start + windowSize)
  const pages = []
  for (let p = start; p < end; p += 1) pages.push(p)

  return (
    <nav className="admin-pagination" aria-label="페이지">
      <button type="button" className="admin-pagination__btn" disabled={page <= 0} onClick={() => onChange(page - 1)}>
        ‹
      </button>
      {pages.map((p) => (
        <button
          key={p}
          type="button"
          className={`admin-pagination__btn${p === page ? ' active' : ''}`}
          onClick={() => onChange(p)}
        >
          {p + 1}
        </button>
      ))}
      <button
        type="button"
        className="admin-pagination__btn"
        disabled={page >= totalPages - 1}
        onClick={() => onChange(page + 1)}
      >
        ›
      </button>
      <span className="admin-pagination__total">총 {totalPages.toLocaleString()} 페이지</span>
    </nav>
  )
}
