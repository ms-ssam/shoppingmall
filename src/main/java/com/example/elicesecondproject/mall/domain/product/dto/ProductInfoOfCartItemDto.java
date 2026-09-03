package com.example.elicesecondproject.mall.domain.product.dto;

import com.example.elicesecondproject.mall.domain.product.entity.Product;
import lombok.Builder;
import lombok.Getter;

// 장바구니 화면의 장바구니 항목에 보여줄 정보 용도입니다.
@Getter
public class ProductInfoOfCartItemDto {
    private long id;
    private String name;
    private String thumbnailUrl;
    private int price;
    private int discountRate;

    @Builder
    private ProductInfoOfCartItemDto(long id, String name, String thumbnailUrl, int price, int discountRate) {
        this.id = id;
        this.name = name;
        this.thumbnailUrl = thumbnailUrl;
        this.price = price;
        this.discountRate = discountRate;
    }

    public static ProductInfoOfCartItemDto of(Product product) {
        return ProductInfoOfCartItemDto.builder()
                .id(product.getId())
                .name(product.getName())
                .thumbnailUrl(product.getThumbnailUrl())
                .price(product.getPrice())
                .discountRate(product.getDiscountRate())
                .build();
    }
}
