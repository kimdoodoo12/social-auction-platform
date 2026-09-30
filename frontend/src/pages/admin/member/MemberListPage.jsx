import { useState } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import AdminPage from '../../../components/admin/AdminPage'
import DataTable from '../../../components/admin/DataTable'
import Pagination from '../../../components/admin/Pagination'
import { Badge, Button, Card, ConfirmModal, FilterBar, LinkButton, Summary } from '../../../components/admin/ui'
import { fetchMembers, normalizeMember, searchMembers, suspendMember } from '../../../api/member'
import { useAsync } from '../../../hooks/useAsync'
import { formatDate, formatNumber } from '../../../utils/format'
import { MEMBER_ROLES, SUSPENDED_ROLE, memberRoleTone } from './memberStatus'
import './member.css'

// [ADMIN] 11 회원 관리 (Figma 50:279)
// Figma처럼 검색창은 하나다. 백엔드는 name/email을 따로 받으므로 '@'가 있으면 이메일, 없으면 이름으로 보낸다.
// 가입일은 날짜 범위로 입력한다(사용자 결정).
const PAGE_SIZE = 8
const FILTER_KEYS = ['q', 'role', 'startDate', 'endDate']

function readFilters(params) {
  return Object.fromEntries(FILTER_KEYS.map((key) => [key, params.get(key) ?? '']))
}

// 검색어 하나를 백엔드 파라미터(name 또는 email)로 나눈다. 둘 다 완전 일치 검색이다.
function toSearchParams({ q, role, startDate, endDate }) {
  const keyword = q.trim()
  const isEmail = keyword.includes('@')
  return {
    name: keyword && !isEmail ? keyword : undefined,
    email: keyword && isEmail ? keyword : undefined,
    role: role || undefined,
    startDate: startDate || undefined,
    endDate: endDate || undefined,
  }
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
    () =>
      hasFilter
        ? searchMembers({ ...toSearchParams(filters), page, size: PAGE_SIZE })
        : fetchMembers({ page, size: PAGE_SIZE }),
    [filters.q, filters.role, filters.startDate, filters.endDate, page],
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

  // 정지 ↔ 정상 복구
  const handleStatusChange = async () => {
    const member = target
    const restoring = member.role === SUSPENDED_ROLE
    setTarget(null)
    setActionError(null)
    try {
      const ok = restoring ? await normalizeMember(member.memberId) : await suspendMember(member.memberId)
      if (!ok) setActionError(`${member.name} 회원을 ${restoring ? '정지 해제' : '정지'}하지 못했습니다.`)
    } catch (error) {
      setActionError(error.message)
    }
    list.reload()
    counts.reload()
  }

  const pageData = list.data
  const goDetail = (r) => navigate(`/admin/members/${r.memberId}`)

  const columns = [
    { key: 'memberId', header: '회원번호', width: 80 },
    { key: 'name', header: '이름', className: 'strong' },
    { key: 'email', header: '이메일' },
    { key: 'createdAt', header: '가입일', align: 'right', render: (r) => formatDate(r.createdAt) },
    { key: 'bcount', header: '입찰 횟수', align: 'right', render: (r) => formatNumber(r.bcount) },
    { key: 'pcount', header: '낙찰 횟수', align: 'right', render: (r) => formatNumber(r.pcount) },
    { key: 'role', header: '상태', render: (r) => <Badge tone={memberRoleTone(r.role)}>{r.role ?? '-'}</Badge> },
    {
      key: 'actions',
      header: '관리',
      render: (r) => (
        <div className="admin-actions" onClick={(e) => e.stopPropagation()}>
          <LinkButton onClick={() => goDetail(r)}>상세</LinkButton>
          <span className="admin-actions__sep">·</span>
          <LinkButton tone="muted" onClick={() => setTarget(r)}>
            {r.role === SUSPENDED_ROLE ? '정지 해제' : '정지'}
          </LinkButton>
        </div>
      ),
    },
  ]

  return (
    <AdminPage title="회원 관리">
      <FilterBar onSearch={() => applyFilters(draft)}>
        <input
          className="admin-input"
          placeholder="이름 또는 이메일로 검색"
          value={draft.q}
          onChange={changeDraft('q')}
        />
        <select className="admin-select" value={draft.role} onChange={changeDraft('role')} aria-label="상태">
          <option value="">전체 상태</option>
          {MEMBER_ROLES.map((role) => (
            <option key={role} value={role}>
              {role}
            </option>
          ))}
        </select>
        <div className="member-filter-date">
          <input type="date" className="admin-input" value={draft.startDate} onChange={changeDraft('startDate')} aria-label="가입일 시작" />
          ~
          <input type="date" className="admin-input" value={draft.endDate} onChange={changeDraft('endDate')} aria-label="가입일 종료" />
        </div>
        <Button type="submit" size="lg" className="admin-search-btn">
          검색
        </Button>
      </FilterBar>

      <Summary label={`${hasFilter ? '검색 결과' : '전체'} ${formatNumber(pageData?.totalElements ?? 0)}명`}>
        {counts.data && (
          <span>
            · 정상 {formatNumber(counts.data.normal)} · 정지 {formatNumber(counts.data.suspended)}
          </span>
        )}
      </Summary>
      {actionError && <div className="admin-state admin-state--error">{actionError}</div>}

      <Card noBody>
        <DataTable
          rowKey="memberId"
          columns={columns}
          rows={pageData?.content}
          loading={list.loading && !pageData}
          error={list.error}
          emptyText={hasFilter ? '검색 조건에 맞는 회원이 없습니다. (이름·이메일은 정확히 일치해야 합니다)' : '등록된 회원이 없습니다.'}
          onRowClick={goDetail}
        />
        {pageData && <Pagination page={pageData.page} totalPages={pageData.totalPages} onChange={(p) => applyFilters(filters, p)} />}
      </Card>

      <ConfirmModal
        open={Boolean(target)}
        title={target?.role === SUSPENDED_ROLE ? '정지 해제' : '회원 정지'}
        message={
          target
            ? `${target.name}(${target.loginId}) 회원을 ${target.role === SUSPENDED_ROLE ? '정지 해제' : '정지'}하시겠습니까?`
            : ''
        }
        confirmText={target?.role === SUSPENDED_ROLE ? '정지 해제' : '정지'}
        danger={target?.role !== SUSPENDED_ROLE}
        onConfirm={handleStatusChange}
        onCancel={() => setTarget(null)}
      />
    </AdminPage>
  )
}
