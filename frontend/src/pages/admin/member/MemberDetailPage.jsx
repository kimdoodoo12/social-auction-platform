import { useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import AdminPage from '../../../components/admin/AdminPage'
import DataTable from '../../../components/admin/DataTable'
import { AsyncBoundary, Badge, Button, Card, ConfirmModal, DetailList } from '../../../components/admin/ui'
import { fetchMemberDetail, suspendMember } from '../../../api/member'
import { useAsync } from '../../../hooks/useAsync'
import { formatDate, formatDateTime, formatNumber, formatPrice } from '../../../utils/format'
import { SUSPENDED_ROLE, memberRoleTone } from './memberStatus'
import './member.css'

// [ADMIN] 12 회원 상세
// 응답: MemberBidHistoryDto { memberId, loginId, name, email, createdAt, bcount, pcount, role, lockedAt, bidDtos, payDtos }
const byTimeDesc = (key) => (a, b) => String(b[key] ?? '').localeCompare(String(a[key] ?? ''))

export default function MemberDetailPage() {
  const { userId } = useParams()
  const detail = useAsync(() => fetchMemberDetail(userId), [userId])
  const [confirmOpen, setConfirmOpen] = useState(false)
  const [actionError, setActionError] = useState(null)

  const member = detail.data
  const suspended = member?.role === SUSPENDED_ROLE

  const handleSuspend = async () => {
    setConfirmOpen(false)
    setActionError(null)
    try {
      const ok = await suspendMember(member.memberId)
      if (!ok) setActionError('회원을 정지하지 못했습니다.')
    } catch (error) {
      setActionError(error.message)
    }
    detail.reload()
  }

  const bids = [...(member?.bidDtos ?? [])].sort(byTimeDesc('bidTime'))
  const payments = [...(member?.payDtos ?? [])].sort(byTimeDesc('createdAt'))

  const actions = member && (
    <Button variant="danger" size="sm" disabled={suspended} onClick={() => setConfirmOpen(true)}>
      {suspended ? '정지된 회원' : '회원 정지'}
    </Button>
  )

  return (
    <AdminPage title="회원 상세" back="/admin/members" actions={actions}>
      <AsyncBoundary loading={detail.loading && !member} error={member ? null : detail.error}>
        {member && (
          <>
            {actionError && <div className="admin-state admin-state--error">{actionError}</div>}
            <div className="member-detail-grid">
              <div className="member-detail-side">
                <Card title="회원 정보">
                  <DetailList
                    items={[
                      { label: '회원번호', value: member.memberId },
                      { label: '아이디', value: member.loginId },
                      { label: '이름', value: member.name },
                      { label: '이메일', value: member.email },
                      { label: '가입일', value: formatDate(member.createdAt) },
                      { label: '상태', value: <Badge tone={memberRoleTone(member.role)}>{member.role ?? '-'}</Badge> },
                      { label: '잠금 일시', value: formatDateTime(member.lockedAt) },
                    ]}
                  />
                </Card>
                <div className="member-stats">
                  <div className="admin-card member-stat">
                    <span className="member-stat__label">입찰 횟수</span>
                    <span className="member-stat__value">
                      {formatNumber(member.bcount ?? 0)}
                      <small>회</small>
                    </span>
                  </div>
                  <div className="admin-card member-stat">
                    <span className="member-stat__label">낙찰 횟수</span>
                    <span className="member-stat__value highlight">
                      {formatNumber(member.pcount ?? 0)}
                      <small>회</small>
                    </span>
                  </div>
                </div>
              </div>

              <div className="member-detail-main">
                <Card title={`입찰 내역 (${formatNumber(bids.length)}건)`} noBody>
                  <DataTable
                    rowKey="bidId"
                    rows={bids}
                    emptyText="입찰 내역이 없습니다."
                    columns={[
                      { key: 'bidId', header: '입찰번호', width: 100 },
                      { key: 'bidPrice', header: '입찰가', align: 'right', className: 'strong', render: (r) => formatPrice(r.bidPrice) },
                      { key: 'bidTime', header: '입찰 일시', align: 'right', render: (r) => formatDateTime(r.bidTime) },
                    ]}
                  />
                </Card>
                <Card title={`결제 내역 (${formatNumber(payments.length)}건)`} noBody>
                  <DataTable
                    rowKey="paymentId"
                    rows={payments}
                    emptyText="결제 내역이 없습니다."
                    columns={[
                      { key: 'paymentId', header: '결제번호', width: 100 },
                      {
                        key: 'auctionId',
                        header: '경매번호',
                        render: (r) => (r.auctionId ? <Link to={`/admin/auctions/${r.auctionId}`}>{r.auctionId}</Link> : '-'),
                      },
                      { key: 'paymentPrice', header: '결제 금액', align: 'right', className: 'strong', render: (r) => formatPrice(r.paymentPrice) },
                      {
                        key: 'paymentStatus',
                        header: '결제 상태',
                        render: (r) => (
                          <Badge tone={r.paymentStatus ? 'soft' : 'outline'}>{r.paymentStatus ? '결제 완료' : '결제 대기'}</Badge>
                        ),
                      },
                      { key: 'createdAt', header: '등록 일시', align: 'right', render: (r) => formatDateTime(r.createdAt) },
                    ]}
                  />
                </Card>
              </div>
            </div>
          </>
        )}
      </AsyncBoundary>

      <ConfirmModal
        open={confirmOpen}
        title="회원 정지"
        message={member ? `${member.name}(${member.loginId}) 회원을 정지하시겠습니까?` : ''}
        confirmText="정지"
        danger
        onConfirm={handleSuspend}
        onCancel={() => setConfirmOpen(false)}
      />
    </AdminPage>
  )
}
