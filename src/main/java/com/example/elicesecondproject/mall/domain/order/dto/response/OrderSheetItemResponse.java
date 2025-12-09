package com.example.elicesecondproject.mall.domain.order.dto.response;

import com.example.elicesecondproject.mall.domain.cart.entity.CartItem;
import com.example.elicesecondproject.mall.domain.option.entity.OptionDetail;
import com.example.elicesecondproject.mall.domain.product.entity.Product;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class OrderSheetItemResponse {
    // 사용자에게 보여줄 화면용(CartItem → DTO)
    // 선택된 카트아이템(제품명, 색, 사이즈, 단가, 수량, 소계) 만 보여주면 됨.

    private Long cartItemId;

    private Long productId;
    private String productName;     // 상품명
    private String thumbnailUrl;

    private String optionGroupName;  // 색
    private String optionDetailName; // 사이즈

    private int unitPrice;
    private int quantity;
    private int subtotalPrice;

    public static OrderSheetItemResponse from(CartItem cartItem) {

        OrderSheetItemResponse dto = new OrderSheetItemResponse();

        dto.cartItemId = cartItem.getId();

        OptionDetail optionDetail = cartItem.getProductOptionDetail();
        Product product = optionDetail.getProduct();

        dto.productId = product.getId();
        dto.productName = product.getName();
        dto.thumbnailUrl = product.getThumbnailUrl();

        dto.optionGroupName = optionDetail.getProductOptionGroup().getName();
        dto.optionDetailName = optionDetail.getName();

        dto.unitPrice = optionDetail.getSaleUnitPrice();
        dto.quantity = cartItem.getQuantity();
        dto.subtotalPrice = dto.unitPrice * dto.quantity;

        return dto;
    }


/*
    // orderItem스냅샷 부분으로 들어갈 정보들
    private Long productId;
    private String productName;     // 상품명
    private String thumbnailUrl;

    private String optionGroupName;  // 색
    private String optionDetailName; // 사이즈

    private int unitPrice;
    private int quantity;
    private int subtotalPrice;

    //===========
    public static OrderSheetItemDto from(CartItem cartItem) {

        OrderSheetItemDto dto = new OrderSheetItemDto();

        dto.cartItemId = cartItem.getId();

        var optionDetail = cartItem.getProductOptionDetail();
        var product = optionDetail.getProduct();

        dto.productId = product.getId();
        dto.productName = product.getName();
        dto.thumbnailUrl = product.getThumbnailUrl();

        dto.optionGroupName = optionDetail.getProductOptionGroup().getName();
        dto.optionDetailName = optionDetail.getName();

        dto.unitPrice = optionDetail.getSaleUnitPrice();
        dto.quantity = cartItem.getQuantity();
        dto.subtotalPrice = dto.unitPrice * dto.quantity;

        return dto;
    }*/
}
