package com.socialauction.backend.products.entity;

import jakarta.persistence.Entity;
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
@Table (name = "image")
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
}
