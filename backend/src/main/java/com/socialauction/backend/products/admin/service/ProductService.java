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
import com.socialauction.backend.global.FileService;
import com.socialauction.backend.global.UploadFolder;
import com.socialauction.backend.organization.repository.OrganizationRepository;
import com.socialauction.backend.products.admin.dto.ImageDto;
import com.socialauction.backend.products.admin.dto.ProductAuctionInfo;
import com.socialauction.backend.products.admin.dto.ProductAuctionSummary;
import com.socialauction.backend.products.admin.dto.ProductDto;
import com.socialauction.backend.products.admin.dto.ProductListResponse;
import com.socialauction.backend.products.admin.dto.ProductManageDto;
import com.socialauction.backend.products.admin.dto.RecentBid;
import com.socialauction.backend.products.admin.dto.TotalDto;
import com.socialauction.backend.products.admin.repository.ImageRepository;
import com.socialauction.backend.products.admin.repository.ProductRepository;
import com.socialauction.backend.products.entity.ImageEntity;
import com.socialauction.backend.products.entity.ProductEntity;

import lombok.RequiredArgsConstructor;

@Service("adminProductService")
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final ImageRepository imageRepository;
    private final CategoryRepository categoryRepository;
    private final OrganizationRepository organizationRepository;
    private final AuctionRepository auctionRepository;
    private final FileService fileService;

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
    public TotalDto findDetail(Integer productId) {
        // 상품아이디로 상품 정보 가져오기
        ProductEntity productEntity = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "존재하지 않는 상품입니다."));
        // 상품아이디로 이미지들의 정보 가져오기
        List<ImageEntity> imageEntities = imageRepository.findByProductId(productId);
        
        // productEntity를 dto로 변환, 이미지 정보도 담기
        ProductDto productDto = ProductDto.from(productEntity, imageEntities);

        ProductAuctionInfo auctionInfo = productRepository.findAuctionInfo(productId);
        List<RecentBid> bidInfo = productRepository.findRecentBids(auctionInfo.getAuctionId());
        
        TotalDto totalDto = new TotalDto();
        totalDto.setProductAuctionInfo(auctionInfo);
        totalDto.setProductDto(productDto);
        totalDto.setRecentBid(bidInfo);

        return totalDto;
        
        
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

        validateNewImages(productDto.getImages());

        // 이미지 설정
        
        productDto.getImages().forEach(dto -> {
            if (dto.getFile() != null && !dto.getFile().isEmpty()) {
                String savedFileName = fileService.upload(UploadFolder.IMAGES, dto.getFile());
                ImageEntity imageEntity = ImageEntity.builder()
                        .image(savedFileName)
                        .sortOrder(dto.getSortOrder())
                        .productEntity(productEntity)
                        .build();
                productEntity.getImageEntity().add(imageEntity);
            }
        });

        // 저장
        ProductEntity savedEntity = productRepository.save(productEntity);
        AuctionEntity auctionEntity = new AuctionEntity();
        auctionEntity.setProductEntity(savedEntity);
        auctionEntity.setAuctionStatus("대기");
        auctionRepository.save(auctionEntity);
        
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

        updateImages(savedEntity, productDto.getImages());

        return true;
    }

    // 등록 시 대표 이미지 파일만 확인한다.
    private void validateNewImages(List<ImageDto> images) {
        boolean hasCover = images.stream().anyMatch(img ->
                Integer.valueOf(1).equals(img.getSortOrder())
                        && img.getFile() != null && !img.getFile().isEmpty());
        Checks.check(!hasCover, "1번 대표 이미지를 등록해주세요.");
    }

    private void updateImages(ProductEntity product, List<ImageDto> images) {
        if (images != null) {
            for (ImageDto dto : images) {
                if (dto == null || dto.getFile() == null || dto.getFile().isEmpty()) continue;
                ImageEntity target = null;
                if (dto.getImageId() != null) {
                    target = product.getImageEntity().stream()
                            .filter(image -> dto.getImageId().equals(image.getImageId()))
                            .findFirst()
                            .orElseThrow(() -> new ResponseStatusException(
                                    HttpStatus.BAD_REQUEST, "해당 상품의 이미지가 아닙니다."));
                }
                if (target != null) fileService.delete(UploadFolder.IMAGES, target.getImage());
                String fileName = fileService.upload(UploadFolder.IMAGES, dto.getFile());
                if (target == null) {
                    product.getImageEntity().add(ImageEntity.builder()
                            .image(fileName).sortOrder(dto.getSortOrder())
                            .productEntity(product).build());
                } else {
                    target.setImage(fileName);
                }
            }
        }
        boolean hasCover = product.getImageEntity().stream()
                .anyMatch(image -> Integer.valueOf(1).equals(image.getSortOrder())
                        && image.getImage() != null && !image.getImage().isBlank());
        Checks.check(!hasCover, "1번 대표 이미지는 비울 수 없습니다.");
    }

    // 검색기능
    public Page<ProductManageDto> findProductManage(
        String productName, String organizationName, String auctionStatus, String categoryName, int page) {
        Pageable pageable = PageRequest.of(page, 8); // 페이징 조건을 객체로 만듦 (page: 조회할 페이지 번호, pagesize: 출력할 개수)
        return productRepository.findProductManage(productName, organizationName, auctionStatus, categoryName, pageable);
    }
} // service end
