package com.socialauction.backend.category.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.socialauction.backend.category.admin.CategoryResponse;
import com.socialauction.backend.category.entity.CategoryEntity;
import com.socialauction.backend.category.repository.CategoryRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor 
public class CategoryService {
    
    private final CategoryRepository cRepository;

    public List<CategoryResponse>findAllCategoryWithProductCount(){

        return cRepository.findAllCategoryWithCounts();
    }

    public boolean save(String name){
        CategoryEntity cEntity = new CategoryEntity(null, name);
        cRepository.save(cEntity);

        if(cEntity.getCategoryId() >= 1){
            return true;
        }
        return false;
    }

    @Transactional
    public boolean update(CategoryEntity cEntity){

        Optional<CategoryEntity> entity = cRepository.findById(cEntity.getCategoryId());
        if(entity.isPresent()){
            entity.get().setName(cEntity.getName());
            return true;
        }
        return false;
    }

    public void delete(int id){
        
        CategoryEntity entity = cRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("can't find category"));

        cRepository.delete(entity);
    }
}
