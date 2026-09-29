package com.socialauction.backend.organization.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder 
@NoArgsConstructor @AllArgsConstructor 
public class OrganizationSearchRequest {
    private String name;
    private Boolean agreementStatus;
}
