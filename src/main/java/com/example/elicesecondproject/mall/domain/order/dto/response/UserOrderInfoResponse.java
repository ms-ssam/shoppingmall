package com.example.elicesecondproject.mall.domain.order.dto.response;

import com.example.elicesecondproject.mall.domain.order.entity.OrderStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class UserOrderInfoResponse {
    private Long id;
    private String orderId;
    private String ordererName;
    private String ordererPhoneNumber;
    private String ordererEmail;
    private String mainProductName;
    private int totalPaymentFee;
    private LocalDateTime orderDate;
    private OrderStatus orderStatus;
}
