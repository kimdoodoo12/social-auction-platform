package com.socialauction.backend.products.entity;

import com.socialauction.backend.category.entity.CategoryEntity;
import com.socialauction.backend.global.BaseTime;
import com.socialauction.backend.organization.entity.OrganizationEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table (name = "products")
@AllArgsConstructor @NoArgsConstructor @Data @Builder
public class ProductEntity extends BaseTime{

    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Integer productId;
    private String name;

    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private OrganizationEntity organizationEntity;

    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn (name = "category_id")
    private CategoryEntity categoryEntity;

    private Integer startPrice;
    private String description;
    private String background;

}
