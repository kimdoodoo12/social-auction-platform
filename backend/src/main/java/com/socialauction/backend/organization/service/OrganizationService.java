package com.socialauction.backend.organization.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.socialauction.backend.organization.dto.admin.OrganizationDetailResponse;
import com.socialauction.backend.organization.dto.admin.OrganizationInfoRequest;
import com.socialauction.backend.organization.dto.admin.OrganizationInfoResponse;
import com.socialauction.backend.organization.dto.admin.OrganizationListResponse;
import com.socialauction.backend.organization.dto.admin.OrganizationProductResponse;
import com.socialauction.backend.organization.dto.admin.OrganizationSearchRequest;
import com.socialauction.backend.organization.entity.OrganizationEntity;
import com.socialauction.backend.organization.repository.OrganizationRepository;

import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class OrganizationService {
    private final OrganizationRepository oRepository;

    public Page<OrganizationListResponse> findAll(Pageable pageable){
        return oRepository.findAllOrganizationWithCounts(pageable);
    }

    public Page<OrganizationListResponse> findAll(OrganizationSearchRequest request, Pageable pageable){
        String name = request.getName();
        boolean agreementStatus = request.getAgreementStatus();
        return oRepository.findAllOrganizationWithCounts(name, agreementStatus, pageable);
    }

    public OrganizationDetailResponse getOrganizationDetailResponse(int id){
        OrganizationInfoResponse oInfoResponse = OrganizationInfoResponse.from(oRepository.findById(id).get());
        List<OrganizationProductResponse> oProductResponse = oRepository.findOrganizationProduct(id);

        return OrganizationDetailResponse.builder()
            .organizationInfoResponse(oInfoResponse)
            .organizationProductResponse(oProductResponse)
            .build();
    }

    public boolean save(OrganizationInfoRequest oInfoRequest){
        OrganizationEntity oEntity = oInfoRequest.toEntity();
        oRepository.save(oEntity);
        if(oEntity.getOrganizationId() >= 1){
            return true;
        }
        return false;
    }

    @Transactional
    public boolean update(int id, OrganizationInfoRequest oInfoRequest){
        OrganizationEntity oEntity = oRepository.findById(id).get();
        oEntity.updateOrganization(oInfoRequest);
        return true;
    }
}
