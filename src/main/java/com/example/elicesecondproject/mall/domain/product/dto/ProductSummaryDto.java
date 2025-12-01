package com.example.elicesecondproject.mall.domain.product.dto;

import com.example.elicesecondproject.mall.domain.product.entity.ProductStatus;
import com.example.elicesecondproject.mall.domain.product.entity.Product;
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
    private int WishListCount;


    public static ProductSummaryDto from(Product product) {
        return ProductSummaryDto.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .salePrice(product.getSalePrice())
                .discountRate(product.getDiscountRate())
                .status(product.getStatus())
                .mainImageUrl(product.getMainImageUrl())
                .averageRating(product.getAverageRating())
                .reviewCount(product.getReviewCount())
                .WishListCount(product.getWishListCount())
                .build();
    }

}