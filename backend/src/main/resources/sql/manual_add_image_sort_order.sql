-- MySQL 8: 기존 DB 보존용 수동 적용 스크립트. 자동 실행 대상이 아님.
-- 앱의 쓰기를 중지하고 백업한 뒤 적용한다. ddl-auto=create-drop으로 재시작하지 않는다.
-- 아래 두 조회 결과가 모두 0행인지 먼저 확인한다. 결과가 있으면 먼저 데이터를 정리한다.
SELECT p.product_id FROM products p LEFT JOIN image i ON i.product_id = p.product_id
GROUP BY p.product_id HAVING COUNT(i.image_id) = 0;
SELECT product_id FROM image GROUP BY product_id HAVING COUNT(*) > 5;

-- sort_order가 없는 기존 스키마에 한 번만 실행한다. DDL은 자동 커밋되므로
-- 전체 파일이 하나의 트랜잭션으로 롤백되는 것은 아니다.
ALTER TABLE image ADD COLUMN sort_order INT NULL;
CREATE TEMPORARY TABLE image_order_backfill AS
SELECT image_id, ROW_NUMBER() OVER (PARTITION BY product_id ORDER BY image_id) AS position
FROM image;
UPDATE image i JOIN image_order_backfill b ON b.image_id = i.image_id
SET i.sort_order = b.position;
DROP TEMPORARY TABLE image_order_backfill;
ALTER TABLE image
    MODIFY sort_order INT NOT NULL,
    ADD CONSTRAINT uk_image_product_order UNIQUE (product_id, sort_order),
    ADD CONSTRAINT ck_image_sort_order CHECK (sort_order BETWEEN 1 AND 5);
