package com.example.elicesecondproject.mall.domain.order.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserOrderItemDetailResponse {
    private String productThumbnailUrl;
    private String productName;

    private int unitPrice;
    private int quantity;
    private int subtotalPrice;
}


