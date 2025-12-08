package com.example.elicesecondproject.mall.domain.cart.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CartItemOptionModifyRequest {
    @NotNull(message = "옵션은 필수 선택입니다.")
    private Long optionDetailId;

    @NotNull(message = "수량은 필수 입력값입니다.")
    @Min(value = 1, message = "수량은 1개 이상이어야 합니다.")
    private Integer updatedQuantity;
}
