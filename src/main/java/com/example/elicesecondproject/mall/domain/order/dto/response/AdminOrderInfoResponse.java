package com.example.elicesecondproject.mall.domain.order.dto.response;

import com.example.elicesecondproject.mall.domain.order.entity.OrderStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class AdminOrderInfoResponse {
    private Long id;
    // TODO: private String orderId 추가 - 관련 html 수정
    private String ordererName;
    private String mainProductName;
    private int totalPaymentFee;
    private LocalDateTime orderDate;
    private OrderStatus orderStatus;
}
