package com.example.elicesecondproject.mall.domain.payment.event;

import com.example.elicesecondproject.mall.domain.order.entity.Order;

public record TossPaymentSucceedEvent(
        Order order,
        Long memberId
) {
    public static TossPaymentSucceedEvent of(Order order, Long memberId) {
        return new TossPaymentSucceedEvent(order, memberId);
    }
}
