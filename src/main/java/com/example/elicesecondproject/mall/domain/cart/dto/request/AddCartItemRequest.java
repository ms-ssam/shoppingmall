package com.example.elicesecondproject.mall.domain.cart.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class AddCartItemRequest {

    @NotEmpty(message = "옵션은 최소 1개 이상 선택해야 합니다.")
    private List<
                @NotNull(message = "옵션 정보는 필수입니다.")
                        Long> optionDetailIds;

    @NotEmpty(message = "수량 정보는 필수입니다.")
    private List<
            @NotNull(message = "수량은 필수입니다.")
            @Min(value = 1, message = "수량은 1개 이상이여야 합니다.")
                    Integer> quantities;
}
