package com.socialauction.backend.products.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity 
@Table (name = "image")
public class ImageEntity {
    @Id 
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Integer image_id;

    @ManyToOne 
    @JoinColumn (name = "product_id")
    private ProductEntity product_id;

    private String image;
}
