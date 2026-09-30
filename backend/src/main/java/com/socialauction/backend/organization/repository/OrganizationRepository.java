package com.socialauction.backend.organization.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.socialauction.backend.organization.dto.admin.OrganizationInfoResponse;
import com.socialauction.backend.organization.dto.admin.OrganizationListResponse;
import com.socialauction.backend.organization.dto.admin.OrganizationProductResponse;
import com.socialauction.backend.organization.dto.admin.OrganizationSearchRequest;
import com.socialauction.backend.organization.entity.OrganizationEntity;

@Repository 
public interface OrganizationRepository extends JpaRepository<OrganizationEntity, Integer>{
    

    @Query(value = "SELECT "
    + "o.organization_id, o.name, o.manager, o.manager_phone, "
    + "o.agreement_date, o.agreement_status, COUNT(products.product_id) "
    + "FROM organization as o LEFT JOIN products on o.organization_id = products.organization_id "
    + "GROUP BY o.organization_id", nativeQuery = true)
    Page<OrganizationListResponse> findAllOrganizationWithCounts(Pageable pageable);

    @Query(value = "SELECT "
    + "o.organization_id, o.name, o.manager, o.manager_phone, "
    + "o.agreement_date, o.agreement_status, COUNT(products.product_id) "
    + "FROM organization as o LEFT JOIN products on o.organization_id = products.organization_id "
    + "WHERE (o.name = :name is null or o.name LIKE CONCAT('%', :name, '%')) AND (o.agreement_status = :agreementStatus is null or o.agreement_status = :agreementStatus)"
    + "GROUP BY o.organization_id "
    , nativeQuery = true)
    Page<OrganizationListResponse> findAllOrganizationWithCounts(@Param("name")String name, @Param("agreementStatus") Boolean agreementStatus, Pageable pageable);


    @Query(value = "SELECT "
    + "p.product_id, p.name, p.start_price, b.bid_price, a.auction_status, p.created_at "
    + "FROM products p "
    + "LEFT JOIN auction a "
    + "ON p.product_id = a.product_id "
    + "LEFT JOIN bid b "
    + "ON a.auction_id = b.auction_id "
    + "AND b.bid_time = (SELECT "
    + "MAX(b2.bid_time) FROM bid b2 "
    + "WHERE b2.auction_id = a.auction_id) "
    + "WHERE p.organization_id = :id "
    + "LIMIT 10", nativeQuery = true)
    List<OrganizationProductResponse> findOrganizationProduct(@Param("id") int id);
}
