import { BASE_URL } from '../../api/client'

const auctionPath = '/ieum/user/auction'

async function readJson(path, signal) {
  const response = await fetch(`${BASE_URL}${path}`, {
    credentials: 'include',
    signal,
  })
  if (!response.ok) throw new Error(`조회 실패 (HTTP ${response.status})`)
  return response.json()
}

export const userAuctionApi = {
  getAuction: (auctionId, signal) => readJson(`${auctionPath}/${auctionId}`, signal),
  getProduct: (auctionId, signal) => readJson(`${auctionPath}/product/${auctionId}`, signal),
  getBids: (auctionId, signal) => readJson(`${auctionPath}/bid?id=${auctionId}`, signal),
  subscribe(auctionId) {
    // * 이 SSE 경로는 와일드카드 CORS이며 쿠키 인증을 사용하지 않는다.
    return new EventSource(`${BASE_URL}/api/sse/${auctionId}/subscribe`)
  },
  async placeBid({ auctionId, memberId, price }) {
    const response = await fetch(`${BASE_URL}${auctionPath}/sse/Bid`, {
      method: 'POST',
      credentials: 'include',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ auctionId, memberId, price }),
    })
    if (!response.ok) throw new Error(`입찰 요청 실패 (HTTP ${response.status})`)
    const body = await response.text()
    let accepted
    try {
      accepted = JSON.parse(body)
    } catch {
      throw new Error('입찰 응답을 해석하지 못했습니다.')
    }
    if (typeof accepted !== 'boolean') throw new Error('입찰 응답이 boolean 형식이 아닙니다.')
    return accepted
  },
}
