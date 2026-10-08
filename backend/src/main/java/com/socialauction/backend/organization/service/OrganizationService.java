package com.socialauction.backend.organization.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.socialauction.backend.global.FileService;
import com.socialauction.backend.global.upload.UploadFolder;
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
    private final FileService fileService;

    @Transactional(readOnly = true)
    public Page<OrganizationListResponse> findAll(Pageable pageable){
        return oRepository.findAllOrganizationWithCounts(pageable);
    }

    @Transactional(readOnly = true)
    public Page<OrganizationListResponse> findAll(String name, Boolean agreementStatus, Pageable pageable){
        return oRepository.findAllOrganizationWithCounts(name, agreementStatus, pageable);
    }

    @Transactional(readOnly = true)
    public OrganizationDetailResponse getOrganizationDetailResponse(int id){
        OrganizationInfoResponse oInfoResponse = OrganizationInfoResponse.from(oRepository.findById(id).get());
        List<OrganizationProductResponse> oProductResponse = oRepository.findOrganizationProduct(id);

        return OrganizationDetailResponse.builder()
            .organizationInfoResponse(oInfoResponse)
            .organizationProductResponse(oProductResponse)
            .build();
    }

    public boolean save(OrganizationInfoRequest oInfoRequest){
        String savedImageFileName = null;
        String savedAgreementFileName = null;

        // 협약서 첨부파일이 존재하면
        if(oInfoRequest.getAgreementFile() != null && !oInfoRequest.getAgreementFile().isEmpty()){
            savedAgreementFileName = fileService.upload(UploadFolder.ORGANIZATION, oInfoRequest.getAgreementFile());
            if(savedAgreementFileName == null){return false;}
        }

        // 기관이미지 첨부파일이 존재하면
        if(oInfoRequest.getOrganizationImageFile() != null && !oInfoRequest.getOrganizationImageFile().isEmpty()){
            savedImageFileName = fileService.upload(UploadFolder.ORGANIZATION, oInfoRequest.getOrganizationImageFile());
            if(savedImageFileName == null){return false;}
        }
        OrganizationEntity oEntity = oInfoRequest.toEntity();
        oEntity.setAgreementFileName(savedAgreementFileName);
        oEntity.setOrganizationImageFileName(savedImageFileName);
        oRepository.save(oEntity);
        if(oEntity.getOrganizationId() >= 1){
            return true;
        }
        return false;
    }

    @Transactional
    public boolean update(int id, OrganizationInfoRequest oInfoRequest){

        Optional<OrganizationEntity> optional = oRepository.findById(id);
        String savedImageFileName = null;
        String savedAgreementFileName = null;

        if(optional.isPresent()){
            OrganizationEntity oEntity = optional.get();
            
            // 협약서 첨부파일이 존재하면
            if(oInfoRequest.getAgreementFile() != null && !oInfoRequest.getAgreementFile().isEmpty()){
                savedAgreementFileName = fileService.upload(UploadFolder.ORGANIZATION, oInfoRequest.getAgreementFile());
                if(savedAgreementFileName == null){return false;}
            }

            // 기관이미지 첨부파일이 존재하면
            if(oInfoRequest.getOrganizationImageFile() != null && !oInfoRequest.getOrganizationImageFile().isEmpty()){
                savedImageFileName = fileService.upload(UploadFolder.ORGANIZATION, oInfoRequest.getOrganizationImageFile());
                if(savedImageFileName == null){return false;}
            }

            // 새 파일이 업로드된 경우에만 교체하고, 이전 파일은 삭제한다 (없으면 기존 파일 유지)
            if(savedAgreementFileName != null){
                fileService.delete(UploadFolder.ORGANIZATION, oEntity.getAgreementFileName());
                oEntity.setAgreementFileName(savedAgreementFileName);
            }
            if(savedImageFileName != null){
                fileService.delete(UploadFolder.ORGANIZATION, oEntity.getOrganizationImageFileName());
                oEntity.setOrganizationImageFileName(savedImageFileName);
            }
            oEntity.updateOrganization(oInfoRequest);

            return true;
        }
        return false;
    }


    // 협약서 파일명 PK로 참조
    public String getAgreementFileName(int id){
        Optional<OrganizationEntity> optional = oRepository.findById(id);
        if(optional.isPresent()){
            return optional.get().getAgreementFileName();
        }
        return null;
    }

    // 기관 이미지 파일명 PK로 참조
    public String getOrganizationImageFileName(int id){
        Optional<OrganizationEntity> optional = oRepository.findById(id);
        if(optional.isPresent()){
            return optional.get().getOrganizationImageFileName();
        }
        return null;
    }
}
