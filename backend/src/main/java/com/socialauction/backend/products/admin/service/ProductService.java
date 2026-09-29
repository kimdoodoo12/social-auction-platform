package com.socialauction.backend.products.admin.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.socialauction.backend.auction.entity.AuctionEntity;
import com.socialauction.backend.auction.repository.AuctionRepository;
import com.socialauction.backend.category.repository.CategoryRepository;
import com.socialauction.backend.global.Checks;
import com.socialauction.backend.organization.repository.OrganizationRepository;
import com.socialauction.backend.products.admin.dto.ImageDto;
import com.socialauction.backend.products.admin.dto.ProductAuctionSummary;
import com.socialauction.backend.products.admin.dto.ProductDto;
import com.socialauction.backend.products.admin.dto.ProductListResponse;
import com.socialauction.backend.products.admin.dto.ProductManageDto;
import com.socialauction.backend.products.admin.repository.ImageRepository;
import com.socialauction.backend.products.admin.repository.ProductRepository;
import com.socialauction.backend.products.entity.ImageEntity;
import com.socialauction.backend.products.entity.ProductEntity;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final ImageRepository imageRepository;
    private final CategoryRepository categoryRepository;
    private final OrganizationRepository organizationRepository;
    private final AuctionRepository auctionRepository;

    // 상품 첫 화면(상품관리) 조회
    @Transactional(readOnly = true)
    public Page<ProductListResponse> findAll(Pageable pageable) {
        // 전체 가져오기
        Page<ProductEntity> products = productRepository.findAll(pageable);
        // 비어있으면 빈 배열 반환
        if (products.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, products.getTotalElements());
        }

        // productId만 저장하는 배열
        List<Integer> productIds = new ArrayList<>();
        products.getContent().forEach(product -> {
            productIds.add(product.getProductId());
        });

        // productId와 image로 맵 구성
        Map<Integer, String> images = new HashMap<>();
        imageRepository.findRepresentImg(productIds)
                .forEach(image -> {
                    images.put(
                            image.getProductEntity().getProductId(),
                            image.getImage()
                    );
                });
        
        // 경매(상태, 현재가) 정보 가져오기 
        Map<Integer, ProductAuctionSummary> auctions = new HashMap<>();
        productRepository.findAuctionSummaries(productIds).forEach(summary -> {
            auctions.put(summary.getProductId(), summary);
        });


        List<ProductListResponse> responses = new ArrayList<>();
        // page정보 빼고 product정보만 꺼내오는거
        products.getContent().forEach(product -> {
            // id와 맞는 auction 정보
            ProductAuctionSummary auction = auctions.get(product.getProductId());
            // 검증 후 현재가 or 시작가 반환
            Integer currentPrice = auction != null && auction.getCurrentPrice() != null
                    ? auction.getCurrentPrice() : product.getStartPrice();
            // 검증 후 현재상태 or "경매대기" 반환
            String status = auction != null ? auction.getStatus() : "경매 대기";
            // 각각 넣어서 배열에 추가
            responses.add(ProductListResponse.from(
                    product, images.get(product.getProductId()), currentPrice, status));
        });
        // 추가한 배열들 반환
        return new PageImpl<>(responses, pageable, products.getTotalElements());
    }

    // 상품 상세 - 기본정보,상품설명, 상품이미지 조회
    @Transactional(readOnly = true)
    public ProductDto findDetail(Integer productId) {
        // 상품아이디로 상품 정보 가져오기
        ProductEntity productEntity = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "존재하지 않는 상품입니다."));
        // 상품아이디로 이미지들의 정보 가져오기
        List<ImageEntity> imageEntities = imageRepository.findByProductId(productId);
        
        // productEntity를 dto로 변환, 이미지 정보도 담기
        ProductDto productDto = ProductDto.from(productEntity, imageEntities);
        
        return productDto;
        
        
    }

    // 상태 이력

    // 판매 중지
    @Transactional
    public boolean stopSelling(Integer productId) {
        AuctionEntity auctionEntity = auctionRepository.findByProductEntity_ProductId(productId);
        auctionEntity.setAuctionStatus("판매 중지");
        return true;
    }

    // 상품 등록
    @Transactional
    public boolean saveProduct(ProductDto productDto) {
        // 상품명과 시작가 검증
        Checks.check(productDto.getName() == null || productDto.getName().isBlank(),
                "상품명을 입력해주세요.");
        Checks.check(productDto.getName().length() > 30,
                "상품명은 30자 이하로 입력해주세요.");
        Checks.check(productDto.getStartPrice() == null || productDto.getStartPrice() < 0,
                "시작가는 0 이상의 금액을 입력해주세요.");

        // 상품 설명과 배경 길이 확인
        Checks.check(productDto.getDescription() != null
                && productDto.getDescription().length() > 255,
                "상품 설명은 255자 이하로 입력해주세요.");
        Checks.check(productDto.getBackground() != null
                && productDto.getBackground().length() > 255,
                "상품 배경은 255자 이하로 입력해주세요.");

        ProductEntity productEntity = productDto.toEntity();

        // 기관, 카테고리를 선택했는지 확인
        Checks.check(productDto.getOrganizationId() == null
                || productDto.getCategoryId() == null,
                "기관과 카테고리를 설정해주세요.");

        // 기관 설정
        productEntity.setOrganizationEntity(
                organizationRepository.findById(productDto.getOrganizationId())
                        .orElseThrow(() -> new ResponseStatusException( // 예외 처리
                                HttpStatus.BAD_REQUEST, "존재하지 않는 기관입니다.")));
                            
        // 카테고리 설정
        productEntity.setCategoryEntity(
                categoryRepository.findById(productDto.getCategoryId())
                        .orElseThrow(() -> new ResponseStatusException( // 예외 처리
                                HttpStatus.BAD_REQUEST, "존재하지 않는 카테고리입니다.")));

        // 이미지 설정
        if (productDto.getImages() != null) { // 비어있는지 확인
            for (ImageDto imageDto : productDto.getImages()) {
                Checks.check(imageDto == null, "이미지 정보를 입력해주세요.");
                String imagePath = imageDto.getImage();
                // 이미지 경로 확인
                Checks.check(imagePath == null || imagePath.isBlank(),
                        "유효한 이미지 경로가 아닙니다.");
                ImageEntity imageEntity = ImageEntity.builder()
                        .image(imagePath) // 받아온 경로를 설정
                        .productEntity(productEntity) // 상품 번호 설정
                        .build();

                productEntity.getImageEntity().add(imageEntity); // 상품에 연결
            }
        }

        // 저장
        ProductEntity savedEntity = productRepository.save(productEntity);
        return savedEntity.getProductId() >= 1;
    }

    // 상품 수정
    @Transactional
    public boolean updateProduct(ProductDto productDto) {
        // 상품번호가 있는지 확인
        Checks.check(productDto.getProductId() == null,
                "상품번호를 입력해주세요.");
        // 기관, 카테고리를 선택했는지 확인
        Checks.check(productDto.getOrganizationId() == null
                || productDto.getCategoryId() == null,
                "기관과 카테고리를 설정해주세요.");

        // 상품명과 시작가 확인
        Checks.check(productDto.getName() == null || productDto.getName().isBlank(),
                "상품명을 입력해주세요.");
        Checks.check(productDto.getName().length() > 30,
                "상품명은 30자 이하로 입력해주세요.");
        Checks.check(productDto.getStartPrice() == null || productDto.getStartPrice() < 0,
                "시작가는 0 이상의 금액을 입력해주세요.");

        // 상품 설명과 배경 길이 확인
        Checks.check(productDto.getDescription() != null
                && productDto.getDescription().length() > 255,
                "상품 설명은 255자 이하로 입력해주세요.");
        Checks.check(productDto.getBackground() != null
                && productDto.getBackground().length() > 255,
                "상품 배경은 255자 이하로 입력해주세요.");

        // 받아온 상품번호로 엔티티 조회
        ProductEntity savedEntity = productRepository.findById(productDto.getProductId())
                .orElseThrow( () -> new ResponseStatusException( // 예외 처리
                        HttpStatus.BAD_REQUEST, "존재하지 않는 상품번호입니다."
                ));

        // 기존 이미지와 새 이미지의 개수 확인
        if (productDto.getImages() != null) { // 이미지가 있는지 확인
            int newImageCount = 0;
            for (ImageDto imageDto : productDto.getImages()) {
                // 이미지 정보 확인
                Checks.check(imageDto == null, "이미지 정보를 입력해주세요.");
                // 이미지 id가 없으면 새 이미지로 계산
                if (imageDto.getImageId() == null) {
                    newImageCount++;
                }
            }
            // 기존 이미지를 교체하는 경우는 개수가 늘어나지 않음
            Checks.check(savedEntity.getImageEntity().size() + newImageCount > 5,
                    "상품 이미지는 최대 5개까지 등록할 수 있습니다.");
        }

        // 받아온 Dto를 Entity로 변환
        ProductEntity productEntity = productDto.toEntity();
        
        // setter 이용해서 수정
        savedEntity.setName(productEntity.getName() );
        savedEntity.setStartPrice(productEntity.getStartPrice() );
        savedEntity.setDescription(productEntity.getDescription() );
        savedEntity.setBackground(productEntity.getBackground() );

        // 기관 정보 변경
        savedEntity.setOrganizationEntity(
                // 받아온 기관 id로 기관 정보를 받아옴
                organizationRepository.findById(productDto.getOrganizationId() )
                .orElseThrow( () -> new ResponseStatusException( // 예외 처리
                        HttpStatus.BAD_REQUEST, "존재하지 않는 기관 id입니다."
                ))
        );

        // 카테고리 변경
        savedEntity.setCategoryEntity( // 위랑 동일한 방식
                categoryRepository.findById(productDto.getCategoryId() )
                .orElseThrow( () -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "존재하지 않는 카테고리 id입니다."
                ))
        );

        // 이미지 변경
        if (productDto.getImages() != null) { // 이미지가 있는지 확인
            for (ImageDto imageDto : productDto.getImages()) {
                // 이미지 정보 확인
                Checks.check(imageDto == null, "이미지 정보를 입력해주세요.");
                String imagePath = imageDto.getImage();
                // 이미지 경로 확인
                Checks.check(imagePath == null || imagePath.isBlank(),
                        "유효한 이미지 경로가 아닙니다.");
                // 빈 이미지 변경
                if (imageDto.getImageId() == null) {
                    ImageEntity imageEntity = ImageEntity.builder()
                            .image(imagePath)
                            .productEntity(savedEntity)
                            .build();
                    savedEntity.getImageEntity().add(imageEntity);
                } else { // 기존 이미지 변경
                    ImageEntity imageEntity = imageRepository.findById(imageDto.getImageId())
                            .orElseThrow(() -> new ResponseStatusException( // 예외 처리
                                    HttpStatus.BAD_REQUEST, "존재하지 않는 이미지입니다."));
                    // imageId가 잘못 왔을 경우를 대비해서 확인            
                    Checks.check(!savedEntity.getProductId().equals(
                            imageEntity.getProductEntity().getProductId()),
                            "해당 상품의 이미지가 아닙니다.");
                    // 적용    
                    imageEntity.setImage(imagePath);
                }
            }
        }

        return true;
    }

    // 검색기능
    public Page<ProductManageDto> findProductManage(
        String productName, String organizationName, String auctionStatus, String categoryName, int page) {
        Pageable pageable = PageRequest.of(page, 8); // 페이징 조건을 객체로 만듦 (page: 조회할 페이지 번호, pagesize: 출력할 개수)
        return productRepository.findProductManage(productName, organizationName, auctionStatus, categoryName, pageable);
    }
} // service end
