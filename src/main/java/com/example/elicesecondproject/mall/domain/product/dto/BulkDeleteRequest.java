package com.example.elicesecondproject.mall.domain.product.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BulkDeleteRequest {
    @NotEmpty(message = "삭제할 상품 ID를 선택해주세요.")
    private List<Long> productIds;
}
