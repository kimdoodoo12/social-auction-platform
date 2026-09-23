DROP DATABASE IF EXISTS ieum;
CREATE DATABASE IF NOT EXISTS ieum;
USE ieum;

CREATE TABLE member (
  member_id INT PRIMARY KEY AUTO_INCREMENT,
  login_id VARCHAR(30) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  name VARCHAR(30) NOT NULL,
  email VARCHAR(255) NOT NULL,
  phone VARCHAR(255),
  CREATED_AT DATETIME NOT NULL,
  UPDATED_AT DATETIME,
  status VARCHAR(20), -- 회원상태(정지, 정상, 잠금, 탈퇴)
  is_active BOOLEAN ,-- 관리자 여부
  locked_At DateTime
  # 입찰내역, 낙찰내역은 입찰로그 JOIN하여 조회
);
  
CREATE TABLE category (
  category_id INT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(30) NOT NULL
);
CREATE TABLE organization(
  organization_id INT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(30) NOT NULL,
  description VARCHAR(255),
  address VARCHAR(255) NOT NULL,
  manager VARCHAR(30) NOT NULL,
  manager_phone VARCHAR(30) NOT NULL,
  agreement_date DATETIME,
  agreement_status BOOLEAN, -- 계약종료, 계약기간
  agreement_file VARCHAR(255), -- 파일 경로, 위치
  organization_image VARCHAR(255), -- 기관 이미지
  agreement_info VARCHAR(255), -- 협약정보
  business_registration VARCHAR(20) NOT NULL -- 사업자 등록번호
  -- 등록상품은 JOIN
);
CREATE TABLE products (
  product_id INT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(30) NOT NULL,
  organization_id INT NOT NULL,
  CONSTRAINT fk_products_organization FOREIGN KEY(organization_id)
    REFERENCES organization(organization_id)
    ON UPDATE CASCADE,
  # 기관삭제는 보통 하지 않는 것으로
  category_id INT NOT NULL,
  CONSTRAINT fk_products_category FOREIGN KEY(category_id)
    REFERENCES category(category_id)
    ON UPDATE CASCADE,
  # 카테고리삭제는 보통하지 않는 것으로
  start_price INT NOT NULL,
  decription VARCHAR(255),
  background VARCHAR(255),
  CREATED_AT DATETIME,
  UPDATE_AT DATETIME
);

CREATE TABLE auction(
  auction_id INT PRIMARY KEY AUTO_INCREMENT,
  product_id INT NOT NULL,
  CONSTRAINT fk_auction_products FOREIGN KEY(product_id)
    REFERENCES products(product_id)
    ON UPDATE CASCADE,
    # 상품삭제는 없다.
  # start_price INT, -- 상품테이블에서 시작가격은 JOIN 가능
  current_price INT,
  start_time DATETIME,
  end_time DATETIME,
  auction_status VARCHAR(30) -- 대기상태, 진행상태, 완료상태 -- BOOLEAN으로 처리하고 NULL이면 경매대기상태?
  # payment_status BOOLEAN -- 결제상태, 미결제 상태 -- 결제상태는 JOIN가능
  # 입찰기록은 join
);

CREATE TABLE bid(
  bid_id INT PRIMARY KEY AUTO_INCREMENT,
  auction_id INT NOT NULL,
  CONSTRAINT fk_bid_auction FOREIGN KEY(auction_id)
    REFERENCES auction(auction_id)
    ON UPDATE CASCADE,
  member_id INT NOT NULL,
  CONSTRAINT fk_bid_member FOREIGN KEY(member_id)
    REFERENCES member(member_id)
    ON UPDATE CASCADE,
  bid_price INT NOT NULL,
  # bid_status VARCHAR(30), -- 낙찰대기, 낙찰이전, 최종낙찰, 이건 없어도 가능
  bid_time DATETIME -- 입찰한 시간 (추적 필요)
);

CREATE TABLE payment(
  payment_id INT PRIMARY KEY AUTO_INCREMENT,
  auction_id INT NOT NULL,
  CONSTRAINT fk_payment_auction FOREIGN KEY(auction_id)
    REFERENCES auction(auction_id)
    ON UPDATE CASCADE,
  member_id INT NOT NULL,
  CONSTRAINT fk_payment_member FOREIGN KEY(member_id)
    REFERENCES member(member_id)
    ON UPDATE CASCADE,
  payment_price INT NOT NULL,
  payment_status BOOLEAN  -- 결제, 미결제
);