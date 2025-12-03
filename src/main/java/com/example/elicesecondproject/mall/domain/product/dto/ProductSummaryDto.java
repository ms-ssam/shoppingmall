package com.example.elicesecondproject.mall.domain.product.dto;

import com.example.elicesecondproject.mall.domain.product.entity.Product;
import com.example.elicesecondproject.mall.domain.product.entity.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSummaryDto {
    private Long id;
    private String name;
    private int price;
    private int salePrice;
    private int discountRate;
    private ProductStatus status;
    private String mainImageUrl;
    private Double averageRating;
    private int reviewCount;
    private int wishListCount;

    // 재고 필드 추가
    private Integer totalStock;
}