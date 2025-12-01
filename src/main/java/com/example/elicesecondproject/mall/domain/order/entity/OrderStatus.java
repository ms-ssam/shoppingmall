package com.example.elicesecondproject.mall.domain.order.entity;

public enum OrderStatus {
    PENDING,                // 주문 생성 후 대기 중 (결제 전)
    CONFIRMED,              // 결제까지 완료 -> 주문 확정
    PREPARING,              // 상품 준비 중
    DELIVERING,             // 배송 중
    DELIVERED,              // 배송 완료
    COMPLETED,              // 구매 확정 (사용자 수령 완료)

    CANCEL_REQUESTED,       // 사용자가 주문 취소 요청 (취소 처리 대기 중)
    CANCELED,               // 주문이 실제로 취소됨
    FAILED                  // 시스템적으로 주문 자체가 실패함 (재고 문제 등...)
}

/**
 * PAID는 PaymentStatus에서
 * PaymentStatus 예시
 * public enum PaymentStatus {
 *     PENDING,         // 결제 시도 전
 *     PROCESSING,      // PG 승인 요청 중
 *     PAID,            // 결제 성공 (승인 완료)
 *     DECLINED,        // 결제 '거절'됨
 *     CANCELED         // 결제 '취소'됨 (승인  취소)
 *     // REFUND_REQUESTED, REFUNDED 추후 고려
 * }
 */