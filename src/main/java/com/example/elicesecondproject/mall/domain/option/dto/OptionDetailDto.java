package com.example.elicesecondproject.mall.domain.option.dto;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OptionDetailDto {
    private Long id;

    @NotBlank(message = "사이즈명은 필수입니다.")
    private String name;

    @NotBlank(message = "SKU는 필수입니다.")
    private String sku;

    @NotNull(message = "추가 금액은 필수입니다.")
    private Integer addPrice;

    @NotNull(message = "재고 수량은 필수입니다.")
    @Min(value = 0, message = "재고 수량은 0개 이상이어야 합니다.")
    private Integer stockQuantity;

    @NotNull(message = "정렬 순서는 필수입니다.")
    private Integer displayOrder;
}

