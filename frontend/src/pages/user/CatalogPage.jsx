import { useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { useAsync } from '../../hooks/useAsync'
import { catalogApi } from './catalogApi'
import { catalogParams, SORT_OPTIONS, STATUS_OPTIONS } from './catalogHelpers'
import { CatalogState, ProductGrid } from './CatalogParts'
import UserHeader from './UserHeader'
import UserFooter from './UserFooter'

// [USER] 02 상품 목록 (Figma 53:2579)
// - 카테고리·경매 상태는 여러 개 선택 (같은 쿼리 키 반복)
// - 상태별 개수는 API가 없어 '-' / 제작 기관 필터는 기관 목록 API가 없어 비활성 (사용자 결정)

const PRICE_MAX = 1000000 // 슬라이더 범위. 입력칸에는 더 큰 값도 넣을 수 있다.
const PRICE_STEP = 1000

function Check({ checked, onChange, label, count, disabled }) {
  return (
    <label className={`shop-check${disabled ? ' disabled' : ''}`}>
      <input type="checkbox" checked={checked} onChange={onChange} disabled={disabled} />
      <span className="shop-check-box" aria-hidden="true" />
      <span className="shop-check-label">{label}</span>
      <span className="shop-check-count">{count}</span>
    </label>
  )
}

function PriceRange({ low, high, onApply }) {
  const [draft, setDraft] = useState({ low: low === '' ? '' : String(low), high: high === '' ? '' : String(high) })
  const lowNum = draft.low === '' ? 0 : Math.min(Number(draft.low), PRICE_MAX)
  const highNum = draft.high === '' ? PRICE_MAX : Math.min(Number(draft.high), PRICE_MAX)

  const commit = (next = draft) => {
    const l = next.low === '' ? '' : Number(next.low)
    const h = next.high === '' ? '' : Number(next.high)
    if (l !== '' && h !== '' && l > h) onApply({ lowprice: h, highprice: l })
    else onApply({ lowprice: l, highprice: h })
  }
  const onKey = (e) => e.key === 'Enter' && commit()
  const setSlider = (key) => (e) => {
    const v = Number(e.target.value)
    setDraft((d) => ({
      ...d,
      [key]: key === 'low' ? String(Math.min(v, highNum)) : v >= PRICE_MAX ? '' : String(Math.max(v, lowNum)),
    }))
  }
  const fill = { left: `${(lowNum / PRICE_MAX) * 100}%`, right: `${100 - (highNum / PRICE_MAX) * 100}%` }

  return (
    <div className="shop-price-range">
      <div className="shop-slider">
        <span className="shop-slider-track" />
        <span className="shop-slider-fill" style={fill} />
        <input type="range" min={0} max={PRICE_MAX} step={PRICE_STEP} value={lowNum} aria-label="최소 가격"
          onChange={setSlider('low')} onPointerUp={() => commit()} onKeyUp={() => commit()} />
        <input type="range" min={0} max={PRICE_MAX} step={PRICE_STEP} value={highNum} aria-label="최대 가격"
          onChange={setSlider('high')} onPointerUp={() => commit()} onKeyUp={() => commit()} />
      </div>
      <div className="shop-range-inputs">
        <label className="shop-range-input">
          <input inputMode="numeric" placeholder="0" value={draft.low} aria-label="최소 가격 입력"
            onChange={(e) => setDraft((d) => ({ ...d, low: e.target.value.replace(/\D/g, '') }))}
            onBlur={() => commit()} onKeyDown={onKey} />
          <span>원</span>
        </label>
        <span className="shop-range-sep">~</span>
        <label className="shop-range-input">
          <input inputMode="numeric" placeholder="최대" value={draft.high} aria-label="최대 가격 입력"
            onChange={(e) => setDraft((d) => ({ ...d, high: e.target.value.replace(/\D/g, '') }))}
            onBlur={() => commit()} onKeyDown={onKey} />
          <span>원</span>
        </label>
      </div>
    </div>
  )
}

function Pager({ page, totalPages, onChange }) {
  if (!totalPages) return null
  const start = Math.max(0, Math.min(page - 2, totalPages - 5))
  const pages = Array.from({ length: Math.min(5, totalPages) }, (_, i) => start + i)
  return (
    <nav className="shop-pagination" aria-label="상품 페이지">
      <button type="button" disabled={page === 0} onClick={() => onChange(page - 1)} aria-label="이전 페이지">‹</button>
      {pages.map((p) => (
        <button key={p} type="button" aria-current={page === p ? 'page' : undefined} onClick={() => onChange(p)}>
          {p + 1}
        </button>
      ))}
      <button type="button" disabled={page >= totalPages - 1} onClick={() => onChange(page + 1)} aria-label="다음 페이지">›</button>
    </nav>
  )
}

export default function CatalogPage() {
  const [search, setSearch] = useSearchParams()
  const key = search.toString()
  const params = catalogParams(key)
  const result = useAsync(() => catalogApi.list(params), [key])
  const categories = useAsync(catalogApi.categories)
  const categoryList = categories.data || []

  // 쿼리 갱신. 배열 값은 같은 키를 반복해서 넣는다. 필터가 바뀌면 1페이지로 돌아간다.
  const update = (changes, { keepPage = false } = {}) => {
    const next = new URLSearchParams(search)
    Object.entries(changes).forEach(([k, v]) => {
      next.delete(k)
      if (Array.isArray(v)) v.forEach((item) => next.append(k, String(item)))
      else if (v !== '' && v != null) next.set(k, String(v))
    })
    if (!keepPage) next.delete('page')
    setSearch(next)
  }
  const toggle = (list, value) => (list.includes(value) ? list.filter((v) => v !== value) : [...list, value])

  const totalCount = categoryList.reduce((sum, c) => sum + Number(c.productCount ?? 0), 0)
  const selectedCategory = params.categoryIds.length === 1 ? categoryList.find((c) => c.categoryId === params.categoryIds[0]) : null
  const title = selectedCategory?.name ?? '전체 상품'

  const applied = [
    ...params.categoryIds.map((id) => categoryList.find((c) => c.categoryId === id)?.name).filter(Boolean),
    ...params.auctionStatuses.map((s) => STATUS_OPTIONS.find(([v]) => v === s)?.[1]),
    params.lowprice !== '' || params.highprice !== '' ? '가격' : null,
    params.productName ? `'${params.productName}'` : null,
    params.organizationId !== '' ? (result.data?.content?.[0]?.organizationName ?? '제작 기관') : null,
  ].filter(Boolean)

  return (
    <div className="shop">
      <UserHeader />
      <main className="shop-width shop-catalog">
        <div className="shop-page-heading">
          <p className="shop-breadcrumb">
            <Link to="/">홈</Link> › {title}
          </p>
          <h1>{title}</h1>
        </div>

        <div className="shop-catalog-layout">
          <aside className="shop-filters" aria-label="상품 필터">
            <section className="shop-filter-group">
              <h2>카테고리</h2>
              <Check label="전체" count={totalCount || '-'} checked={!params.categoryIds.length} onChange={() => update({ categoryId: [] })} />
              {categoryList.map((c) => (
                <Check key={c.categoryId} label={c.name} count={c.productCount ?? '-'}
                  checked={params.categoryIds.includes(c.categoryId)}
                  onChange={() => update({ categoryId: toggle(params.categoryIds, c.categoryId) })} />
              ))}
              {categories.error && (
                <p className="shop-error">카테고리를 불러오지 못했습니다. <button type="button" onClick={categories.reload}>다시 시도</button></p>
              )}
            </section>

            <section className="shop-filter-group">
              <h2>경매 상태</h2>
              {STATUS_OPTIONS.map(([value, label]) => (
                <Check key={value} label={label} count="-" checked={params.auctionStatuses.includes(value)}
                  onChange={() => update({ auctionStatus: toggle(params.auctionStatuses, value) })} />
              ))}
            </section>

            <section className="shop-filter-group">
              <h2>가격 범위</h2>
              <PriceRange key={`${params.lowprice}-${params.highprice}`} low={params.lowprice} high={params.highprice}
                onApply={(v) => update(v)} />
            </section>

            {/* 사용자용 기관 목록 API가 없어 틀만 두고 비활성 (사용자 결정) */}
            <section className="shop-filter-group" title="준비 중인 기능입니다.">
              <h2>제작 기관</h2>
              <p className="shop-filter-pending">기관 목록을 불러올 수 없어 준비 중입니다.</p>
              <button type="button" className="shop-more-link" disabled>기관 더 보기 →</button>
            </section>

            <button type="button" className="shop-button secondary shop-filter-reset" onClick={() => setSearch(new URLSearchParams())}>
              필터 초기화
            </button>
          </aside>

          <section className="shop-results" aria-label="상품 검색 결과">
            <div className="shop-results-heading">
              <p>
                <strong>{result.loading ? '…' : `${(result.data?.totalElements ?? 0).toLocaleString()}개`}</strong>
                {applied.length > 0 && <span> · {applied.join(', ')} 필터 적용됨</span>}
              </p>
              <div className="shop-sort-tabs" role="tablist" aria-label="정렬">
                {SORT_OPTIONS.map(([value, label]) => (
                  <button key={value} type="button" role="tab" aria-selected={params.sortType === value}
                    className={params.sortType === value ? 'active' : ''} onClick={() => update({ sortType: value })}>
                    {label}
                  </button>
                ))}
              </div>
            </div>

            <CatalogState loading={result.loading} error={result.error} empty={!result.data?.content?.length} onRetry={result.reload}>
              <ProductGrid products={result.data?.content || []} />
            </CatalogState>
            {!result.loading && !result.error && (
              <Pager page={params.page} totalPages={result.data?.totalPages || 0} onChange={(p) => update({ page: p }, { keepPage: true })} />
            )}
          </section>
        </div>
      </main>
      <UserFooter />
    </div>
  )
}
