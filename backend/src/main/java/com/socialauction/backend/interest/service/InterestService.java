package com.socialauction.backend.interest.service;

import java.time.Duration;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.socialauction.backend.interest.entity.InterestEntity;
import com.socialauction.backend.interest.repository.InterestRepository;
import com.socialauction.backend.member.entity.MemberEntity;
import com.socialauction.backend.products.entity.ProductEntity;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InterestService {

    private final InterestRepository interestRepository;

    // 상품 상세 조회 시 관심 기록 저장
    @Transactional
    public void recordView(
            MemberEntity memberEntity,
            ProductEntity productEntity) {

        LocalDateTime now = LocalDateTime.now();

        InterestEntity interest = interestRepository
            .findByMemberEntity_MemberIdAndProductEntity_ProductId(
                memberEntity.getMemberId(),
                productEntity.getProductId()
            )
            .orElse(null);

        // 첫 조회
        if (interest == null) {
            interest = InterestEntity.builder()
                .memberEntity(memberEntity)
                .productEntity(productEntity)
                .interestScore(5.0)
                .viewCount(1)
                .lastViewedAt(now)
                .build();

            interestRepository.save(interest);
            return;
        }

        // 마지막 조회 이후 경과 시간
        long elapsedMinutes = Duration.between(
            interest.getLastViewedAt(), now
        ).toMinutes();

        // 기존 점수에 최근성 반영
        double score = calculateCurrentScore(
            interest.getInterestScore(),
            interest.getLastViewedAt(),
            now
        );

        // 5분 이상 지난 경우에만 추가 점수 부여
        if (elapsedMinutes >= 5) {
            int nextViewCount = interest.getViewCount() + 1;

            score += getAdditionalScore(nextViewCount);
            interest.setViewCount(nextViewCount);
        }

        interest.setInterestScore(score);
        interest.setLastViewedAt(now);

        // 조회한 엔티티는 트랜잭션 종료 시 변경 내용이 반영됨
    }

    // 조회 횟수에 따른 추가 점수
    private double getAdditionalScore(int viewCount) {
        return switch (viewCount) {
            case 1 -> 5.0;
            case 2 -> 9.0;
            case 3 -> 5.0;
            case 4 -> 3.0;
            default -> 2.0;
        };
    }

    // 마지막 조회부터 18시간마다 관심 점수의 40% 감소
    public double calculateCurrentScore(
            double score,
            LocalDateTime lastViewedAt,
            LocalDateTime now) {

        long elapsedHours = Math.max(
            0, Duration.between(lastViewedAt, now).toHours()
        );

        long decreaseCount = elapsedHours / 18;

        return score * Math.pow(0.6, decreaseCount);
    }
}
