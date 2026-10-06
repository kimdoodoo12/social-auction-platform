import { useState } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import {
  AUCTION_STATUS_OPTIONS,
  fetchCategoryOptions,
  fetchOrganizationOptions,
  fetchProducts,
  searchProducts,
  stopSelling,
} from '../../../api/product'
import AdminPage from '../../../components/admin/AdminPage'
import DataTable from '../../../components/admin/DataTable'
import Pagination from '../../../components/admin/Pagination'
import { Button, Card, ConfirmModal, FilterBar, LinkButton, Summary } from '../../../components/admin/ui'
import { useAsync } from '../../../hooks/useAsync'
import { formatDate, formatNumber, formatPrice } from '../../../utils/format'
import { ProductStatusBadge, ProductThumb } from './ProductParts'
import './product.css'

// [ADMIN] 03 상품 관리
// 검색 조건이 없으면 GET /ieum/admin/product/main, 하나라도 있으면 GET /ieum/admin/product/search 를 호출한다.
const FILTER_KEYS = ['productName', 'organizationName', 'categoryName', 'auctionStatus']

const STATUS_LABEL = { 대기: '경매 대기', 진행: '경매 진행 중', 완료: '경매 종료' }

// 경매가 진행/대기 중인 상품만 판매 중지할 수 있다
const canStop = (status) => status === '진행' || status === '대기'

export default function ProductListPage() {
  const navigate = useNavigate()
  const [searchParams, setSearchParams] = useSearchParams()

  const page = Math.max(0, Number(searchParams.get('page')) || 0)
  const applied = Object.fromEntries(FILTER_KEYS.map((k) => [k, searchParams.get(k) ?? '']))
  const searching = FILTER_KEYS.some((k) => applied[k])

  const [draft, setDraft] = useState(applied)
  const [stopTarget, setStopTarget] = useState(null)
  const [stopError, setStopError] = useState(null)

  const list = useAsync(
    () => (searching ? searchProducts({ ...applied, page }) : fetchProducts({ page })),
    [page, applied.productName, applied.organizationName, applied.categoryName, applied.auctionStatus],
  )
  const categories = useAsync(fetchCategoryOptions, [])
  const organizations = useAsync(fetchOrganizationOptions, [])

  const pushQuery = (next) => {
    const params = {}
    FILTER_KEYS.forEach((k) => {
      const v = (next[k] ?? '').trim()
      if (v) params[k] = v
    })
    if (next.page) params.page = String(next.page)
    setSearchParams(params)
  }

  const handleSearch = () => pushQuery({ ...draft, page: 0 })
  const setDraftField = (key) => (e) => setDraft((prev) => ({ ...prev, [key]: e.target.value }))

  // 상세/수정 응답에 경매 상태가 없어 목록 값을 함께 넘긴다
  const go = (row, suffix = '') =>
    navigate(`/admin/products/${row.productId}${suffix}`, { state: { status: row.status } })

  const handleStop = async () => {
    const target = stopTarget
    setStopTarget(null)
    try {
      await stopSelling(target.productId)
      setStopError(null)
      list.reload()
    } catch (error) {
      setStopError(`판매 중지 실패: ${error.message}`)
    }
  }

  const data = list.data

  return (
    <AdminPage title="상품 관리">
      <FilterBar onSearch={handleSearch}>
        <input
          className="admin-input"
          placeholder="상품명으로 검색"
          value={draft.productName}
          onChange={setDraftField('productName')}
        />
        <select className="admin-select" value={draft.organizationName} onChange={setDraftField('organizationName')}>
          <option value="">전체 기관</option>
          {(organizations.data ?? []).map((o) => (
            <option key={o.id} value={o.name}>
              {o.name}
            </option>
          ))}
        </select>
        <select className="admin-select" value={draft.auctionStatus} onChange={setDraftField('auctionStatus')}>
          <option value="">전체 상태</option>
          {AUCTION_STATUS_OPTIONS.map((s) => (
            <option key={s} value={s}>
              {STATUS_LABEL[s] ?? s}
            </option>
          ))}
        </select>
        <select className="admin-select" value={draft.categoryName} onChange={setDraftField('categoryName')}>
          <option value="">전체 카테고리</option>
          {(categories.data ?? []).map((c) => (
            <option key={c.id} value={c.name}>
              {c.name}
            </option>
          ))}
        </select>
        <Button type="submit" size="lg" className="admin-search-btn">
          검색
        </Button>
      </FilterBar>

      {stopError && <div className="admin-state admin-state--error">{stopError}</div>}

      <div className="admin-list-head">
        <Summary label={data ? `${searching ? '검색 결과' : '전체'} ${formatNumber(data.totalElements)}개` : '전체'} />
        <Button variant="primary" size="lg" onClick={() => navigate('/admin/products/new')}>
          + 상품 등록
        </Button>
      </div>

      <Card noBody>
        <DataTable
          rowKey="key"
          rows={data?.content}
          loading={list.loading && !data}
          error={list.error}
          emptyText={searching ? '검색 결과가 없습니다.' : '등록된 상품이 없습니다.'}
          onRowClick={(r) => go(r)}
          columns={[
            { key: 'productId', header: '상품번호', width: 74 },
            { key: 'image', header: '이미지', width: 52, render: (r) => <ProductThumb path={r.image} alt={r.productName} /> },
            { key: 'productName', header: '상품명', className: 'strong' },
            { key: 'organizationName', header: '제작기관' },
            { key: 'startPrice', header: '시작가', align: 'right', render: (r) => formatPrice(r.startPrice) },
            {
              key: 'currentPrice',
              header: '현재가',
              align: 'right',
              // 입찰 전(경매 대기)에는 Figma처럼 "—"
              render: (r) => (r.status === '대기' || r.currentPrice == null ? '—' : formatPrice(r.currentPrice)),
            },
            { key: 'status', header: '상태', render: (r) => <ProductStatusBadge status={r.status} /> },
            { key: 'createdAt', header: '등록일', align: 'right', render: (r) => formatDate(r.createdAt) },
            {
              key: 'manage',
              header: '관리',
              render: (r) => (
                <span className="admin-actions" onClick={(e) => e.stopPropagation()}>
                  <LinkButton onClick={() => go(r)}>상세</LinkButton>
                  <span className="admin-actions__sep">·</span>
                  <LinkButton onClick={() => go(r, '/edit')}>수정</LinkButton>
                  <span className="admin-actions__sep">·</span>
                  <LinkButton
                    tone="muted"
                    disabled={!canStop(r.status)}
                    title={canStop(r.status) ? undefined : '판매 중지할 수 있는 경매가 없습니다.'}
                    onClick={() => setStopTarget(r)}
                  >
                    판매중지
                  </LinkButton>
                </span>
              ),
            },
          ]}
        />
        {data && <Pagination page={data.page} totalPages={data.totalPages} onChange={(p) => pushQuery({ ...applied, page: p })} />}
      </Card>

      <ConfirmModal
        open={Boolean(stopTarget)}
        title="판매 중지"
        message={stopTarget ? `'${stopTarget.productName}' 상품을 판매 중지할까요?` : ''}
        confirmText="판매 중지"
        danger
        onConfirm={handleStop}
        onCancel={() => setStopTarget(null)}
      />
    </AdminPage>
  )
}
