package com.example.elicesecondproject.mall.domain.payment.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TossConfirmResponse {
    private String paymentKey;
    private String orderId;
    private String status;
    private int amount;
    private String method;
    private String approvedAt;
}
