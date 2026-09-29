package com.socialauction.backend.products.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestMapping;

import com.socialauction.backend.products.entity.ProductEntity;

import lombok.RequiredArgsConstructor;

@Repository 
public interface ProductRepository extends JpaRepository<ProductEntity, Integer>{

    
}
