package com.socialauction.backend.products.user.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.socialauction.backend.category.entity.CategoryEntity;
import com.socialauction.backend.category.repository.CategoryRepository;
import com.socialauction.backend.interest.repository.InterestRepository;
import com.socialauction.backend.interest.service.InterestService;
import com.socialauction.backend.products.user.dto.ProductDto;
import com.socialauction.backend.products.user.dto.ProductRecommendDto;
import com.socialauction.backend.products.user.dto.RecommendDto;
import com.socialauction.backend.products.user.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service("userProductService") 
@RequiredArgsConstructor 
public class ProductService {
    private final ProductRepository productRepository;
    private final InterestService interestService;
    private final CategoryRepository categoryRepository;
    private final InterestRepository interestRepository;
    
    // 추천 상품 조회
    public List<ProductDto> findRecommendProduct(Long memberId) {
        List<ProductRecommendDto> recommendDtos =
            productRepository.findCount(memberId);

        List<Integer> selectedCategoryIds;

        if (!recommendDtos.isEmpty()) {
            // 입찰 이력 있음: 입찰 횟수 기반 카테고리 추첨
            selectedCategoryIds = selectCategoryIds(recommendDtos);
        } else if (interestRepository.existsByMemberEntity_MemberId(memberId)) {
            // 조회 이력만 있음: 카테고리 동일 확률 추첨
            selectedCategoryIds = selectRandomCategoryIds();
        } else {
            // 둘 다 없음: 전체 추천 가능 상품에서 랜덤 4개
            return productRepository.findRandomProducts();
        }

        // DB 조회에 전달할 때만 카테고리 중복 제거
        List<Integer> categoryIds = selectedCategoryIds.stream()
            .distinct()
            .toList();

        // 상품 후보와 회원 관심 정보 조회
        List<RecommendDto> candidates =
            productRepository.findRecommendCandidates(memberId, categoryIds);

        // 카테고리 추첨 순서대로 중복 없는 상품 선택
        List<RecommendDto> selectedProducts =
            selectProducts(selectedCategoryIds, candidates);

        List<ProductDto> result = new ArrayList<>();

        for (RecommendDto selected : selectedProducts) {
            ProductDto product = ProductDto.builder()
                .auctionStatus(selected.getAuctionStatus())
                .image(selected.getImage())
                .organizationName(selected.getOrganizationName())
                .ProductName(selected.getProductName())
                .currentPrice(selected.getCurrentPrice())
                .endTime(selected.getEndTime())
                .bidCount(selected.getBidCount())
                .build();

            result.add(product);
        }

        return result;
    }

    // 전체 카테고리 중 동일 확률로 4번 추첨
    private List<Integer> selectRandomCategoryIds() {
        List<CategoryEntity> categories = categoryRepository.findAll();
        List<Integer> selectedCategoryIds = new ArrayList<>();

        for (int i = 0; i < 4; i++) {
            int randomIndex = (int) (Math.random() * categories.size());

            selectedCategoryIds.add(
                categories.get(randomIndex).getCategoryId()
            );
        }

        return selectedCategoryIds;
    }

    // 입찰 횟수를 가중치로 카테고리 4번 추첨
    private List<Integer> selectCategoryIds(
            List<ProductRecommendDto> recommendDtos) {

        List<Integer> selectedCategoryIds = new ArrayList<>();

        int total = 0;
        for (ProductRecommendDto dto : recommendDtos) {
            total += dto.getCount();
        }

        for (int i = 0; i < 4; i++) {
            int random = (int) (Math.random() * total) + 1;
            int cumulativeCount = 0;

            for (ProductRecommendDto dto : recommendDtos) {
                cumulativeCount += dto.getCount();

                if (random <= cumulativeCount) {
                    selectedCategoryIds.add(dto.getCategoryId());
                    break;
                }
            }
        }

        return selectedCategoryIds;
    }

    // 카테고리 추첨 순서대로 중복 없는 상품 선택
    private List<RecommendDto> selectProducts(
            List<Integer> selectedCategoryIds,
            List<RecommendDto> candidates) {

        List<RecommendDto> selectedProducts = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (Integer categoryId : selectedCategoryIds) {
            List<RecommendDto> categoryCandidates = new ArrayList<>();

            for (RecommendDto candidate : candidates) {
                // 이번에 선택한 카테고리가 아니면 제외
                if (!categoryId.equals(candidate.getCategoryId())) {
                    continue;
                }

                // 이미 선택한 상품인지 ID 비교
                boolean duplicated = false;

                for (RecommendDto selected : selectedProducts) {
                    if (candidate.getProductId()
                            .equals(selected.getProductId())) {
                        duplicated = true;
                        break;
                    }
                }

                if (!duplicated) {
                    categoryCandidates.add(candidate);
                }
            }

            RecommendDto selected = selectProduct(categoryCandidates, now);
            selectedProducts.add(selected);
        }

        return selectedProducts;
    }

    // 후보 중 상품 하나를 관심 점수로 추첨
    private RecommendDto selectProduct(
            List<RecommendDto> candidates,
            LocalDateTime now) {

        List<Double> weights = new ArrayList<>();
        double totalWeight = 0;

        for (RecommendDto candidate : candidates) {
            double weight;

            if (candidate.getLastViewedAt() == null) {
                // 미조회 상품: 쿼리에서 가져온 2점
                weight = candidate.getInterestScore();
            } else {
                // 조회 상품: 최근성만 반영
                weight = interestService.calculateCurrentScore(
                    candidate.getInterestScore(),
                    candidate.getLastViewedAt(),
                    now
                );
            }

            weights.add(weight);
            totalWeight += weight;
        }

        // 모든 가중치가 0이면 동일 확률로 추첨
        if (totalWeight == 0) {
            int index = (int) (Math.random() * candidates.size());
            return candidates.get(index);
        }

        RecommendDto selected = null;

        // 마지막 후보를 강제로 반환하지 않고, 선택될 때까지 추첨
        while (selected == null) {
            double random = Math.random() * totalWeight;
            double cumulativeWeight = 0;

            for (int i = 0; i < candidates.size(); i++) {
                cumulativeWeight += weights.get(i);

                if (random < cumulativeWeight) {
                    selected = candidates.get(i);
                    break;
                }
            }
        }

        return selected;
    }

    // 마감 임박 상품 조회
    public List<ProductDto> findEndProduct( ) {
        Pageable pageable = PageRequest.of(0, 4, Sort.by("a.end_time"));
        return productRepository.findProduct("ending", pageable).getContent();
    }
    
    // 인기 상품 조회
    public List<ProductDto> findPopularProduct( ) {
        Pageable pageable = PageRequest.of(0, 4, Sort.by("bs.bid_count").descending());
        return productRepository.findProduct("popular", pageable).getContent();
    }
    
    // 새로 등록된 상품 조회
    public List<ProductDto> findNewProduct( ) {
        Pageable pageable = PageRequest.of(0, 4, Sort.by("p.created_at").descending());
        return productRepository.findProduct("new", pageable).getContent();
    }

    // 상품 목록 조회
    public Page<ProductDto> findList(
        Pageable pageable,
        String productName, Integer organizationId, String auctionStatus,
        Integer categoryId, Integer lowprice, Integer highprice, String sort
    ) {
        Page<ProductDto> list = productRepository.findProductList(
            productName, organizationId, auctionStatus,
            categoryId, lowprice, highprice, sort, pageable);
        return list;
    }
}
