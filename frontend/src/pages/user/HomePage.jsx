import { Link } from 'react-router-dom'
import { useAsync } from '../../hooks/useAsync'
import { catalogApi } from './catalogApi'
import { CatalogState, ProductGrid } from './CatalogParts'
import UserHeader from './UserHeader'
import UserFooter from './UserFooter'

function ProductSection({ type, title, description, to }) {
  const { data, loading, error, reload } = useAsync(() => catalogApi.section(type), [type])
  return <section className="shop-section" aria-label={title}>
    <div className="shop-section-heading"><div><h2>{title}</h2><p>{description}</p></div><Link to={to}>전체 보기 <span aria-hidden="true">→</span></Link></div>
    <CatalogState loading={loading} error={error} empty={!data?.length} onRetry={reload}><ProductGrid products={data || []} /></CatalogState>
  </section>
}

export default function HomePage() {
  return <div className="shop"><UserHeader /><main className="shop-width">
    <section className="shop-hero"><div><p className="shop-eyebrow">가치를 잇는 경매, 이음옥션</p><h1>좋은 물건을 만나는 일,<br />좋은 변화를 만드는 일.</h1><p className="shop-hero-description">정성으로 만든 상품에 나만의 가치를 더해 보세요.<br />당신의 입찰이 만드는 사람의 내일로 이어집니다.</p><Link className="shop-button" to="/products">상품 둘러보기 <span aria-hidden="true">↗</span></Link></div><div className="shop-hero-art" aria-hidden="true"><span className="shop-orbit one" /><span className="shop-orbit two" /><span className="shop-art-label">만드는 마음<br /><b>이어지는 가치</b></span></div></section>
    <ProductSection type="recommend" title="당신에게 이어지는 상품" description="관심을 바탕으로 고른 상품과 새로운 발견" to="/products" />
    <ProductSection type="end" title="곧 마감되는 경매" description="참여할 수 있는 시간이 얼마 남지 않았어요" to="/products?auctionStatus=진행&sortType=time" />
    <ProductSection type="popular" title="지금 관심받는 상품" description="많은 분들이 입찰로 마음을 전하고 있어요" to="/products?sortType=popular" />
    <ProductSection type="new" title="첫 입찰을 기다려요" description="아직 입찰이 없는 상품의 첫 주인공이 되어 보세요" to="/products?auctionStatus=대기&sortType=new" />
    <section className="shop-guide"><h2>처음 참여하시나요?</h2><ol><li><b>01</b><strong>마음에 드는 상품 찾기</strong><p>상품과 제작 기관의 이야기를 살펴보세요.</p></li><li><b>02</b><strong>나의 금액으로 입찰하기</strong><p>상세 화면에서 최소 입찰가와 마감 시간을 확인하세요.</p></li><li><b>03</b><strong>마이페이지에서 결과 확인</strong><p>경매 종료 후 낙찰과 결제 상태를 확인하세요.</p></li></ol></section>
  </main><UserFooter /></div>
}
