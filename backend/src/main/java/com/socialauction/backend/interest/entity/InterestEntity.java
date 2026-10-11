package com.socialauction.backend.interest.entity;

import java.time.LocalDateTime;

import com.socialauction.backend.member.entity.MemberEntity;
import com.socialauction.backend.products.entity.ProductEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "interest",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_interest_member_product",
            columnNames = {"member_id", "product_id"}
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer interestId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private MemberEntity memberEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity productEntity;

    // 마지막 조회 시점의 관심 점수
    @Column(nullable = false)
    private Double interestScore;

    // 유효 조회 횟수
    @Column(nullable = false)
    private Integer viewCount;

    // 마지막 조회 시각
    @Column(nullable = false)
    private LocalDateTime lastViewedAt;
}
