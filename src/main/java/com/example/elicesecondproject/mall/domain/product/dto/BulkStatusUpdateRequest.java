package com.example.elicesecondproject.mall.domain.product.dto;

import com.example.elicesecondproject.mall.domain.product.entity.ProductStatus;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BulkStatusUpdateRequest {
    @NotEmpty(message = "변경할 상품 ID를 선택해주세요.")
    private List<Long> productIds;

    @NotNull(message = "변경할 상태를 선택해주세요.")
    private ProductStatus status;
}