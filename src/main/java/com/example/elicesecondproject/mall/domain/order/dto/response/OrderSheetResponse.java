package com.example.elicesecondproject.mall.domain.order.dto.response;

import lombok.Getter;

import java.util.List;

@Getter
public class OrderSheetResponse {
    // 아이템 목록
    private final List<OrderSheetItemResponse> items;

    // 주문자 정보 스냅샷 구조와 맞춤
    private final OrdererInfoResponse orderer;

    // 화면 표시용 값들
    private final int totalPrice;         // 상품 총액
    private final int deliveryFee;        // 배송비
    private final int finalPaymentAmount; // 총 결제 금액


    public OrderSheetResponse(OrdererInfoResponse orderer,
                              List<OrderSheetItemResponse> items,
                              int deliveryFee) {
        this.items = items;

        this.orderer = orderer;

        this.totalPrice = items.stream()
                .mapToInt(OrderSheetItemResponse::getSubtotalPrice)
                .sum();
        this.deliveryFee = deliveryFee;
        this.finalPaymentAmount = this.totalPrice + this.deliveryFee;
    }
}
