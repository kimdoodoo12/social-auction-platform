package com.socialauction.backend.organization.dto.admin;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder
@AllArgsConstructor @NoArgsConstructor 
public class OrganizationDetailResponse {
    
    // 기관 상세정보 응답 dto
    OrganizationInfoResponse organizationInfoResponse;

    // 기관 등록된 상품 dto
    List<OrganizationProductResponse> organizationProductResponse;
}
