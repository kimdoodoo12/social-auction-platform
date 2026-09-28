package com.socialauction.backend.category.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.socialauction.backend.category.admin.CategoryResponse;
import com.socialauction.backend.category.entity.CategoryEntity;
import com.socialauction.backend.category.service.CategoryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/ieum/admin/category")
@RequiredArgsConstructor 
public class CategoryController {
    
    private final CategoryService cService;

    @GetMapping("")
    public List<CategoryResponse> findAll(){
        return cService.findAllCategoryWithProductCount();
    }

    @PostMapping("")
    public boolean save(@RequestBody String name){
        return cService.save(name);
    }
    
    @PutMapping("")
    public boolean update(@RequestBody CategoryEntity cEntity){
        return cService.update(cEntity);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable("id") int id){
        cService.delete(id);
    }
}
