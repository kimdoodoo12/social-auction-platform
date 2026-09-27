package com.socialauction.backend.category.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.socialauction.backend.category.entity.CategoryEntity;

@Repository 
public interface CategoryRepository extends JpaRepository<CategoryEntity, Integer>{

    
} 
