# 사용자 경매 화면과 SSE 점검

이 폴더의 `preview.html`은 기존 라우터를 바꾸지 않고 Vite 개발 서버에서 화면을 확인하기 위한 진입점입니다.

1. 프런트 개발 서버를 실행합니다: `npm run dev`
2. 백엔드를 `http://localhost:8080`에서 실행합니다.
3. `http://localhost:5173/src/pages/user/preview.html?auctionId=7`을 엽니다. 실제 존재하는 경매 ID로 바꿀 수 있습니다.
4. 상단 점검 도구에서 SSE 연결 상태와 최근 `priceUpdate` 이벤트를 확인합니다.
5. 입찰 확인 모달에서 확정하려면 먼저 일반 회원으로 로그인해야 합니다. 계정의 회원 번호는 세션 조회 결과에서 가져옵니다.

`auctionApi.js`는 백엔드 경매 조회·입찰·SSE 경계입니다. `useAuctionLive.js`는 최초 조회와 SSE 구독을 담당하며, `AuctionDetailPage.jsx`는 화면과 입찰 확인을 담당합니다. 백엔드 이벤트 이름 또는 필드가 바뀌면 API 경계와 구독 훅을 수정합니다.

`POST /ieum/user/auction/sse/Bid`의 JSON `true`는 입찰 성공, `false`는 입찰 거절로 표시합니다. 성공 응답 후에도 같은 회원 번호와 입찰가의 `priceUpdate` 이벤트를 별도로 확인합니다. 가격·입찰 횟수·입찰자 수·상태·종료 시각·최근 입찰 내역은 SSE 이벤트로 갱신하며, 입찰 결과와 이벤트 확인 시간 초과 뒤에도 자동 재조회하지 않습니다. 8초 동안 내 입찰 이벤트가 확인되지 않으면 SSE 수신 건수가 늘었는지에 따라 미수신과 값 불일치를 구분해 표시합니다. JSON boolean이 아닌 응답은 계약 오류로 처리합니다.

SSE 상태와 최근 수신 가격·회원 번호는 정식 화면 하단에도 표시합니다. 연결 오류, 이벤트 미수신, 이벤트 수신 후 현재 입찰과 값이 다른 경우를 구분해 확인할 수 있습니다. `priceUpdate`에는 `price`, `bidderName`(회원 ID), `bidCount`, `bidMemberCount`, `status`, `endTime`, `recentBids`가 필요합니다. `recentBids`는 최근 5건 전체를 교체하며 각 항목은 최초 입찰 내역 조회와 같은 형식입니다. 재연결 중 놓친 이벤트는 자동 복구하지 않습니다.

현재 백엔드의 첫 입찰 최소 금액은 `topPrice + 1,000원`입니다. 화면은 이 동작에 맞춥니다.

기존 앱에 정식 경로를 연결할 때는 `App.jsx`에서 `AuctionRoutePage`를 가져와 `/user/auction/:auctionId` 경로에 등록하면 됩니다. 이 파일은 해당 라우터를 수정하지 않습니다.

회귀 확인: `npx eslint src/pages/user`, 경매 ID 변경, 대기·진행·종료 화면, 새 입찰의 SSE 가격·횟수·내역·종료 시각 갱신, 입찰 결과에 따른 자동 재조회가 없는지, 로그아웃 상태의 입찰 차단, 잘못된 경매 ID, SSE 연결 해제·재연결을 확인합니다.
