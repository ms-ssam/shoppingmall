package com.example.elicesecondproject.mall.domain.product.dto;

import com.example.elicesecondproject.mall.domain.category.dto.CategoryResponse;
import com.example.elicesecondproject.mall.domain.option.dto.ProductOptionGroupDto;
import com.example.elicesecondproject.mall.domain.product.entity.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter; // Setter 추가 (Service에서 값 주입용)

import java.util.List;

@Getter
@Setter // Service에서 liked 값을 채우기 위해 Setter 추가
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

    private String mainImageUrl;

    private List<ProductOptionGroupDto> optionGroups;
    private List<ProductImageDto> images;
    private Double averageRating;
    private Integer reviewCount;
    private Integer wishListCount;
    private Integer totalStock;

    private boolean liked;
}