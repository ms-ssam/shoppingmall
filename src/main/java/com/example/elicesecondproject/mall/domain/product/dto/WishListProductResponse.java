package com.example.elicesecondproject.mall.domain.product.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WishListProductResponse {

    private Long id;
    private String thumbnailUrl;
    private String name;
    private int price; // 정가 기본 가격 -> 옵션마다 추가 금액이 있는 방식
    private int discountRate; // 할인율 (%)

    private double averageRating ;
    private int reviewCount;
    private int wishListCount;

}
