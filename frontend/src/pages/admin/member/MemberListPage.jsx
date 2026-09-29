import { useState } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import AdminPage from '../../../components/admin/AdminPage'
import DataTable from '../../../components/admin/DataTable'
import Pagination from '../../../components/admin/Pagination'
import { Badge, Button, Card, ConfirmModal, FilterBar, LinkButton, Summary } from '../../../components/admin/ui'
import { fetchMembers, searchMembers, suspendMember } from '../../../api/member'
import { useAsync } from '../../../hooks/useAsync'
import { formatDate, formatNumber } from '../../../utils/format'
import { MEMBER_ROLES, SUSPENDED_ROLE, memberRoleTone } from './memberStatus'
import './member.css'

// [ADMIN] 11 회원 관리
const PAGE_SIZE = 10
const FILTER_KEYS = ['name', 'email', 'role', 'startDate', 'endDate']

function readFilters(params) {
  return Object.fromEntries(FILTER_KEYS.map((key) => [key, params.get(key) ?? '']))
}

export default function MemberListPage() {
  const navigate = useNavigate()
  const [params, setParams] = useSearchParams()
  const filters = readFilters(params)
  const page = Number(params.get('page')) || 0
  const hasFilter = FILTER_KEYS.some((key) => filters[key])

  // 입력 중인 검색 조건. 검색 버튼을 누르면 URL에 반영한다.
  const [draft, setDraft] = useState(filters)
  const [target, setTarget] = useState(null)
  const [actionError, setActionError] = useState(null)

  const list = useAsync(
    () => (hasFilter ? searchMembers({ ...filters, page, size: PAGE_SIZE }) : fetchMembers({ page, size: PAGE_SIZE })),
    [filters.name, filters.email, filters.role, filters.startDate, filters.endDate, page],
  )

  // 상태별 인원은 검색 API의 totalElements로 구한다. 실패하면 보조 정보이므로 요약 줄에서만 생략한다.
  const counts = useAsync(
    () =>
      Promise.all([searchMembers({ role: '정상', size: 1 }), searchMembers({ role: SUSPENDED_ROLE, size: 1 })]).then(
        ([normal, suspended]) => ({ normal: normal.totalElements, suspended: suspended.totalElements }),
      ),
    [],
  )

  const changeDraft = (key) => (e) => setDraft((prev) => ({ ...prev, [key]: e.target.value }))

  const applyFilters = (next, nextPage = 0) => {
    const search = {}
    FILTER_KEYS.forEach((key) => {
      if (next[key]) search[key] = next[key]
    })
    if (nextPage > 0) search.page = String(nextPage)
    setParams(search)
  }

  const handleReset = () => {
    const empty = Object.fromEntries(FILTER_KEYS.map((key) => [key, '']))
    setDraft(empty)
    applyFilters(empty)
  }

  const handleSuspend = async () => {
    const member = target
    setTarget(null)
    setActionError(null)
    try {
      const ok = await suspendMember(member.memberId)
      if (!ok) setActionError(`${member.name} 회원을 정지하지 못했습니다.`)
    } catch (error) {
      setActionError(error.message)
    }
    list.reload()
    counts.reload()
  }

  const pageData = list.data
  const columns = [
    { key: 'memberId', header: '회원번호', width: 90 },
    { key: 'loginId', header: '아이디' },
    { key: 'name', header: '이름', className: 'strong' },
    { key: 'email', header: '이메일' },
    { key: 'createdAt', header: '가입일', render: (r) => formatDate(r.createdAt) },
    { key: 'bcount', header: '입찰', align: 'right', render: (r) => formatNumber(r.bcount) },
    { key: 'pcount', header: '낙찰', align: 'right', render: (r) => formatNumber(r.pcount) },
    { key: 'role', header: '상태', render: (r) => <Badge tone={memberRoleTone(r.role)}>{r.role ?? '-'}</Badge> },
    {
      key: 'actions',
      header: '관리',
      align: 'right',
      render: (r) => (
        <div className="admin-actions admin-actions--end" onClick={(e) => e.stopPropagation()}>
          <LinkButton onClick={() => navigate(`/admin/members/${r.memberId}`)}>상세</LinkButton>
          <span className="admin-actions__sep">·</span>
          <LinkButton
            tone={r.role === SUSPENDED_ROLE ? 'muted' : 'primary'}
            disabled={r.role === SUSPENDED_ROLE}
            onClick={() => setTarget(r)}
          >
            정지
          </LinkButton>
        </div>
      ),
    },
  ]

  return (
    <AdminPage title="회원 관리">
      <FilterBar onSearch={() => applyFilters(draft)}>
        <input className="admin-input" placeholder="이름" value={draft.name} onChange={changeDraft('name')} />
        <input className="admin-input" placeholder="이메일" value={draft.email} onChange={changeDraft('email')} />
        <select className="admin-select" value={draft.role} onChange={changeDraft('role')} aria-label="상태">
          <option value="">전체 상태</option>
          {MEMBER_ROLES.map((role) => (
            <option key={role} value={role}>
              {role}
            </option>
          ))}
        </select>
        <div className="member-filter-date">
          <input
            type="date"
            className="admin-input"
            value={draft.startDate}
            onChange={changeDraft('startDate')}
            aria-label="가입일 시작"
          />
          ~
          <input
            type="date"
            className="admin-input"
            value={draft.endDate}
            onChange={changeDraft('endDate')}
            aria-label="가입일 종료"
          />
        </div>
        <Button variant="dark" type="submit">
          검색
        </Button>
        <Button onClick={handleReset}>초기화</Button>
      </FilterBar>

      <Card noBody>
        <div className="member-list-head">
          <Summary label={`${hasFilter ? '검색 결과' : '전체'} ${formatNumber(pageData?.totalElements ?? 0)}명`}>
            {counts.data && (
              <span>
                · 정상 {formatNumber(counts.data.normal)} · 정지 {formatNumber(counts.data.suspended)}
              </span>
            )}
          </Summary>
          {actionError && <span className="member-notice-error">{actionError}</span>}
        </div>
        <DataTable
          rowKey="memberId"
          columns={columns}
          rows={pageData?.content}
          loading={list.loading && !pageData}
          error={list.error}
          emptyText={hasFilter ? '검색 조건에 맞는 회원이 없습니다.' : '등록된 회원이 없습니다.'}
          onRowClick={(r) => navigate(`/admin/members/${r.memberId}`)}
        />
        {pageData && (
          <Pagination
            page={pageData.page}
            totalPages={pageData.totalPages}
            onChange={(p) => applyFilters(filters, p)}
          />
        )}
      </Card>

      <ConfirmModal
        open={Boolean(target)}
        title="회원 정지"
        message={target ? `${target.name}(${target.loginId}) 회원을 정지하시겠습니까?` : ''}
        confirmText="정지"
        danger
        onConfirm={handleSuspend}
        onCancel={() => setTarget(null)}
      />
    </AdminPage>
  )
}
