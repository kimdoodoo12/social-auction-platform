package com.socialauction.backend.organization.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity @Table(name = "organization")
@Getter @Setter @ToString @Builder 
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
    private String manager_phone;

    @Column
    private Boolean agreement_status;

    @Column
    private String agreement_file;

    @Column
    private String organization_image;

    @Column 
    private String agreement_info;

    @Column
    private String businessRegistrationNumber;
    
}
