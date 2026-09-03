package com.example.elicesecondproject.mall.domain.order.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class OrderSheetFromCartRequest {
    @NotEmpty(message = "주문할 상품을 선택해주세요.")
    private List<Long> cartItemIds;
}
