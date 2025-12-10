package com.example.elicesecondproject.mall.domain.order.dto.response;


import com.example.elicesecondproject.mall.domain.order.entity.DeliveryInfo;
import com.example.elicesecondproject.mall.domain.order.entity.OrderStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;


@Getter
@Setter
public class UserOrderDetailResponse {

    private LocalDateTime orderDate;    // 주문 날짜
    private Long id;

    private String ordererName;
    private String ordererPhoneNumber;
    private DeliveryInfo deliveryInfo;  // 배송 정보
    private OrderStatus orderStatus;         // 주문 상태

    private List<UserOrderItemDetailResponse> orderItems ;

    private int totalPrice;             // 상품 총액
    private int deliveryFee;            // 배송비
    private int totalPaymentFee;        // 총 결제 금액

}