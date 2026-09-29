package com.socialauction.backend.products.user.service;

import org.springframework.stereotype.Service;

import com.socialauction.backend.products.user.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class ProductService {
    private final ProductRepository productRepository;
}
