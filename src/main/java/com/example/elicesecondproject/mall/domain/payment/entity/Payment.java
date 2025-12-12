package com.example.elicesecondproject.mall.domain.payment.entity;

import com.example.elicesecondproject.mall.domain.order.entity.PaymentStatus;
import com.example.elicesecondproject.mall.global.entity.BaseEntity;
import com.example.elicesecondproject.mall.global.error.ErrorCode;
import com.example.elicesecondproject.mall.global.error.exception.BusinessException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "payments")
public class Payment extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    private LocalDateTime approvedAt;
    // TODO: 결제 수단, 등의 정보 저장하고 싶다면 어떻게? confirm() 메서드 return 값이 있나? Toss의 Payment 객체? 그럼 그 안에 해당 정보 존재?

    public static Payment createReadyPayment(String orderId, Long memberId, int amount) {
        Payment payment = new Payment();
        payment.orderId = orderId;
        payment.memberId = memberId;
        payment.amount = amount;
        payment.paymentStatus = PaymentStatus.READY;

        return payment;
    }

    public void markAsCompleted(String paymentKey) {
        this.paymentKey = paymentKey;
        changePaymentStatus(PaymentStatus.COMPLETED);
        approvedAt = LocalDateTime.now();
    }

    public void markAsFailed() {
        changePaymentStatus(PaymentStatus.FAILED);
    }

    private void changePaymentStatus(PaymentStatus newStatus) {
        if(!paymentStatus.canChangeTo(newStatus)) {
            throw new BusinessException(ErrorCode.INVALID_PAYMENT_STATUS_CHANGE);
        }
        paymentStatus = newStatus;
    }

}
