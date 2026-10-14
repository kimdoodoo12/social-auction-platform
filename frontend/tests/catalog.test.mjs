import test from 'node:test'
import assert from 'node:assert/strict'
import { catalogParams, remainingClock } from '../src/pages/user/catalogHelpers.js'

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
  const result = catalogParams('productName=가방&categoryId=2&categoryId=3&auctionStatus=진행&auctionStatus=대기&organizationId=7&page=3&sortType=highprice')
  assert.equal(result.productName, '가방')
  assert.deepEqual(result.categoryIds, [2, 3])
  assert.equal(result.organizationId, 7)
  assert.equal(result.page, 3)
  assert.deepEqual(result.auctionStatuses, ['진행', '대기'])
  assert.equal(result.sortType, 'highprice')
})

test('다중 필터의 중복과 잘못된 값을 제외하고 빈 조건을 처리한다', () => {
  const result = catalogParams('categoryId=2&categoryId=2&categoryId=nope&categoryId=-1&categoryId=2147483648&auctionStatus=진행&auctionStatus=진행&auctionStatus=invalid')
  assert.deepEqual(result.categoryIds, [2])
  assert.deepEqual(result.auctionStatuses, ['진행'])
  assert.deepEqual(catalogParams('').categoryIds, [])
  assert.deepEqual(catalogParams('').auctionStatuses, [])
})

test('남은 시간을 시·분·초로 표시하고 만료·누락·24시간 초과를 처리한다', () => {
  const now = new Date('2026-10-13T12:00:00Z').getTime()
  assert.equal(remainingClock(null, now), '--:--:--')
  assert.equal(remainingClock('invalid', now), '--:--:--')
  assert.equal(remainingClock('2026-10-13T11:59:00Z', now), '00:00:00')
  assert.equal(remainingClock('2026-10-13T12:00:00Z', now), '00:00:00')
  assert.equal(remainingClock('2026-10-13T13:30:04Z', now), '01:30:04')
  assert.equal(remainingClock('2026-10-14T18:15:04Z', now), '30:15:04')
})
