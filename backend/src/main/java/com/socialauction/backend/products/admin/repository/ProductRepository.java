package com.socialauction.backend.products.admin.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.socialauction.backend.products.admin.dto.ProductAuctionInfo;
import com.socialauction.backend.products.admin.dto.ProductAuctionSummary;
import com.socialauction.backend.products.admin.dto.ProductDto;
import com.socialauction.backend.products.admin.dto.ProductListResponse;
import com.socialauction.backend.products.admin.dto.ProductManageDto;
import com.socialauction.backend.products.admin.dto.RecentBid;
import com.socialauction.backend.products.entity.ProductEntity;

@Repository("adminProductRepository")
public interface ProductRepository extends JpaRepository<ProductEntity, Integer> {
    @Query(value = """
        SELECT
            p.product_id AS productId,
            i.image AS image,
            p.name AS productName,
            o.name AS organizationName,
            p.start_price AS startPrice,
            COALESCE(bs.current_price, p.start_price) AS currentPrice,
            a.auction_status AS status,
            p.created_at AS createdAt
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
                MAX(bid_price) AS current_price
            FROM bid
            GROUP BY auction_id
        ) bs
            ON bs.auction_id = a.auction_id
        """,
        countQuery = """
        SELECT COUNT(*)
        FROM products p
        JOIN organization o
            ON o.organization_id = p.organization_id
        JOIN auction a
            ON a.product_id = p.product_id
        JOIN image i
            ON i.product_id = p.product_id
            AND i.sort_order = 1
        """,
        nativeQuery = true)
    Page<ProductListResponse> findProductList(Pageable pageable);
    // productId, 상태,현재가 가져오기
    @Query("""
            select a.productEntity.productId as productId,
                   a.auctionStatus as status,
                   max(b.bidPrice) as currentPrice
            from AuctionEntity a
            left join a.bidList b
            where a.productEntity.productId in :productIds
            group by a.productEntity.productId, a.auctionId, a.auctionStatus
            """)
    List<ProductAuctionSummary> findAuctionSummaries(
            @Param("productIds") List<Integer> productIds);


    // 검색 기능을 위한것
    @Query (value = """
            select p.product_id, i.image, p.name, o.name, p.start_price,
            max(b.bid_price), a.auction_status, p.created_at
            from products p
            join auction a                  /* 경매 테이블 조인 */
                on p.product_id = a.product_id
            left join bid b                      /* 입찰 테이블 조인 */
                on b.auction_id = a.auction_id
            join organization o             /* 기관 테이블 조인 */
                on p.organization_id = o.organization_id
            join image i                    /* 이미지 테이블 조인 */
                on p.product_id = i.product_id
                /* 이미지 번호가 제일 낮은 이미지 한개만 가져옴 */
                AND i.sort_order = 1
            join category c                 /* 카테고리 테이블 조인 */
                on p.category_id = c.category_id
            where p.name like concat('%', :productName, '%') /* 검색 결과(상품이름)을 포함하는 정보만 찾음 */
            /* 각종 필터링 적용 */
            and (
                :organizationName is null or
                :organizationName = o.name
            )
            and (
                :auctionStatus is null or
                :auctionStatus = a.auction_status
            )
            and (
                :categoryName is null or
                :categoryName = c.name
            )
            
            group by p.product_id, i.image, p.name, o.name, p.start_price,
            a.auction_status, p.created_at
            /* 상품 번호를 기준으로 내림차순 정렬 */
            order by p.product_id desc 
            """, 
            /* 페이징 처리를 위해 검색 개수를 계산 */
            countQuery = """
                select count(*)
                from products p
                join organization o
                    on p.organization_id = o.organization_id
                join auction a
                    on p.product_id = a.product_id
                join category c
                    on p.category_id = c.category_id
                join image i
                    on i.product_id = p.product_id
                    and i.sort_order = 1
                where p.name like concat('%', :productName, '%')
                and (
                    :organizationName is null or
                    :organizationName = o.name
                )
                and (
                    :auctionStatus is null or
                    :auctionStatus = a.auction_status
                )
                and (
                    :categoryName is null or
                    :categoryName = c.name
                )
                """,
            nativeQuery = true) // sql문을 사용하기 위해서 적용
            // jpql문 - 엔티티명과 자바 필드명 사용
            // sql문 - DB 테이블명, 컬럼명 사용
    Page<ProductManageDto> findProductManage(
        @Param ("productName") String productName,
        @Param ("organizationName") String organizationName,
        @Param ("auctionStatus") String auctionStatus,
        @Param ("categoryName") String categoryName,
        Pageable pageable);

    // 가격 경매 정보
    @Query(value = """
        SELECT
            a.auction_id AS auctionId,
            p.start_price AS startPrice,
            COALESCE(MAX(b.bid_price), p.start_price) AS currentPrice,
            COUNT(b.bid_id) AS bidCount,
            COUNT(DISTINCT b.member_id) AS bidderCount,
            a.start_time AS startTime,
            a.end_time AS endTime
        FROM products p
        JOIN auction a
            ON a.product_id = p.product_id
        LEFT JOIN bid b
            ON b.auction_id = a.auction_id
        WHERE p.product_id = :productId
        GROUP BY
            a.auction_id,
            p.start_price,
            a.start_time,
            a.end_time
        """, nativeQuery = true)
    ProductAuctionInfo findAuctionInfo(
        @Param("productId") Integer productId
    );
	
    // 최근 입찰 내역
    @Query(value = """
        SELECT
            ROW_NUMBER() OVER (
                ORDER BY b.bid_time ASC, b.bid_id ASC
            ) AS bidNumber,
            COUNT(*) OVER () AS totalCount,
            b.member_id AS memberId,
            m.name AS memberName,
            b.bid_price AS bidPrice,
            b.bid_time AS bidTime
        FROM bid b
        JOIN member m
            ON m.member_id = b.member_id
        WHERE b.auction_id = :auctionId
        ORDER BY b.bid_time DESC, b.bid_id DESC
        LIMIT 5
        """, nativeQuery = true)
    List<RecentBid> findRecentBids(
        @Param("auctionId") Integer auctionId
    );
        
}
