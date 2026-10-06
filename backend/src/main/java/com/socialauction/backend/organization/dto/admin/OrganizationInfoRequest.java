package com.socialauction.backend.organization.dto.admin;

import java.time.LocalDateTime;

import org.springframework.web.multipart.MultipartFile;

import com.socialauction.backend.organization.entity.OrganizationEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder 
@NoArgsConstructor @AllArgsConstructor 
public class OrganizationInfoRequest {
    
    private Integer organizationId;
    private String name;
    private String description;
    private String address;
    private String manager;
    private String managerPhone;
    private LocalDateTime agreementDate;
    private Boolean agreementStatus;
    private String agreementFileName;
    private MultipartFile agreementFile;
    private String organizationImageFileName;
    private MultipartFile organizationImageFile;
    private String agreementInfo;
    private String businessRegistration;

    public OrganizationEntity toEntity(){
        return OrganizationEntity.builder()
            .organizationId(this.organizationId)
            .name(this.name)
            .description(this.description)
            .address(this.address)
            .manager(this.manager)
            .managerPhone(this.managerPhone)
            .agreementDate(this.agreementDate)
            .agreementStatus(this.agreementStatus)
            .agreementFileName(this.agreementFileName)
            .organizationImageFileName(this.organizationImageFileName)
            .agreementInfo(this.agreementInfo)
            .businessRegistration(this.businessRegistration)
            .build();
    }

    public static OrganizationInfoRequest from(OrganizationEntity entity){
        return OrganizationInfoRequest.builder()
            .organizationId(entity.getOrganizationId())
            .name(entity.getName())
            .description(entity.getDescription())
            .address(entity.getAddress())
            .manager(entity.getManager())
            .managerPhone(entity.getManagerPhone())
            .agreementDate(entity.getAgreementDate())
            .agreementStatus(entity.getAgreementStatus())
            .agreementFileName(entity.getAgreementFileName())
            .organizationImageFileName(entity.getOrganizationImageFileName())
            .agreementInfo(entity.getAgreementInfo())
            .businessRegistration(entity.getBusinessRegistration())
            .build();
    }
}
