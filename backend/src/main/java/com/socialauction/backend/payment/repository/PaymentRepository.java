package com.socialauction.backend.payment.repository;

import java.util.List;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.socialauction.backend.payment.dto.PaymentListResponse;
import com.socialauction.backend.payment.entity.PaymentEntity;

@Repository 
public interface PaymentRepository extends JpaRepository<PaymentEntity,Integer> {

    @Query(value = """
            SELECT a.auction_id AS auctionId, pd.name AS productName, o.name AS organizationName, m.name AS memberName, py.payment_price AS paymentPrice, py.created_at AS createdAt, py.payment_status AS paymentStatus 
            FROM payment py LEFT JOIN auction a on py.auction_id = a.auction_id 
            LEFT JOIN products pd on pd.product_id = a.product_id 
            LEFT JOIN organization o on o.organization_id = pd.organization_id 
            LEFT JOIN member m on py.member_id = m.member_id
            """, nativeQuery = true)
    Page<PaymentListResponse> findAllPayment(Pageable pageable);
}
