package com.example.elicesecondproject.mall.domain.category.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MoveCategoryRequest {

    @NotNull(message = "이동할 카테고리 ID는 필수입니다.")
    private Long targetId;

    private Long parentId;

    @NotNull(message = "정렬 순서는 필수입니다.")
    private Integer displayOrder;
}
