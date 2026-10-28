package com.zosh.repository;

import com.zosh.model.PaymentDetails;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentDetailRepository extends JpaRepository<PaymentDetails ,Long> {
    PaymentDetails findByUserId(Long userId);
}
