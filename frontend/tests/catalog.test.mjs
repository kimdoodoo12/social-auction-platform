import test from 'node:test'
import assert from 'node:assert/strict'
import { catalogParams, remainingLabel } from '../src/pages/user/catalogHelpers.js'

test('한쪽 가격 필터와 0원 조건을 잃지 않는다', () => {
  assert.equal(catalogParams('lowprice=0').lowprice, 0)
  assert.equal(catalogParams('lowprice=15000').highprice, '')
  assert.equal(catalogParams('highprice=20000').lowprice, '')
})

test('잘못된 페이지·가격·정렬은 안전한 기본값을 사용한다', () => {
  const result = catalogParams('page=-1&lowprice=nope&highprice=2147483648&sortType=invalid')
  assert.equal(result.page, 0)
  assert.equal(result.lowprice, '')
  assert.equal(result.highprice, '')
  assert.equal(result.sortType, 'popular')
})

test('검색·카테고리·페이지 이동 조건을 URL에서 복원한다', () => {
  const result = catalogParams('productName=가방&categoryId=2&auctionStatus=진행&page=3&sortType=lowprice')
  assert.equal(result.productName, '가방')
  assert.equal(result.categoryId, 2)
  assert.equal(result.page, 3)
  assert.equal(result.auctionStatus, '진행')
  assert.equal(result.sortType, 'lowprice')
})

test('대기·종료·만료된 경매의 남은 시간을 구분한다', () => {
  const now = new Date('2026-10-13T12:00:00').getTime()
  assert.equal(remainingLabel(null, '대기', now), '첫 입찰을 기다리고 있어요')
  assert.equal(remainingLabel(null, '완료', now), '경매 종료')
  assert.equal(remainingLabel('2026-10-13T11:59:00', '진행', now), '마감 시간 도달')
  assert.equal(remainingLabel('2026-10-13T13:30:00', '진행', now), '1시간 30분 남음')
  assert.equal(remainingLabel('invalid', '진행', now), '진행 중')
})
