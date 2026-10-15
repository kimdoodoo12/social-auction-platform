package com.socialauction.backend.global;

import java.util.List;

import org.springframework.data.domain.Page;

import lombok.Getter;

@Getter 
public class PageResponse<T> {
    private List<T> content;
    private int page;
    private long totalElements;
    private int totalPages;

    public PageResponse(Page<T> pageData){
        this.content = pageData.getContent();
        this.page = pageData.getNumber();
        this.totalElements = pageData.getTotalElements();
        this.totalPages = pageData.getTotalPages();
    }
}
