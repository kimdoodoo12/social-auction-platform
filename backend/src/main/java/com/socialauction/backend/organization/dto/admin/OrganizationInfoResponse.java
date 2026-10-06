package com.socialauction.backend.organization.dto.admin;

import java.time.LocalDateTime;

import org.springframework.web.multipart.MultipartFile;

import com.socialauction.backend.global.UploadFolder;
import com.socialauction.backend.global.UploadUrls;
import com.socialauction.backend.organization.entity.OrganizationEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder 
@NoArgsConstructor @AllArgsConstructor 
public class OrganizationInfoResponse {
    
    private Integer organizationId;
    private String name;
    private String description;
    private String address;
    private String manager;
    private String managerPhone;
    private LocalDateTime agreementDate;
    private Boolean agreementStatus;
    private String agreementFileName;
    private String organizationImageFileName;
    private String agreementInfo;
    private String businessRegistration;

    public OrganizationEntity toEntity(){
        return OrganizationEntity.builder()
            .organizationId(this.organizationId)
            .name(this.name)
            .description(this.description)
            .address(this.address)
            .manager(this.address)
            .managerPhone(this.managerPhone)
            .agreementDate(this.agreementDate)
            .agreementStatus(this.agreementStatus)
            .agreementFileName(this.agreementFileName)
            .organizationImageFileName(this.organizationImageFileName)
            .agreementInfo(this.agreementInfo)
            .businessRegistration(this.businessRegistration)
            .build();
    }

    public static OrganizationInfoResponse from(OrganizationEntity entity){
        return OrganizationInfoResponse.builder()
            .organizationId(entity.getOrganizationId())
            .name(entity.getName())
            .description(entity.getDescription())
            .address(entity.getAddress())
            .manager(entity.getManager())
            .managerPhone(entity.getManagerPhone())
            .agreementDate(entity.getAgreementDate())
            .agreementStatus(entity.getAgreementStatus())
            .agreementFileName(entity.getAgreementFileName())
            // 상품 이미지처럼 조회 가능한 URL(/organization/파일명)로 내려준다
            .organizationImageFileName(UploadUrls.from(UploadFolder.ORGANIZATION, entity.getOrganizationImageFileName()))
            .agreementInfo(entity.getAgreementInfo())
            .businessRegistration(entity.getBusinessRegistration())
            .build();
    }
}
