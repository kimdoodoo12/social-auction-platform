package com.socialauction.backend.products.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.socialauction.backend.products.entity.ImageEntity;

@Repository
public interface ImageRepository extends JpaRepository<ImageEntity, Integer> {

    // 상품마다 이미지 번호가 가장 작은 이미지를 대표 이미지로 사용한다.
    @Query("""
            select i from ImageEntity i
            join fetch i.productEntity
            where i.productEntity.productId in :productIds
              and i.image_id = (
                  select min(other.image_id) from ImageEntity other
                  where other.productEntity = i.productEntity
              )
            """)
    List<ImageEntity> findRepresentImg(
            @Param("productIds") List<Integer> productIds);

    
    // 이미지 여러개 찾기
    List<ImageEntity> findByProductId(Integer productId);
}
