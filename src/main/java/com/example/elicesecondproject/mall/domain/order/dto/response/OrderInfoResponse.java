package com.example.elicesecondproject.mall.domain.order.dto.response;

import com.example.elicesecondproject.mall.domain.member.dto.response.MemberNameResponse;
import com.example.elicesecondproject.mall.domain.order.entity.OrderStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class OrderInfoResponse {
    private Long id;
    private MemberNameResponse member;
    private String mainProductName;
    private int totalPaymentFee;
    private LocalDateTime orderDate;
    private OrderStatus orderStatus;
}
