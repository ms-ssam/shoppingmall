package com.example.elicesecondproject.mall.domain.payment.service;

import com.example.elicesecondproject.mall.domain.payment.entity.Payment;
import com.example.elicesecondproject.mall.domain.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {
    private final PaymentRepository paymentRepository;

    @Transactional
    public Payment createReadyPayment(String orderId, Long memberId, int amount) {
        return paymentRepository.save(Payment.createReadyPayment(orderId, memberId, amount));
    }
}
