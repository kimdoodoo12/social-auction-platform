package com.socialauction.backend.organization.dto.admin;

import java.time.LocalDateTime;

import com.socialauction.backend.organization.entity.OrganizationEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder 
@NoArgsConstructor @AllArgsConstructor 
public class OrganizationListResponse {
    private Integer organizationId;

    private String name;

    private String manager;

    private String managerPhone;

    private LocalDateTime agreementDate;

    private Boolean agreementStatus;

    // 기관에서의 등록된 상품
    private Long productCount;

}