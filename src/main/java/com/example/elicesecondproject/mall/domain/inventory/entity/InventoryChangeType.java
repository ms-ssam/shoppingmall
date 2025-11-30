package com.example.elicesecondproject.mall.domain.inventory.entity;

public enum InventoryChangeType {
    PURCHASE,      // 입고
    SALE,          // 출고 (판매)
    ADJUSTMENT,    // 재고 조정
    RETURN,        // 반품
    DAMAGE,        // 손실/파손
    ORDER,         // 주문 (재고 예약)
    CANCEL,        // 주문 취소 (재고 복구)
    REFUND         // 환불 (재고 복구)
}