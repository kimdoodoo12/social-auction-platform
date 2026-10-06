import { useState } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import {
  AUCTION_STATUS_OPTIONS,
  fetchCategoryOptions,
  fetchOrganizationOptions,
  fetchProducts,
  NO_AUCTION_STATUS,
  searchProducts,
  stopSelling,
  STOPPED_STATUS,
} from '../../../api/product'
import AdminPage from '../../../components/admin/AdminPage'
import DataTable from '../../../components/admin/DataTable'
import Pagination from '../../../components/admin/Pagination'
import { Button, Card, ConfirmModal, FilterBar, LinkButton, Summary } from '../../../components/admin/ui'
import { useAsync } from '../../../hooks/useAsync'
import { formatDate, formatNumber, formatPrice } from '../../../utils/format'
import { ProductImage, ProductStatusBadge } from './ProductParts'
import './product.css'

// [ADMIN] 03 상품 관리
// 검색 조건이 없으면 GET /ieum/admin/product/main, 하나라도 있으면 GET /ieum/admin/product/search 를 호출한다.
const FILTER_KEYS = ['productName', 'organizationName', 'categoryName', 'auctionStatus']

// 경매가 없는 상품은 백엔드 판매 중지 처리 시 경매를 찾지 못해 실패하므로 막는다.
const canStop = (status) => Boolean(status) && status !== NO_AUCTION_STATUS && status !== STOPPED_STATUS

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
  const handleReset = () => {
    const empty = Object.fromEntries(FILTER_KEYS.map((k) => [k, '']))
    setDraft(empty)
    pushQuery({ ...empty, page: 0 })
  }
  const setDraftField = (key) => (e) => setDraft((prev) => ({ ...prev, [key]: e.target.value }))

  const openDetail = (row) =>
    navigate(`/admin/products/${row.productId}`, {
      // 상세 응답(ProductDto)에 시작가·상태가 없어 목록 값을 함께 넘긴다
      state: { startPrice: row.startPrice, status: row.status },
    })

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
          placeholder="상품명 검색"
          value={draft.productName}
          onChange={setDraftField('productName')}
        />
        <select className="admin-select" value={draft.organizationName} onChange={setDraftField('organizationName')}>
          <option value="">제작기관 전체</option>
          {(organizations.data ?? []).map((o) => (
            <option key={o.id} value={o.name}>
              {o.name}
            </option>
          ))}
        </select>
        <select className="admin-select" value={draft.categoryName} onChange={setDraftField('categoryName')}>
          <option value="">카테고리 전체</option>
          {(categories.data ?? []).map((c) => (
            <option key={c.id} value={c.name}>
              {c.name}
            </option>
          ))}
        </select>
        <select className="admin-select" value={draft.auctionStatus} onChange={setDraftField('auctionStatus')}>
          <option value="">경매 상태 전체</option>
          {AUCTION_STATUS_OPTIONS.map((s) => (
            <option key={s} value={s}>
              {s}
            </option>
          ))}
        </select>
        <Button type="submit" variant="dark">
          검색
        </Button>
        <Button onClick={handleReset}>초기화</Button>
      </FilterBar>

      {stopError && <div className="admin-state admin-state--error">{stopError}</div>}

      <div className="product-list-head">
        <Summary label={searching ? '검색 결과' : '전체 상품'}>
          {data ? `${formatNumber(data.totalElements)}개` : ''}
        </Summary>
        <Button variant="primary" onClick={() => navigate('/admin/products/new')}>
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
          onRowClick={openDetail}
          columns={[
            { key: 'image', header: '이미지', width: 76, render: (r) => <ProductImage path={r.image} alt={r.productName} /> },
            { key: 'productId', header: '상품번호' },
            { key: 'productName', header: '상품명', className: 'strong' },
            { key: 'organizationName', header: '제작기관' },
            { key: 'startPrice', header: '시작가', align: 'right', render: (r) => formatPrice(r.startPrice) },
            { key: 'currentPrice', header: '현재가', align: 'right', render: (r) => formatPrice(r.currentPrice) },
            { key: 'status', header: '상태', render: (r) => <ProductStatusBadge status={r.status} /> },
            { key: 'createdAt', header: '등록일', render: (r) => formatDate(r.createdAt) },
            {
              key: 'manage',
              header: '관리',
              align: 'right',
              render: (r) => (
                <span className="admin-actions admin-actions--end" onClick={(e) => e.stopPropagation()}>
                  <LinkButton onClick={() => openDetail(r)}>상세</LinkButton>
                  <span className="admin-actions__sep">·</span>
                  <LinkButton
                    onClick={() =>
                      navigate(`/admin/products/${r.productId}/edit`, {
                        state: { startPrice: r.startPrice, status: r.status },
                      })
                    }
                  >
                    수정
                  </LinkButton>
                  <span className="admin-actions__sep">·</span>
                  <LinkButton
                    tone="muted"
                    disabled={!canStop(r.status)}
                    title={canStop(r.status) ? undefined : '진행 중인 경매가 없어 판매 중지할 수 없습니다.'}
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
        message={stopTarget ? `'${stopTarget.productName}' 상품의 경매를 판매 중지할까요?` : ''}
        confirmText="판매 중지"
        danger
        onConfirm={handleStop}
        onCancel={() => setStopTarget(null)}
      />
    </AdminPage>
  )
}
