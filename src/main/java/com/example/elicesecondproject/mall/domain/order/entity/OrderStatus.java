package com.example.elicesecondproject.mall.domain.order.entity;

public enum OrderStatus {
    PENDING,            // 결제 대기중
    FAILED,             // 결제 완료 전 결제 취소 or 실패
    PAID,               // 결제 완료
    PREPARING,          // 상품 준비중
    DELIVERING,         // 택배사에 전달
    DELIVERED,          // 배송 완료
    CANCEL_REQUESTED,   // 주문 취소 요청 (사용자 요청)
    CANCELED;            // 주문 취소 승인 (관리자 승인)

    public boolean canChangeTo(OrderStatus target) {
        // 같은 상태로 변경 시도는 허용 (no-op)
        if (this == target) {
            return true;
        }

        return switch (this) {
            case PENDING   -> target == PAID || target == FAILED;
            case FAILED -> false;
            case PAID   -> target == PREPARING ||  target == CANCEL_REQUESTED;
            case PREPARING -> target == DELIVERING;
            case DELIVERING  -> target == DELIVERED;
            case DELIVERED   -> false;
            case CANCEL_REQUESTED  -> target == CANCELED;
            case CANCELED   -> false;
            default -> false;
        };
    }
}
