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

@Entity 
@Table (name = "products")
public class ProductEntity extends BaseTime{
    
    @Id 
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Integer product_id;
    private String name;
    
    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private OrganizationEntity organization_id;
    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn (name = "category_id")
    private CategoryEntity category_id;
    private Integer start_price;
    private String description;
    private String background;
    

}
