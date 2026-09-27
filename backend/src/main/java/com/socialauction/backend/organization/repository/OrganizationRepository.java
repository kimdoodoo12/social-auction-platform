package com.socialauction.backend.organization.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.socialauction.backend.organization.entity.OrganizationEntity;

@Repository 
public interface OrganizationRepository extends JpaRepository<OrganizationEntity, Integer> {
    
}
