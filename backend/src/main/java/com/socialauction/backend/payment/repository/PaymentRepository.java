package com.socialauction.backend.payment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.socialauction.backend.payment.entity.PaymentEntity;

@Repository 
public interface PaymentRepository extends JpaRepository<PaymentEntity,Integer> {
    
}
