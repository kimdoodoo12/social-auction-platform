package com.socialauction.backend.category.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.socialauction.backend.category.admin.CategoryResponse;
import com.socialauction.backend.category.entity.CategoryEntity;

public interface CategoryRepository extends JpaRepository<CategoryEntity, Integer>{
    
    @Query(value = "SELECT " 
    + "category.category_id, category.name, COUNT(product_id) "
    + "FROM products LEFT JOIN category "
    + "on products.category_id = category.category_id "
    + "GROUP BY category_id", nativeQuery = true)
    List<CategoryResponse> findAllCategoryWithCounts();
}
