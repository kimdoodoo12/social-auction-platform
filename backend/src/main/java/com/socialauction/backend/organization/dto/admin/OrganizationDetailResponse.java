package com.socialauction.backend.organization.dto.admin;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder
@AllArgsConstructor @NoArgsConstructor 
public class OrganizationDetailResponse {
    
    OrganizationInfoResponse organizationInfoResponse;
    List<OrganizationProductResponse> organizationProductResponse;
}
