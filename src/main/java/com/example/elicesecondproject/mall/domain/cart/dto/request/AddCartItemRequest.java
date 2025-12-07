package com.example.elicesecondproject.mall.domain.cart.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class AddCartItemRequest {
    // 장바구니에 넣기 위한 상품 정보
    // 상품 id, 옵션, 사이즈, 수량
    // 상품이 아니라 optionDetail을 넣는거
    // OptionDetail → Product 역참조
    @NotNull(message = "옵션 정보는 필수입니다.")
    private Long optionDetailId;

    @Min(value = 1, message = "수량은 1개 이상이여야 합니다.")
    private int quantity;
}
