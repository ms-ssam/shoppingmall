package com.example.elicesecondproject.mall.domain.cart.dto.response;

import lombok.Getter;

import java.util.List;

@Getter
public class CartInfoResponseDto {
    private long cartId;
    private List<CartItemInfoResponseDto> cartItems;
    private long totalPrice;  // (수량 고려)
    private long totalCount;  // 장바구니 안 장바구니 항목의 개수 (각각의 수량 고려 X)
}