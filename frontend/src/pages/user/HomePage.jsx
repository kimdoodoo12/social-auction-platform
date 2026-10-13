import { Link } from 'react-router-dom'
import { useAsync } from '../../hooks/useAsync'
import { catalogApi } from './catalogApi'
import { CatalogState, ProductGrid } from './CatalogParts'
import UserHeader from './UserHeader'
import UserFooter from './UserFooter'
import heroImage from '../../assets/main-hero.png'

// [USER] 01 메인 (Figma 25:2327)
// 섹션: 추천(recommend) · 마감 임박(end) · 인기(popular) · 신규(new) — 각 4개
const SECTIONS = [
  { type: 'recommend', title: '오늘의 추천 상품', description: '협약 기관이 직접 추천한 상품입니다.' },
  {
    type: 'end',
    title: '마감 임박 경매',
    description: '오늘 안에 종료되는 경매입니다. 놓치기 전에 입찰해 보세요.',
    to: '/products?auctionStatus=진행&sortType=time',
  },
  {
    type: 'popular',
    title: '진행 중인 인기 경매',
    description: '지금 가장 많은 입찰이 몰리고 있는 상품입니다.',
    to: '/products?sortType=popular',
  },
  {
    type: 'new',
    title: '새로 등록된 상품',
    description: '아직 아무도 입찰하지 않았습니다. 첫 입찰과 동시에 24시간 경매가 시작됩니다.',
    to: '/products?auctionStatus=대기&sortType=new',
  },
]

const CARDS_PER_SECTION = 4
const PARTNER_CHIPS = 6 // Figma 협약 기관 띠: 기관명 칩 6개

// 협약 기관 띠: 기관 수와 기관명 (불러오지 못하면 '-'와 빈 칩 영역)
function PartnersBand() {
  const { data } = useAsync(catalogApi.organizations)
  const count = data?.count ?? '-'
  return (
    <section className="shop-partners" aria-label="협약 기관">
      <h2>이음옥션은 {count}개 기관과 협약을 맺고 있습니다</h2>
      <p>모든 상품은 협약 기관이 제공하고, 관리자가 직접 검수한 뒤 등록됩니다.</p>
      <ul className="shop-partner-chips">
        {(data?.names || []).slice(0, PARTNER_CHIPS).map((name) => <li key={name}>{name}</li>)}
      </ul>
    </section>
  )
}

function ProductSection({ type, title, description, to }) {
  const { data, loading, error, reload } = useAsync(() => catalogApi.section(type), [type])
  const products = (data || []).slice(0, CARDS_PER_SECTION)
  return (
    <section className="shop-section" aria-label={title}>
      <div className="shop-section-heading">
        <div>
          <h2>{title}</h2>
          <p>{description}</p>
        </div>
        {to && <Link to={to}>전체보기 →</Link>}
      </div>
      <CatalogState loading={loading} error={error} empty={!products.length} onRetry={reload}>
        <ProductGrid products={products} />
      </CatalogState>
    </section>
  )
}

export default function HomePage() {
  return (
    <div className="shop">
      <UserHeader />
      <main>
        <section className="shop-hero">
          <div className="shop-width shop-hero-inner">
            <div className="shop-hero-copy">
              <p className="shop-eyebrow">사회적 가치 경매</p>
              <h1>
                당신의 입찰이
                <br />
                누군가의 내일이 됩니다
              </h1>
              <p className="shop-hero-description">
                장애인 및 사회적 약자가 만든 상품을 협약 기관과 함께 엄선했습니다.
                <br />
                첫 입찰이 등록되는 순간부터 24시간 동안 경매가 진행됩니다.
              </p>
              <div className="shop-hero-cta">
                <Link className="shop-button" to="/products?auctionStatus=진행">
                  진행 중인 경매 보기
                </Link>
                {/* 서비스 소개 페이지가 없어 비활성 */}
                <button type="button" className="shop-button secondary" disabled title="준비 중인 기능입니다.">
                  서비스 소개
                </button>
              </div>
            </div>
            <img className="shop-hero-image" src={heroImage} alt="이음옥션에 참여한 사람들" />
          </div>
        </section>

        <div className="shop-width shop-sections">
          {SECTIONS.map((section) => (
            <ProductSection key={section.type} {...section} />
          ))}

          {/* 협약 기관 안내: 사용자용 기관 API가 없어 개수는 '-'로 두고 틀만 유지한다 (사용자 결정) */}
          <PartnersBand />
        </div>
      </main>
      <UserFooter />
    </div>
  )
}
