package com.example.elicesecondproject.mall.domain.cart.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
public class CartItemInfoResponseDto {
    private long cartItemId;
    private long productId;  //  TODO: 이거랑 밑에 거 필요한지 확인하기
    private long optionDetailId;
    private String productName;
    private String color;  // 색상명 - 그레이, 블랙 등..
    private String size;  // 사이즈명 - S, M, L 등..
    private String image;  // 대표 이미지'
    private int originalUnitPrice;  // 할인이 안 들어간 단위 정가 -> Product에 있는 UnitPrice
    private int discountRate;
    private int additionalOptionPrice;  // Product 원가 + DoptionDetail 옵션 추가금
    private int saleUnitPrice;  // (Product 원가에 할인률 적용한 금액) + OptionDetail 추가금
    private int quantity;
    private int subtotalPrice;  //
    private boolean soldOut;  // ❗계산해야 하는 부분 (비즈니스 로직 들어감) ✅

    @Builder
    private CartItemInfoResponseDto(long cartItemId,
                                    long productId,
                                    long optionDetailId,
                                    String productName,
                                    String color,
                                    String size,
                                    String image,
                                    int originalUnitPrice,
                                    int discountRate,
                                    int additionalOptionPrice,
                                    int saleUnitPrice,
                                    int quantity,
                                    int subtotalPrice,
                                    boolean soldOut) {
        this.cartItemId = cartItemId;
        this.productId = productId;
        this.optionDetailId = optionDetailId;
        this.productName = productName;
        this.color = color;
        this.size = size;
        this.image = image;
        this.originalUnitPrice = originalUnitPrice;
        this.discountRate = discountRate;
        this.additionalOptionPrice = additionalOptionPrice;
        this.saleUnitPrice = saleUnitPrice;
        this.quantity = quantity;
        this.subtotalPrice = subtotalPrice;
        this.soldOut = soldOut;
    }

}
//