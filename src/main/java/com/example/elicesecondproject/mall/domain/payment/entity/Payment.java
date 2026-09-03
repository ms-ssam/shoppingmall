package com.example.elicesecondproject.mall.domain.payment.entity;

import com.example.elicesecondproject.mall.domain.order.entity.PaymentStatus;
import com.example.elicesecondproject.mall.global.entity.BaseEntity;
import com.example.elicesecondproject.mall.global.error.ErrorCode;
import com.example.elicesecondproject.mall.global.error.exception.BusinessException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "payments")
public class Payment extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long memberId;

    private String paymentKey;

    @Column(nullable = false, updatable = false)
    private String orderId;  // 주문 번호 (!= PK)

    @Column(nullable = false, updatable = false)
    private int amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus;

    private OffsetDateTime approvedAt;  // OffsetDateTime

    private String method;

    public static Payment createReadyPayment(String orderId, Long memberId, int amount) {
        Payment payment = new Payment();
        payment.orderId = orderId;
        payment.memberId = memberId;
        payment.amount = amount;
        payment.paymentStatus = PaymentStatus.READY;

        return payment;
    }

    public void markAsCompleted(String paymentKey, String method, String approvedAt) {
        this.paymentKey = paymentKey;
        this.method = method;
        this.approvedAt = OffsetDateTime.parse(approvedAt);
        changePaymentStatus(PaymentStatus.COMPLETED);
    }

    public void markAsFailed() {
        changePaymentStatus(PaymentStatus.FAILED);
    }

    private void changePaymentStatus(PaymentStatus newStatus) {
        if (!paymentStatus.canChangeTo(newStatus)) {
            throw new BusinessException(ErrorCode.INVALID_PAYMENT_STATUS_CHANGE);
        }
        paymentStatus = newStatus;
    }

    public void validAmount(Long amount) {
        // 비교 총액이 null이면 안됨
        assert amount != null;

        if (this.amount != amount) {
            throw new BusinessException(ErrorCode.INVALID_PAYMENT_AMOUNT);
        }
    }
}
