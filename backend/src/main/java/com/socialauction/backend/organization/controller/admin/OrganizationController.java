package com.socialauction.backend.organization.controller.admin;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.reactive.config.EnableWebFlux;

import com.socialauction.backend.organization.dto.admin.OrganizationDetailResponse;
import com.socialauction.backend.organization.dto.admin.OrganizationInfoRequest;
import com.socialauction.backend.organization.dto.admin.OrganizationInfoResponse;
import com.socialauction.backend.organization.dto.admin.OrganizationListResponse;
import com.socialauction.backend.organization.dto.admin.OrganizationSearchRequest;
import com.socialauction.backend.organization.service.OrganizationService;

import lombok.RequiredArgsConstructor;

@RestController 
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor 
@RequestMapping("/ieum/admin/organization")
public class OrganizationController {

    private final OrganizationService oService;

    @GetMapping("")
    public Page<OrganizationListResponse> findAll(@PageableDefault(page=0,  size=4, sort = "organization_id", direction = Sort.Direction.ASC)Pageable pageable){
        Page<OrganizationListResponse> organizations = oService.findAll(pageable);
        return organizations;
    }
    @GetMapping("/search")
    public Page<OrganizationListResponse> findAll(@RequestParam(name = "name", required = false)String name, @RequestParam(name = "agreementStatus", required = false) Boolean agreementStatus, @PageableDefault(page=0, size=10, sort = "organization_id", direction = Sort.Direction.ASC)Pageable pageable){
        Page<OrganizationListResponse> organizations = oService.findAll(name, agreementStatus, pageable);
        return organizations;
    }

    @GetMapping("/detail/{id}")
    public OrganizationDetailResponse find(@PathVariable("id") int id){
        return oService.getOrganizationDetailResponse(id);
    }

    @PostMapping("")
    public boolean save(@RequestBody OrganizationInfoRequest oInfoRequest){
        return oService.save(oInfoRequest);
    }

    @PutMapping("/detail/{id}")
    public boolean update(@PathVariable("id") int id, @RequestBody OrganizationInfoRequest oInfoResquest){
        return oService.update(id, oInfoResquest);
    }
}
