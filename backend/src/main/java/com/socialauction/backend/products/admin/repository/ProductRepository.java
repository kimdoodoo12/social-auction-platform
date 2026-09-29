package com.socialauction.backend.products.admin.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.socialauction.backend.products.admin.dto.ProductAuctionSummary;
import com.socialauction.backend.products.admin.dto.ProductDto;
import com.socialauction.backend.products.admin.dto.ProductManageDto;
import com.socialauction.backend.products.entity.ProductEntity;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, Integer> {

    @Override
    // organizationEntity가 lazy설정이므로 먼저 정보를 받아올 수 있게 EntityGraph 어노테이션이 필요
    @EntityGraph(attributePaths = "organizationEntity")
    Page<ProductEntity> findAll(Pageable pageable);

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
            left join auction a                  /* 경매 테이블 조인 */
                on p.product_id = a.product_id
            left join bid b                      /* 입찰 테이블 조인 */
                on b.auction_id = a.auction_id
            left join organization o             /* 기관 테이블 조인 */
                on p.organization_id = o.organization_id
            left join image i                    /* 이미지 테이블 조인 */
                on p.product_id = i.product_id
                /* 이미지 번호가 제일 낮은 이미지 한개만 가져옴 */
                and i.image_id = (
                    select min(i2.image_id)
                    from image i2
                    where i2.product_id = p.product_id
                )
            left join category c                 /* 카테고리 테이블 조인 */
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
                :categoryName = c.category_name
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
                left join organization o
                    on p.organization_id = o.organization_id
                left join auction a
                    on p.product_id = a.product_id
                left join category c
                    on p.category_id = c.category_id
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
                    :categoryName = c.category_name
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
    @Query (value = """
                select
                    p.start
            """, nativeQuery = true)

	
	
        
}
