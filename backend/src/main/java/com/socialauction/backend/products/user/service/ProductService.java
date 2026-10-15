package com.socialauction.backend.products.user.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.socialauction.backend.global.PageResponse;
import com.socialauction.backend.interest.service.InterestService;
import com.socialauction.backend.organization.dto.user.OrganizationMainResponse;
import com.socialauction.backend.organization.repository.OrganizationRepository;
import com.socialauction.backend.products.user.dto.ProductDto;
import com.socialauction.backend.products.user.dto.RecommendDto;
import com.socialauction.backend.products.user.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service("userProductService") 
@RequiredArgsConstructor 
public class ProductService {
    private final ProductRepository productRepository;
    private final InterestService interestService;
    private final OrganizationRepository organizationRepository;
   
    // 상품 4개를 관심 점수로 추첨
    public List<ProductDto> findRecommendProduct(
            Long memberId,
            LocalDateTime now) {

        // 경매상태가 종료가 아닌 상품들의 상품정보와 추천점수 정보를 가져옴
        List<RecommendDto> candidates =
        productRepository.findRecommendCandidates(memberId);
    
        // 가중치
        List<Double> weights = new ArrayList<>();
        double totalWeight = 0;

        // 가중치 부여
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
            weight = Math.max(2.0, weight); // 가중치가 2점 미만으로 내려가는 것을 방지
            weights.add(weight);
            totalWeight += weight;
        }


        // 반환할 리스트 선언
        List<ProductDto> list = new ArrayList<>();
        // 4개 뽑을때까지 반복
        while (list.size() < 4 && !candidates.isEmpty()) {
            RecommendDto selected = null;
            double random = Math.random() * totalWeight;
            double cumulativeWeight = 0;

            // 랜덤으로 1개 뽑기
            for (int i = 0; i < candidates.size(); i++) {
                cumulativeWeight += weights.get(i);
                if (random < cumulativeWeight) {
                    // 선택한 상품을 가져오면서 후보에서 제거
                    selected = candidates.remove(i);

                    // 해당 상품의 가중치도 제거하고 총합에서 차감
                    double selectedWeight = weights.remove(i);
                    totalWeight -= selectedWeight;

                    break;
                }
            }
            if( selected != null ) {
                // ProductDto 타입으로 변환
                ProductDto dto = new ProductDto();
                dto.setAuctionId(selected.getAuctionId());
                dto.setAuctionStatus(selected.getAuctionStatus());
                dto.setBidCount(selected.getBidCount());
                dto.setCurrentPrice(selected.getCurrentPrice());
                dto.setEndTime(selected.getEndTime());
                dto.setImage(selected.getImage());
                dto.setOrganizationName(selected.getOrganizationName());
                dto.setProductId(selected.getProductId());
                dto.setProductName(selected.getProductName());

                // 반환할 리스트에 추가
                list.add(dto);
            }
        }
        return list;
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
        Pageable pageable = PageRequest.of(0, 4, Sort.by("created_at").descending());
        return productRepository.findProduct("new", pageable).getContent();
    }

    // 상품 목록 조회
    public PageResponse<ProductDto> findList(
        Pageable pageable,
        String productName, Integer organizationId, String auctionStatus,
        Integer categoryId, Integer lowprice, Integer highprice, String sort
    ) {
        Page<ProductDto> list = productRepository.findProductList(
            productName, organizationId, auctionStatus,
            categoryId, lowprice, highprice, sort, pageable);
        Page<ProductDto> response = list;
        return new PageResponse<>(response);
    }

    // 메인페이지 기관목록과 개수 조회
    public OrganizationMainResponse findOrganizationMainResponse(){
        return OrganizationMainResponse.builder()
            .organzationList(organizationRepository.findOrganizationNameList())
            .organizationCount(organizationRepository.findOrganizationCount())
            .build();
    }
}
