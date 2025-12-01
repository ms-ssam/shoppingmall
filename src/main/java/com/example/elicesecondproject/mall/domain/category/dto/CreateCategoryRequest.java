package com.example.elicesecondproject.mall.domain.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateCategoryRequest {

    @NotBlank(message = "카테고리명은 필수입니다.")
    @Size(max = 100, message = "카테고리명은 최대 100자까지 입력 가능합니다.")
    private String name;

    private Long parentId;

    @NotBlank(message = "슬러그는 필수입니다.")
    @Size(max = 100, message = "슬러그는 최대 100자까지 입력 가능합니다.")
    private String slug;

    @NotNull(message = "정렬 순서는 필수입니다.")
    private Integer displayOrder;

    private Boolean isVisible = true;

    @Size(max = 500, message = "설명은 최대 500자까지 입력 가능합니다.")
    private String description;
}
