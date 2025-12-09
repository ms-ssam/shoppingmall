package com.example.elicesecondproject.mall.domain.order.dto.response;

import com.example.elicesecondproject.mall.domain.member.entity.Member;
import lombok.Getter;

@Getter
public class OrdererInfoResponse {
    private final String name;
    private final String phoneNumber;
    private final String email;

    public OrdererInfoResponse(String name, String phoneNumber, String email) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.email = email;
    }

    public static OrdererInfoResponse from(Member member) {
        return new OrdererInfoResponse(
                member.getName(),
                member.getPhone(),      // or getPhoneNumber()
                member.getEmail()
        );
    }
}
