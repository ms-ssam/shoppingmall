package com.example.elicesecondproject.mall.domain.order.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserOrderItemDetailResponse {
    private Long productId;
    private String productName;
    private String productThumbnailUrl;

    private String productOptionGroupName;  // 색
    private String optionDetail;            // 사이즈

    private int unitPrice;
    private int quantity;
    private int subtotalPrice;
}


