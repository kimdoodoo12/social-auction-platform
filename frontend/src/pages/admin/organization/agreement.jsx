import { Badge } from '../../../components/admin/ui'

// 협약 상태(Boolean agreementStatus) 표시 — true: 협약 중, false: 협약 종료 (사용자 결정)
export function AgreementBadge({ status }) {
  if (status === true) return <Badge tone="soft">협약 중</Badge>
  if (status === false) return <Badge tone="muted">협약 종료</Badge>
  return '-'
}
