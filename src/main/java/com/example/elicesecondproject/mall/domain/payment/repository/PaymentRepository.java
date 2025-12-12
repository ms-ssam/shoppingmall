package com.example.elicesecondproject.mall.domain.payment.repository;

import com.example.elicesecondproject.mall.domain.order.entity.PaymentStatus;
import com.example.elicesecondproject.mall.domain.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment,Long> {

    // 주문번호와 사용자 Id, 결제 상태로 결제 기록을 조회
    Optional<Payment> findByOrderIdAndMemberIdAndPaymentStatus(String orderId, Long memberId, PaymentStatus paymentStatus);
}
