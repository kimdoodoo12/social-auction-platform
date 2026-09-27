package com.socialauction.backend.products.entity;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringExclude;

import com.socialauction.backend.category.entity.CategoryEntity;
import com.socialauction.backend.global.BaseTime;
import com.socialauction.backend.organization.entity.OrganizationEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

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

    @OneToMany (mappedBy = "productEntity", cascade = CascadeType.ALL)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude // 무한재귀방지
    @Builder.Default 
    private List<ImageEntity> imageEntity = new ArrayList<>();

    private Integer startPrice;
    private String description;
    private String background;

}
