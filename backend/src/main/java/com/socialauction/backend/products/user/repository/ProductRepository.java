package com.socialauction.backend.products.user.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestMapping;

import com.socialauction.backend.products.entity.ProductEntity;
import com.socialauction.backend.products.user.dto.ProductDto;
import com.socialauction.backend.products.user.dto.ProductRecommendDto;

import lombok.RequiredArgsConstructor;

@Repository("userProductRepository") 
public interface ProductRepository extends JpaRepository<ProductEntity, Integer>{

    // 현재 로그인한 사용자의 카테고리별 입찰 횟수 확인
    @Query(value = """
        SELECT
            p.category_id AS categoryId,
            COUNT(b.bid_id) AS bidCount
        FROM bid b
        JOIN auction a
            ON a.auction_id = b.auction_id
        JOIN products p
            ON p.product_id = a.product_id
        WHERE b.member_id = :memberId
        GROUP BY p.category_id
        """, nativeQuery = true)
    List<ProductRecommendDto> findCount(
        @Param("memberId") Long memberId
    );

    // 추천 상품을 출력하기 위한거
    // 입력받은 categoryId를 가지고 있는 상품 1개를 무작위로 가져옴
    @Query(value = """
        SELECT
            a.auction_status AS auctionStatus,
            i.image AS image,
            o.name AS organizationName,
            p.name AS productName,
            CAST(COALESCE(bs.current_price, p.start_price) AS SIGNED) AS currentPrice, /* 반환 타입을 맞추기 위해서 cast ... as signed 사용 */
            CAST(COALESCE(bs.bid_count, 0) AS SIGNED) AS bidCount
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
        where (:categoryId is null or p.category_id = :categoryId)
        ORDER BY RAND()
        LIMIT 1
        """, nativeQuery = true)
    ProductDto findRecommend(
        @Param ("categoryId") Integer categoryId
    );
    
    
    // 상품 조회
    @Query(value = """
        SELECT
            a.auction_status AS auctionStatus,
            i.image AS image,
            o.name AS organizationName,
            p.name AS productName,
            CAST(COALESCE(bs.current_price, p.start_price) AS SIGNED) AS currentPrice,
            CAST(COALESCE(bs.bid_count, 0) AS SIGNED) AS bidCount
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
            (:type = 'popular' AND bs.bid_count > 1)
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
            CAST(COALESCE(bs.current_price, p.start_price) AS SIGNED) AS currentPrice,
            CAST(COALESCE(bs.bid_count, 0) AS SIGNED) AS bidCount
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
        and (
            (:lowprice is null and :highprice is null) /* 가격 필터링 */
            or (
                coalesce(bs.current_price, p.start_price) /* 현재가가 있으면 현재가, 없으면 시작가 */
                between :lowprice and :highprice
            )
        )
        ORDER BY
            case when :sort = 'popular' /* 인기순 */
                then bs.bid_count end desc,
            case when :sort = 'time' /* 마감시간 순 추후에 작업 */
                then bs.bid_count end desc,
            case when :sort = 'new'  /* 최신순 */
                then p.created_at end desc,
            case when :sort = 'lowprice'  /* 가격 낮은 순 */
                then coalesce(bs.current_price, p.start_price) end asc,
            case when :sort = 'highprice' /* 가격 높은 순 */
                then coalesce(bs.current_price, p.start_price) end desc,
            p.product_id desc /* 조건이 같다면 상품번호 내림차순 */
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


    @Query(value="""
                SELECT COUNT(product_id) FROM products;
            """, nativeQuery = true)
    Integer findProductCount();
}
