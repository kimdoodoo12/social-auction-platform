# 이음 옥션 (IEUM Auction)

> 당신의 입찰이 누군가의 내일이 됩니다.

장애인과 사회적 약자가 만든 상품을 협약 기관을 통해 엄선해 판매하는 **사회적 가치 상품 경매 플랫폼**입니다.
2026년 2학기 고급웹프로그래밍 5조 팀 프로젝트 (2026.09 ~ 2026.10)

## 기술 스택

| 영역 | 사용 기술 |
| --- | --- |
| Frontend | React 19, React Router 7, Vite 8 (React Compiler) |
| Backend | Java 17, Spring Boot 4.1, Spring Data JPA, Validation |
| 인증·보안 | JWT (Access 쿠키) + Redis Refresh Token, AES 개인정보 암호화, BCrypt |
| 실시간 | SSE (Server-Sent Events) 현재가 방송 |
| DB | MySQL (`ieum`), Redis |

## 경매 규칙

일반적인 경매와 달리 **경매 시작 시각을 관리자가 정하지 않습니다.**

1. 상품은 관리자만 등록하며, 등록 직후 상태는 `경매 대기`입니다.
2. **첫 입찰이 들어온 순간부터 24시간** 카운트다운이 시작됩니다.
3. 이후 입찰은 현재 최고가보다 최소 1,000원 높아야 합니다.
4. 종료 시 최고 입찰자가 낙찰되고 결제(payment) 레코드가 생성됩니다.

> 시연을 위해 코드상 종료 시간은 첫 입찰 후 3분으로 설정되어 있습니다 (`UserAuctionService`).

## 주요 기능

**사용자**
- 메인 / 카탈로그: 인기·신규·마감 임박 경매, 카테고리·검색
- 경매 상세: SSE로 현재가·입찰 내역 실시간 갱신, 입찰 확인 모달
- 관심 기반 추천: 조회 기록에 최근성 가중치(18시간마다 40% 감쇠)를 반영한 관심 점수
- 회원가입 / 로그인 / 마이페이지

**관리자**
- 대시보드: 상품·경매 상태별·회원·협약 기관 수, 최근 등록 상품·최근 낙찰
- 상품 / 경매 / 회원 / 기관(협약서 파일 업로드) / 카테고리 관리

## 아키텍처 포인트

### 입찰 동시성 처리
여러 사용자가 동시에 입찰해도 가격 검증이 꼬이지 않도록 입찰과 종료 명령을 하나의 큐로 직렬화합니다.

```
입찰 요청 ─┐
          ├─▶ AuctionQueue (ArrayBlockingQueue) ─▶ 워커 스레드
종료 요청 ─┘         │                               │
                (가득 차면 QueueFullException)    경매별 ReentrantLock
                                                     │
                                       검증·저장 ─▶ 커밋 후 SSE 방송
```

- `BlockingQueue.take()`로 대기해 busy-wait 없이 명령을 꺼냅니다.
- `ConcurrentHashMap<auctionId, ReentrantLock>`로 **경매 단위**로만 잠가 서로 다른 경매는 병렬 처리합니다.
- SSE 방송은 트랜잭션 커밋 이후 이벤트 리스너에서 보내 롤백된 입찰이 화면에 나가지 않습니다.

### 경매 종료 스케줄링과 복구
- 첫 입찰 시 `TaskScheduler`에 종료 작업을 예약합니다 (`AuctionScheduler`).
- `UserAuctionService → AuctionScheduler → AuctionQueue → UserAuctionService` 순환 참조를 피하려고 종료 로직을 `ClosingAuctionService`로 분리했습니다.
- 서버가 재시작되면 메모리의 예약 작업이 사라지므로, 기동 시 `AuctionRecovery`가 진행 중인 경매를 다시 예약하거나 이미 지난 경매는 즉시 종료합니다.

### 인증
- 로그인 시 Access·Refresh Token을 쿠키로 내려주고, Refresh Token은 Redis에도 7일간 저장해 `/reissue`에서 대조합니다.
- 입찰자 ID는 클라이언트 값이 아닌 검증된 토큰에서 꺼내 사용합니다.
- 로그인 아이디·이름 등 개인정보는 AES로 암호화해 저장하고, 비밀번호는 BCrypt로 해시합니다.

## 프로젝트 구조

```
backend/   Spring Boot (도메인별 패키지: auction, bid, products, member, organization, category, dashboard, payment, interest, global)
frontend/  React + Vite (pages/user, pages/admin)
```

## 실행 방법

사전 준비: JDK 17, Node.js, MySQL, Redis (Spring 기본 접속 설정 localhost:6379 사용)

```bash
# 1. DB 생성
#    application.properties의 접속 주소가 jdbc:mysql://localhost:3306/ieum 이므로 ieum 데이터베이스가 있어야 합니다.

# 2. backend/src/main/resources/application.properties 에서 DB 계정, jwt.secret, aes.secret 설정
#    (ddl-auto=create-drop, 기동 시 sql/sample.sql 샘플 데이터 적재)

# 3. 백엔드 (http://localhost:8080)
cd backend
./gradlew bootRun

# 4. 프론트엔드 (http://localhost:5173, /ieum·/api 등은 8080으로 프록시)
cd frontend
npm install
npm run dev
```

## 팀 구성

| 이름 | 담당 |
| --- | --- |
| 김두현 (팀장) | 프로젝트 총괄, 일정 관리 |
| 최윤성 | 상품 관리, 관심 기반 추천 |
| 서현욱 | 회원·인증(JWT/Redis), AES 암호화 |
| 안현준 | 경매·입찰 로직, 입찰 큐, SSE |

## 한계와 향후 과제

- PortOne PG 결제 미연동 (낙찰 시 결제 레코드만 생성)
- 경매 종료 시간을 시연용 3분에서 24시간으로 되돌리기
- 단일 서버 메모리 큐·스케줄러 기반이라 다중 인스턴스 확장 시 분산 락/메시지 큐 필요
