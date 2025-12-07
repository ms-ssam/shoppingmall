package com.example.elicesecondproject.mall.domain.cart.dto.response;

import com.example.elicesecondproject.mall.domain.cart.entity.Cart;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class CartInfoResponseDto {
    private long cartId;
    private List<CartItemInfoResponseDto> cartItems;
    private int totalPrice;  // (수량 고려)
    private int totalCount;  // 장바구니 안 장바구니 항목의 개수 (각각의 수량 고려 X)

    @Builder
    private CartInfoResponseDto(long cartId, List<CartItemInfoResponseDto> cartItems, int totalPrice, int totalCount) {
        this.cartId = cartId;
        this.cartItems = cartItems;
        this.totalPrice = totalPrice;
        this.totalCount = totalCount;
    }

    public static CartInfoResponseDto of(Cart cart) {
        List<CartItemInfoResponseDto> items = cart.getCartItems().stream()
                .map(CartItemInfoResponseDto::of)
                .toList();

        int totalPrice = items.stream()
                .mapToInt(item -> item.getSubtotalPrice())
                .sum();  // 여기 로직이 문제?

        int totalCount = items.size();

        return CartInfoResponseDto.builder()
                .cartId(cart.getId())
                .cartItems(items)
                .totalPrice(totalPrice)
                .totalCount(totalCount)
                .build();
    }
}