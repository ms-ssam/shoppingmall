package com.example.elicesecondproject.mall.domain.product.dto;

import com.example.elicesecondproject.mall.domain.option.dto.ProductOptionGroupDto;
import com.example.elicesecondproject.mall.domain.product.entity.ProductStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProductRequest {

    @Size(max = 200, message = "상품명은 최대 200자까지 입력 가능합니다.")
    private String name;

    @Min(value = 0, message = "가격은 0원 이상이어야 합니다.")
    private Integer price;

    @Min(value = 0, message = "할인율은 0% 이상이어야 합니다.")
    @Max(value = 100, message = "할인율은 100% 이하여야 합니다.")
    private Integer discountRate;

    private String description;
    private Long categoryId;
    private ProductStatus status;

    @Valid
    private List<ProductOptionGroupDto> optionGroups;

    @Valid
    private List<ProductImageDto> images;
}