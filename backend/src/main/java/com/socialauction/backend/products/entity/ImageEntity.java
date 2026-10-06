package com.socialauction.backend.products.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Column;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "image", uniqueConstraints = @UniqueConstraint( // 중복 방지
        name = "uk_image_product_order", columnNames = {"product_id", "sort_order"}))
@NoArgsConstructor @AllArgsConstructor @Builder @Data 
public class ImageEntity {
    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Integer imageId;

    @ManyToOne
    @JoinColumn (name = "product_id")
    @EqualsAndHashCode.Exclude // 무한재귀방지
    private ProductEntity productEntity;

    private String image;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;
}
