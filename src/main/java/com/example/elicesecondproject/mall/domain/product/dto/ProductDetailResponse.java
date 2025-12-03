package com.example.elicesecondproject.mall.domain.product.dto;

import com.example.elicesecondproject.mall.domain.category.dto.CategoryResponse;
import com.example.elicesecondproject.mall.domain.option.dto.ProductOptionGroupDto;
import com.example.elicesecondproject.mall.domain.product.entity.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetailResponse {
    private Long id;
    private String name;
    private Integer price;
    private Integer salePrice;
    private Integer discountRate;
    private String description;
    private ProductStatus status;
    private CategoryResponse category;
    private List<ProductOptionGroupDto> optionGroups;
    private List<ProductImageDto> images;
    private Double averageRating;
    private Integer reviewCount;
    private Integer wishListCount;

    //재고 추가
    private Integer totalStock;
}