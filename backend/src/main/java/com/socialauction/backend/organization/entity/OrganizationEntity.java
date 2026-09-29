package com.socialauction.backend.organization.entity;

import java.time.LocalDateTime;

import com.socialauction.backend.organization.dto.admin.OrganizationInfoRequest;
import com.socialauction.backend.organization.dto.admin.OrganizationInfoResponse;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity @Table(name = "organization")
@Getter @Setter @ToString @Builder 
@AllArgsConstructor @NoArgsConstructor 
public class OrganizationEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer organizationId;

    @Column 
    private String name;

    @Column
    private String description;

    @Column
    private String address;

    @Column
    private String manager;

    @Column
    private String managerPhone;

    @Column
    private LocalDateTime agreementDate;

    @Column
    private Boolean agreementStatus;

    @Column
    private String agreementFile;

    @Column
    private String organizationImage;

    @Column 
    private String agreementInfo;

    @Column
    private String businessRegistration;
    
    public void updateOrganization(OrganizationInfoRequest oInfoRequest){
        this.name = oInfoRequest.getName();
        this.description = oInfoRequest.getDescription();
        this.address = oInfoRequest.getAddress();
        this.manager = oInfoRequest.getManager();
        this.managerPhone = oInfoRequest.getManagerPhone();
        this.agreementDate = oInfoRequest.getAgreementDate();
        this.agreementStatus = oInfoRequest.getAgreementStatus();
        this.agreementFile = oInfoRequest.getAgreementFile();
        this.organizationImage = oInfoRequest.getOrganizationImage();
        this.agreementInfo = oInfoRequest.getAgreementInfo();
        this.businessRegistration = oInfoRequest.getBusinessRegistration();
    }
}
