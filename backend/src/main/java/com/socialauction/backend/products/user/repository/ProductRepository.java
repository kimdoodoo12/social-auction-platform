package com.socialauction.backend.products.user.repository;

import java.util.List;

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
        AND p.category_id IS NOT NULL
        GROUP BY p.category_id
        """, nativeQuery = true)
    List<ProductRecommendDto> findCount(
        @Param("memberId") Integer memberId
    );

    // 추천 상품을 출력하기 위한거
    // 입력받은 categoryId를 가지고 있는 상품 1개를 무작위로 가져옴
    @Query(value = """
        SELECT
            p.product_id AS productId,
            a.auction_status AS auctionStatus,
            i.image AS image,
            o.name AS organizationName,
            p.name AS productName,
            COALESCE(bs.current_price, p.start_price) AS currentPrice,
            COALESCE(bs.bid_count, 0) AS bidCount
        FROM products p
        LEFT JOIN organization o
            ON o.organization_id = p.organization_id
        LEFT JOIN auction a
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
        LEFT JOIN image i
            ON i.product_id = p.product_id
            AND i.image_id = (
                SELECT min(img.image_id)
                FROM image img
                WHERE img.product_id = p.product_id
            )
        where (:categoryId is null or p.category_id = :categoryId)
        ORDER BY RAND()
        LIMIT 1
        """, nativeQuery = true)
    ProductDto findRecommend(
        @Param ("categoryId") Integer categoryId
    );
    


}
