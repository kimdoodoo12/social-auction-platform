package com.socialauction.backend.interest.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.socialauction.backend.interest.entity.InterestEntity;

public interface InterestRepository
        extends JpaRepository<InterestEntity, Integer> {

    // 해당 회원의 특정 상품 관심 기록 조회
    Optional<InterestEntity>
        findByMemberEntity_MemberIdAndProductEntity_ProductId(
            Long memberId,
            Integer productId
        );

    // 해당 회원의 상품 조회 이력 존재 여부
    boolean existsByMemberEntity_MemberId(Long memberId);
}
