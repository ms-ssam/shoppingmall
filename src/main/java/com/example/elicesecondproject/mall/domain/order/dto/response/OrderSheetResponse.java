package com.example.elicesecondproject.mall.domain.order.dto.response;

import lombok.Getter;

import java.util.List;

@Getter
public class OrderSheetResponse {
    // 주문서 화면에 보여줄 아이템 목록 + 배송비 + 총 결제 예정 금액

    private final List<OrderSheetItemResponse> items;

    // 화면 표시용 값들
    private final int totalPrice;         // 상품 총액
    private final int deliveryFee;        // 배송비
    private final int finalPaymentAmount; // 총 결제 금액


    public OrderSheetResponse(List<OrderSheetItemResponse> items, int deliveryFee) {
        this.items = items;
        this.totalPrice = items.stream()
                .mapToInt(OrderSheetItemResponse::getSubtotalPrice)
                .sum();
        this.deliveryFee = deliveryFee;
        this.finalPaymentAmount = this.totalPrice + this.deliveryFee;
    }
}
