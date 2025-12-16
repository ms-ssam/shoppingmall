package com.example.elicesecondproject.mall.domain.payment.dto;

import com.example.elicesecondproject.mall.domain.order.entity.PaymentStatus;
import com.example.elicesecondproject.mall.domain.payment.entity.Payment;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PaymentInfoResponse {
    private final Long id;
    private final String paymentKey;
    private final String method;
    private final OffsetDateTime approvedAt;
    private final int amount;
    private final PaymentStatus paymentStatus;

    public static PaymentInfoResponse of(Payment payment) {
        return new PaymentInfoResponse(
                payment.getId(),
                payment.getPaymentKey(),
                payment.getMethod(),
                payment.getApprovedAt(),
                payment.getAmount(),
                payment.getPaymentStatus()
        );
    }
}
