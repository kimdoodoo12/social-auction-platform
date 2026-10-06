package com.socialauction.backend.products.admin.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.socialauction.backend.products.entity.ImageEntity;

@Repository
public interface ImageRepository extends JpaRepository<ImageEntity, Integer> {

    // 1번 위치의 이미지를 대표 이미지로 사용한다.
    @Query("""
            select i from ImageEntity i
            join fetch i.productEntity
            where i.productEntity.productId in :productIds
              and i.sortOrder = 1
            """)
    List<ImageEntity> findRepresentImg(
            @Param("productIds") List<Integer> productIds);

    
    // 이미지 여러개 찾기
    @Query("""
    select i from ImageEntity i
    where i.productEntity.productId = :productId
    order by i.sortOrder asc
    """)
    List<ImageEntity> findByProductId(@Param("productId") Integer productId);
}
