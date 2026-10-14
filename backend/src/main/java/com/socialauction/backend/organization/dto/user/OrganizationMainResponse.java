package com.socialauction.backend.organization.dto.user;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor @NoArgsConstructor @Data @Builder 
public class OrganizationMainResponse {
    Integer organizationCount;
    List<OrganizationListResponse> organzationList;
}
