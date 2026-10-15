package com.socialauction.backend.products.user.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.socialauction.backend.products.entity.ProductEntity;
import com.socialauction.backend.products.user.dto.ProductDto;
import com.socialauction.backend.products.user.dto.RecommendDto;


@Repository("userProductRepository") 
public interface ProductRepository extends JpaRepository<ProductEntity, Integer>{

    // 대기·진행 중인 전체 추천 후보와 회원 관심 정보 조회
    @Query(value = """
        SELECT
            p.product_id AS productId,
            a.auction_id AS auctionId,
            a.auction_status AS auctionStatus,
            i.image AS image,
            o.name AS organizationName,
            p.name AS productName,
            CAST(COALESCE(bs.current_price, p.start_price) AS SIGNED) AS currentPrice, /* 반환 타입을 Long(BIGINT)으로 고정 */
            a.end_time AS endTime,
            COALESCE(bs.bid_count, 0) AS bidCount,
            COALESCE(it.interest_score, 2.0) AS interestScore,
            it.last_viewed_at AS lastViewedAt
        FROM products p
        JOIN organization o
            ON o.organization_id = p.organization_id
        JOIN auction a
            ON a.product_id = p.product_id
        JOIN image i
            ON i.product_id = p.product_id
            AND i.sort_order = 1
        LEFT JOIN (
            SELECT
                auction_id,
                MAX(bid_price) AS current_price,
                COUNT(*) AS bid_count
            FROM bid
            GROUP BY auction_id
        ) bs
            ON bs.auction_id = a.auction_id
        LEFT JOIN interest it
            ON it.product_id = p.product_id
            AND it.member_id = :memberId
            WHERE a.auction_status IN ('대기', '진행')
        """, nativeQuery = true)
    List<RecommendDto> findRecommendCandidates(
        @Param("memberId") Long memberId
    );

    // 상품 조회
    @Query(value = """
        SELECT
            a.auction_status AS auctionStatus,
            i.image AS image,
            o.name AS organizationName,
            p.name AS productName,
            CAST(COALESCE(bs.current_price, p.start_price) AS SIGNED) AS currentPrice, /* 반환 타입을 Long(BIGINT)으로 고정 */
            a.end_time AS endTime,
            COALESCE(bs.bid_count, 0) AS bidCount,
            p.product_id AS productId,
            a.auction_id AS auctionId
        FROM products p
        JOIN organization o
            ON o.organization_id = p.organization_id
        JOIN auction a
            ON a.product_id = p.product_id
            /* 입찰횟수 */
        LEFT JOIN (
            SELECT
                auction_id,
                max(bid_price) AS current_price,
                count(*) AS bid_count
            FROM bid
            GROUP BY auction_id
        ) bs
            ON bs.auction_id = a.auction_id
        JOIN image i
            ON i.product_id = p.product_id
            AND i.sort_order = 1
        WHERE
            (:type = 'new' AND bs.auction_id IS NULL)
            OR
            (:type = 'popular' AND bs.bid_count > 1 AND a.auction_status IN ('대기', '진행') )
            OR
            (
                :type = 'ending'
                AND a.auction_status = '진행'
            )
        """, nativeQuery = true)
    Page<ProductDto> findProduct(@Param ("type") String type, Pageable pageable);
    // type은 (new, popular, ending)중 하나로 매개변수를 받고, 페이지 객체를 매개변수로 받는다.
    // 페이지로 받되 동적으로 LIMIT와 ORDER BY를 결정하기 위해 사용

    // 상품 목록
    @Query(value = """
        SELECT
            a.auction_status AS auctionStatus,
            i.image AS image,
            o.name AS organizationName,
            p.name AS productName,
            CAST(COALESCE(bs.current_price, p.start_price) AS SIGNED) AS currentPrice, /* 반환 타입을 Long(BIGINT)으로 고정 */
            a.end_time AS endTime,
            COALESCE(bs.bid_count, 0) AS bidCount,
            p.product_id AS productId,
            a.auction_id AS auctionId
        FROM products p
        JOIN organization o
            ON o.organization_id = p.organization_id
        JOIN auction a
            ON a.product_id = p.product_id
            /* 입찰횟수 */
        LEFT JOIN (
            SELECT
                auction_id,
                max(bid_price) AS current_price,
                count(*) AS bid_count
            FROM bid
            GROUP BY auction_id
        ) bs
            ON bs.auction_id = a.auction_id
        JOIN image i
            ON i.product_id = p.product_id
            AND i.sort_order = 1
        where p.name like concat('%', :productName, '%') /* 검색 결과(상품이름)을 포함하는 정보만 찾음 */
        and (
            :categoryId is null or p.category_id = :categoryId /* 카테고리 필터링 */
        )
        and (
            :auctionStatus is null or :auctionStatus = a.auction_status /* 경매 상태 필터링 */
        )
        and (
            :organizationId is null or :organizationId = p.organization_id /* 기관 필터링 */
        )
        and (:lowprice is null or COALESCE(bs.current_price, p.start_price) >= :lowprice)
        and (:highprice is null or COALESCE(bs.current_price, p.start_price) <= :highprice)
        ORDER BY
            case when :sort = 'popular' /* 인기순 */
                then bs.bid_count end desc,
            case when :sort = 'time' then
                case when a.auction_status = '진행' then 0 else 1 end end asc,
            case when :sort = 'time' then a.end_time end asc,
            case when :sort = 'new'  /* 최신순 */
                then p.created_at end desc,
            case when :sort = 'lowprice'  /* 가격 낮은 순 */
                then coalesce(bs.current_price, p.start_price) end asc,
            case when :sort = 'highprice' /* 가격 높은 순 */
                then coalesce(bs.current_price, p.start_price) end desc,
            p.product_id desc /* 조건이 같다면 상품번호 내림차순 */
        """, countQuery = """
        SELECT COUNT(*)
        FROM products p
        JOIN organization o
            ON o.organization_id = p.organization_id
        JOIN auction a
            ON a.product_id = p.product_id
            /* 입찰횟수 */
        LEFT JOIN (
            SELECT
                auction_id,
                max(bid_price) AS current_price,
                count(*) AS bid_count
            FROM bid
            GROUP BY auction_id
        ) bs
            ON bs.auction_id = a.auction_id
        JOIN image i
            ON i.product_id = p.product_id
            AND i.sort_order = 1
        where p.name like concat('%', :productName, '%') /* 검색 결과(상품이름)을 포함하는 정보만 찾음 */
        and (
            :categoryId is null or p.category_id = :categoryId /* 카테고리 필터링 */
        )
        and (
            :auctionStatus is null or :auctionStatus = a.auction_status /* 경매 상태 필터링 */
        )
        and (
            :organizationId is null or :organizationId = p.organization_id /* 기관 필터링 */
        )
        and (:lowprice is null or COALESCE(bs.current_price, p.start_price) >= :lowprice)
        and (:highprice is null or COALESCE(bs.current_price, p.start_price) <= :highprice)
        """, nativeQuery = true)
    Page<ProductDto> findProductList( 
        @Param ("productName") String productName,
        @Param ("organizationId") Integer organizationId,
        @Param ("auctionStatus") String auctionStatus,
        @Param ("categoryId") Integer categoryId,
        @Param ("lowprice") Integer lowprice,
        @Param ("highprice") Integer highprice,
        @Param ("sort") String sort,
        Pageable pageable );


    // 상품 전체 개수
    @Query(value="""
                SELECT COUNT(product_id) FROM products
            """, nativeQuery = true)
    Integer findProductCount();
}
