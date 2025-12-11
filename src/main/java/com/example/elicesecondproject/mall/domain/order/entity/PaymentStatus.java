package com.example.elicesecondproject.mall.domain.order.entity;

public enum PaymentStatus {
    READY,          // 결제 준비
    COMPLETED,      // 결제 완료
    FAILED,         // 결제 실패
    CANCELLED;       // 결제 취소

    public boolean canChangeTo(PaymentStatus target) {

        // no-op 허용
        if (this == target) return true;

        return switch (this) {
            case READY -> target == COMPLETED || target == FAILED;

            case COMPLETED -> target == CANCELLED;  // PG 또는 관리자에 의한 취소 가능여부를 비즈니스 정책에 따라 허용/금지

            case FAILED -> false;   // 실패 → 다른 상태로 갈 수 없음

            case CANCELLED -> false; // 취소 후 변경 불가

            default -> false;
        };
    }
}
