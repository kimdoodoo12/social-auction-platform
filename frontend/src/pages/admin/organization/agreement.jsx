import { Badge } from '../../../components/admin/ui'

// 협약 상태(Boolean agreementStatus) 표시
export function AgreementBadge({ status }) {
  if (status === true) return <Badge tone="soft">협약</Badge>
  if (status === false) return <Badge tone="muted">미협약</Badge>
  return '-'
}
