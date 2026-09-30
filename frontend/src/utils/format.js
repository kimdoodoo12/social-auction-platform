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

// "2026-09-16T14:20:08" → "09-16 14:20" (Figma 경매 목록 표기)
export function formatShortDateTime(value) {
  if (!value) return '—'
  return String(value).replace('T', ' ').slice(5, 16)
}

// "010-2345-6789" → "010-****-6789" (Figma 연락처 표기)
export function maskPhone(value) {
  if (!value) return '-'
  const parts = String(value).split('-')
  if (parts.length !== 3) return value
  return `${parts[0]}-${'*'.repeat(parts[1].length)}-${parts[2]}`
}
