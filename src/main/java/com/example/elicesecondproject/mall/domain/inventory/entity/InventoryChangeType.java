package com.example.elicesecondproject.mall.domain.inventory.entity;

public enum InventoryChangeType {
    IN,      // 입고
    OUT,     // 출고 (폐기, 조정 등)
    ORDER,   // 주문 (차감)
    CANCEL   // 주문 취소 (복구)
}