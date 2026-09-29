// 표시용 포맷 함수

// "2026-09-15T12:00:00" / "2026-09-15" → "2026-09-15"
export function formatDate(value) {
  if (!value) return '-'
  return String(value).slice(0, 10)
}

// "2026-09-15T12:34:56" → "2026-09-15 12:34"
export function formatDateTime(value) {
  if (!value) return '-'
  return String(value).replace('T', ' ').slice(0, 16)
}

export function formatNumber(value) {
  if (value === null || value === undefined || value === '') return '-'
  const n = Number(value)
  return Number.isNaN(n) ? String(value) : n.toLocaleString('ko-KR')
}

export function formatPrice(value) {
  const n = formatNumber(value)
  return n === '-' ? n : `${n}원`
}
