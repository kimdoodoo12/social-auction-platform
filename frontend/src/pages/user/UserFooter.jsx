// Figma 00 Foundation · Footer / User (연결할 페이지가 없는 항목은 텍스트로만 둔다)
const COLUMNS = [
  { title: '이음옥션', items: ['서비스 소개', '협약 기관 안내', '공지사항'] },
  { title: '고객지원', items: ['자주 묻는 질문', '경매 이용 안내', '1:1 문의'] },
  { title: '정책', items: ['이용약관', '개인정보처리방침', '입찰 및 낙찰 규정'] },
]

export default function UserFooter() {
  return (
    <footer className="shop-footer">
      <div className="shop-width">
        <div className="shop-footer-columns">
          {COLUMNS.map((col) => (
            <div key={col.title} className="shop-footer-col">
              <strong>{col.title}</strong>
              {col.items.map((item) => (
                <span key={item}>{item}</span>
              ))}
            </div>
          ))}
          <div className="shop-footer-col mission">
            <strong>사회적 가치</strong>
            <span>
              장애인 및 사회적 약자가 만든 상품을
              <br />
              협약 기관과 함께 엄선해 소개합니다.
            </span>
          </div>
        </div>
        <small>© 2026 이음옥션 | 사업자등록번호 000-00-00000 | 고객센터 1500-0000 (평일 10:00–18:00)</small>
      </div>
    </footer>
  )
}
