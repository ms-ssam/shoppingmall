package com.example.elicesecondproject.mall.domain.order.dto.response;


import com.example.elicesecondproject.mall.domain.order.entity.OrderStatus;
import com.example.elicesecondproject.mall.domain.order.entity.PaymentStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;


@Getter
@Setter
public class UserOrderDetailResponse {
    private Long id;                                        // 주문 id
    private LocalDateTime orderDate;                        // 주문 날짜

    private OrderStatus orderStatus;                        // 주문 상태
    private PaymentStatus paymentStatus;                    // 결제 상태

    private String ordererName;                             // 주문자 이름
    private String ordererPhoneNumber;                      // 주문자 연락처

    private String mainProductName;                         // 대표 상품명

    private int totalPrice;                                 // 상품 총액
    private int deliveryFee;                                // 배송비
    private int totalPaymentFee;                            // 총 결제 금액

    private DeliveryInfoResponse deliveryInfo;              // 배송정보
    private List<UserOrderItemDetailResponse> orderItems;   // 주문 상품 리스트

}