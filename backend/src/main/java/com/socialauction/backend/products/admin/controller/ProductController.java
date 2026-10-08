package com.socialauction.backend.products.admin.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

import com.socialauction.backend.products.admin.dto.ProductDto;
import com.socialauction.backend.products.admin.dto.ProductListResponse;
import com.socialauction.backend.products.admin.dto.ProductManageDto;
import com.socialauction.backend.products.admin.dto.TotalDto;
import com.socialauction.backend.products.admin.service.ProductService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;




@RestController("adminProductController")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@RequiredArgsConstructor
@RequestMapping ("/ieum/admin/product")
public class ProductController {
    private final ProductService productService;

    // 상품 첫 화면(상품관리) 조회
    @GetMapping("main") // 주소 추후에 설정
    public Page<ProductListResponse> findAll(
            @PageableDefault(size = 8, sort = {"createdAt", "productId"},
                    direction = Sort.Direction.DESC) Pageable pageable) {
        return productService.findProductList(pageable);
    }

    // 상품 상세 - 기본정보,상품설명, 상품이미지 조회
    @GetMapping("/detail/{productId}") // 주소 추후에 설정
    public TotalDto findDetail(@PathVariable ("productId") Integer productId) {
        return productService.findDetail(productId);
    }

    // 판매 중지 
    @PostMapping("stop")
    public boolean stopSelling( @RequestParam (name = "productId" ) Integer productId ) {
        return productService.stopSelling(productId);
    }
    


    // 상품 등록
    @PostMapping("add")
    public boolean saveProduct(@ModelAttribute ProductDto productDto) {
        return productService.saveProduct(productDto);
    }
    
    // 상품 수정
    @PutMapping("update")
    public boolean updateProduct(@ModelAttribute ProductDto productDto) {
        return productService.updateProduct(productDto);
    }

    // 검색 기능
    @GetMapping("search")
    public Page<ProductManageDto> findProductManage(
        @RequestParam(name = "productName", defaultValue = "") String productName,          // 상품 이름 (기본값을 공백으로 설정)
        // required = false : 요청을 받지 않아도 되게 설정( 받지 않을 경우 null )
        @RequestParam(name = "organizationName", required = false) String organizationName, // 기관 이름 
        @RequestParam(name = "auctionStatus", required = false) String auctionStatus,       // 경매 상태
        @RequestParam(name = "categoryName", required = false) String categoryName,         // 카테고리 이름
        @RequestParam(name = "page", defaultValue = "0") int page                           // 페이지 기본값을 0으로 설정
        ) {
            return productService.findProductManage(productName, organizationName, auctionStatus, categoryName, page);
        }
    
    
    
} // controller end
