import { useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { useAsync } from '../../hooks/useAsync'
import { catalogApi } from './catalogApi'
import { catalogParams, SORT_OPTIONS } from './catalogHelpers'
import { CatalogState, ProductGrid } from './CatalogParts'
import UserHeader from './UserHeader'
import UserFooter from './UserFooter'

function Filters({ params, categories, onApply }) {
  const [error, setError] = useState('')
  function submit(event) {
    event.preventDefault()
    const values = Object.fromEntries(new FormData(event.currentTarget))
    if (values.lowprice !== '' && values.highprice !== '' && Number(values.lowprice) > Number(values.highprice)) {
      setError('최대 가격은 최소 가격 이상으로 입력해 주세요.'); return
    }
    setError(''); onApply(values)
  }
  return <form className="shop-filters" onSubmit={submit}>
    <div className="shop-filter-title"><h2>상세 검색</h2><button type="button" onClick={() => onApply({})}>초기화</button></div>
    <label>상품명<input name="productName" defaultValue={params.productName} placeholder="상품 이름을 입력하세요" maxLength={100} /></label>
    <label>카테고리<select name="categoryId" defaultValue={params.categoryId}><option value="">전체 카테고리</option>{categories.map(c => <option key={c.categoryId} value={c.categoryId}>{c.name}</option>)}</select></label>
    <label>경매 상태<select name="auctionStatus" defaultValue={params.auctionStatus}><option value="">전체 상태</option><option value="대기">경매 대기</option><option value="진행">진행 중</option><option value="완료">종료</option></select></label>
    <fieldset><legend>가격 범위 (원)</legend><label className="shop-sr-only" htmlFor="lowprice">최소 가격</label><input id="lowprice" name="lowprice" type="number" min="0" max="2147483647" step="1" defaultValue={params.lowprice} placeholder="최소 가격" /><span>~</span><label className="shop-sr-only" htmlFor="highprice">최대 가격</label><input id="highprice" name="highprice" type="number" min="0" max="2147483647" step="1" defaultValue={params.highprice} placeholder="최대 가격" /></fieldset>
    {error && <p role="alert" className="shop-error">{error}</p>}<button className="shop-button" type="submit">조건 적용</button>
  </form>
}

export default function CatalogPage() {
  const [search, setSearch] = useSearchParams()
  const key = search.toString()
  const params = catalogParams(key)
  const result = useAsync(() => catalogApi.list(params), [key])
  const categories = useAsync(catalogApi.categories)
  function update(values, replace = false) {
    const next = replace ? new URLSearchParams() : new URLSearchParams(search)
    for (const [k,v] of Object.entries(values)) v === '' || v == null ? next.delete(k) : next.set(k,String(v))
    setSearch(next)
  }
  const pages = result.data?.totalPages || 0
  const start = Math.max(0, Math.min(params.page - 2, pages - 5))
  return <div className="shop"><UserHeader /><main className="shop-width shop-catalog">
    <div className="shop-page-heading"><p className="shop-eyebrow">마음을 담아 만든 물건들</p><h1>상품 둘러보기</h1><p>필요한 물건에 담긴 새로운 이야기를 찾아보세요.</p></div>
    <div className="shop-catalog-layout"><aside><Filters key={key} params={params} categories={categories.data || []} onApply={(values) => update({ ...values, sortType: params.sortType, page: 0 }, true)} />{categories.error && <p className="shop-error">카테고리를 불러오지 못했습니다. <button onClick={categories.reload}>다시 시도</button></p>}</aside>
      <section aria-label="상품 검색 결과"><div className="shop-results-heading"><p>검색 결과 <strong>{result.loading ? '…' : (result.data?.totalElements ?? 0).toLocaleString()}</strong>개</p><label><span className="shop-sr-only">정렬</span><select value={params.sortType} onChange={e => update({sortType:e.target.value,page:0})}>{SORT_OPTIONS.map(([key,label]) => <option key={key} value={key}>{label}</option>)}</select></label></div>
        <CatalogState loading={result.loading} error={result.error} empty={!result.data?.content?.length} onRetry={result.reload}><ProductGrid products={result.data?.content || []} /></CatalogState>
        {!result.loading && !result.error && pages > 0 && <nav className="shop-pagination" aria-label="상품 페이지"><button disabled={params.page === 0} onClick={() => update({page:params.page-1})}>이전</button>{Array.from({length:Math.min(5,pages)},(_,i)=>start+i).map(page => <button key={page} aria-current={params.page === page ? 'page' : undefined} onClick={() => update({page})}>{page+1}</button>)}<button disabled={params.page >= pages-1} onClick={() => update({page:params.page+1})}>다음</button></nav>}
        {!result.loading && !result.error && params.page > 0 && !result.data?.content?.length && <button className="shop-button secondary" onClick={() => update({page:0})}>첫 페이지로 돌아가기</button>}
      </section></div>
  </main><UserFooter /></div>
}
