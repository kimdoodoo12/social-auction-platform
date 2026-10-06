import { useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import AdminPage from '../../../components/admin/AdminPage'
import DataTable from '../../../components/admin/DataTable'
import {
  AsyncBoundary,
  BackLink,
  Badge,
  Button,
  Card,
  ConfirmModal,
  InfoRows,
  StatCard,
} from '../../../components/admin/ui'
import { fetchMemberDetail, normalizeMember, suspendMember } from '../../../api/member'
import { useAsync } from '../../../hooks/useAsync'
import { formatDate, formatDateTime, formatNumber, formatPrice } from '../../../utils/format'
import { SUSPENDED_ROLE, memberRoleTone } from './memberStatus'
import './member.css'

// [ADMIN] 12 회원 상세 (Figma 75:387) — GET /user/detail/info/{userid}
// 응답: MemberBidHistoryDto { memberId, loginId, name, email, createdAt, bcount, pcount, role, lockedAt, notpay, bidHistory, payDtos }
// 백엔드에 없는 값(관심 상품, 정지 이력, 연락처, 최근 로그인, 마케팅 수신)은
// Figma 틀을 유지하고 '-'로 표시한다(사용자 결정).
const RECENT_BIDS = 5
const byTimeDesc = (key) => (a, b) => String(b[key] ?? '').localeCompare(String(a[key] ?? ''))

export default function MemberDetailPage() {
  const { userId } = useParams()
  const navigate = useNavigate()
  const detail = useAsync(() => fetchMemberDetail(userId), [userId])
  const [confirmOpen, setConfirmOpen] = useState(false)
  const [actionError, setActionError] = useState(null)

  const member = detail.data
  const suspended = member?.role === SUSPENDED_ROLE

  // 정지 ↔ 정상 복구
  const handleStatusChange = async () => {
    setConfirmOpen(false)
    setActionError(null)
    try {
      const ok = suspended ? await normalizeMember(member.memberId) : await suspendMember(member.memberId)
      if (!ok) setActionError(suspended ? '회원을 정지 해제하지 못했습니다.' : '회원을 정지하지 못했습니다.')
    } catch (error) {
      setActionError(error.message)
    }
    detail.reload()
  }

  const allBids = [...(member?.bidHistory ?? [])].sort(byTimeDesc('bidTime'))
  const bids = allBids.slice(0, RECENT_BIDS)
  const payments = [...(member?.payDtos ?? [])].sort(byTimeDesc('winningDate'))
  const statusLabel = suspended ? '정지 해제' : '계정 정지'

  return (
    <AdminPage title={`회원 상세 · ${userId}`}>
      <BackLink to="/admin/members">회원 관리로 돌아가기</BackLink>

      <AsyncBoundary loading={detail.loading && !member} error={member ? null : detail.error}>
        {member && (
          <>
            {actionError && <div className="admin-state admin-state--error">{actionError}</div>}

            <section className="admin-card admin-hero">
              <span className="member-avatar" aria-hidden="true" />
              <div className="admin-hero__body">
                <div className="admin-hero__meta">{member.memberId}</div>
                <div className="member-hero-title">
                  <span className="admin-hero__title">{member.name}</span>
                  <Badge tone={memberRoleTone(member.role)}>{member.role ?? '-'}</Badge>
                </div>
                <div className="admin-hero__sub">
                  {member.email} · - · 가입 {formatDate(member.createdAt)}
                </div>
              </div>
              <div className="admin-actions">
                <Button size="lg" pending>
                  비밀번호 초기화 안내
                </Button>
                <Button size="lg" onClick={() => setConfirmOpen(true)}>
                  {statusLabel}
                </Button>
              </div>
            </section>

            <div className="member-stats">
              <StatCard label="총 입찰" value={formatNumber(member.bcount ?? 0)} unit="회" />
              <StatCard label="낙찰" value={formatNumber(member.pcount ?? 0)} unit="건" highlight />
              <StatCard label="미결제" value={formatNumber(member.notpay ?? 0)} unit="건" />
              <StatCard label="관심 상품" value={null} />
              <StatCard label="정지 이력" value={null} />
            </div>

            <div className="admin-split member-split">
              <div className="admin-stack">
                <Card
                  title="입찰 내역"
                  actions={<span className="member-card-meta">총 {formatNumber(allBids.length)}회 · 최신 {bids.length}건</span>}
                  noBody
                >
                  <DataTable
                    rowKey="productId"
                    rows={bids}
                    emptyText="입찰 내역이 없습니다."
                    columns={[
                      { key: 'productName', header: '상품명', render: (r) => r.productName ?? '-' },
                      { key: 'myBidPrice', header: '내 입찰가', align: 'right', render: (r) => formatPrice(r.myBidPrice) },
                      { key: 'finalPrice', header: '현재/최종가', align: 'right', render: (r) => formatPrice(r.finalPrice) },
                      { key: 'result', header: '결과', render: (r) => r.result ?? '-' },
                      { key: 'bidTime', header: '입찰 시각', align: 'right', render: (r) => formatDateTime(r.bidTime) },
                    ]}
                  />
                </Card>

                <Card title="낙찰 내역" actions={<span className="member-card-meta">총 {formatNumber(payments.length)}건</span>} noBody>
                  <DataTable
                    rowKey="auctionId"
                    rows={payments}
                    emptyText="낙찰 내역이 없습니다."
                    onRowClick={(r) => r.auctionId && navigate(`/admin/auctions/${r.auctionId}`)}
                    columns={[
                      { key: 'auctionId', header: '경매번호', render: (r) => r.auctionId ?? '-' },
                      { key: 'product', header: '상품명', render: (r) => r.product ?? '-' },
                      { key: 'organization', header: '제작기관', render: (r) => r.organization ?? '-' },
                      { key: 'paymentPrice', header: '낙찰가', align: 'right', render: (r) => formatPrice(r.paymentPrice) },
                      { key: 'winningDate', header: '낙찰일', align: 'right', render: (r) => formatDate(r.winningDate) },
                      { key: 'paymentStatus', header: '결제 상태', align: 'right', render: (r) => r.paymentStatus ?? '-' },
                    ]}
                  />
                </Card>
              </div>

              <div className="admin-stack">
                <Card title="회원 정보">
                  <InfoRows
                    items={[
                      { label: '이름', value: member.name },
                      { label: '이메일', value: member.email },
                      { label: '연락처', value: '-' },
                      { label: '가입일', value: formatDate(member.createdAt) },
                      { label: '최근 로그인', value: '-' },
                      { label: '마케팅 수신', value: '-' },
                    ]}
                  />
                </Card>

                <Card title="계정 정지 · 삭제">
                  <div className="admin-stack member-danger">
                    <div>
                      <strong>계정 정지</strong>
                      <p>
                        로그인과 입찰을 막습니다. 진행 중인 입찰은 유지되며, 정지 해제로 언제든 되돌릴 수 있습니다. 낙찰 후 반복
                        미결제 시 사용합니다.
                      </p>
                      <Button size="lg" className="admin-btn--block" onClick={() => setConfirmOpen(true)}>
                        {statusLabel}
                      </Button>
                    </div>
                    <div>
                      <strong>삭제</strong>
                      <p>
                        회원 탈퇴 처리입니다. 입찰·낙찰 기록은 거래 증빙으로 보존되며 개인정보만 파기됩니다. 관리자가 임의로 실행하지
                        않습니다.
                      </p>
                      <Button size="lg" className="admin-btn--block" pending>
                        삭제 (관리자 실행 불가)
                      </Button>
                    </div>
                  </div>
                </Card>
              </div>
            </div>
          </>
        )}
      </AsyncBoundary>

      <ConfirmModal
        open={confirmOpen}
        title={statusLabel}
        message={member ? `${member.name}(${member.loginId}) 회원을 ${suspended ? '정지 해제' : '정지'}하시겠습니까?` : ''}
        confirmText={suspended ? '정지 해제' : '정지'}
        danger={!suspended}
        onConfirm={handleStatusChange}
        onCancel={() => setConfirmOpen(false)}
      />
    </AdminPage>
  )
}
