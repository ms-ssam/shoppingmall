package com.example.elicesecondproject.mall.domain.order.dto.response;

import com.example.elicesecondproject.mall.domain.order.entity.OrderItem;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AdminOrderItemDetailResponse {
    private final Long productId;
    private final String productName;
    private final String productOptionGroupName;
    private final String optionDetailName;
    private final int unitPrice;
    private final int quantity;
    private final int subtotalPrice;

    public static AdminOrderItemDetailResponse of(OrderItem orderItem) {
        return new AdminOrderItemDetailResponse(
                orderItem.getProductId(),
                orderItem.getProductName(),
                orderItem.getProductOptionGroupName(),
                orderItem.getOptionDetail(),
                orderItem.getUnitPrice(),
                orderItem.getQuantity(),
                orderItem.getSubtotalPrice()
        );
    }
}