package com.example.elicesecondproject.mall.domain.category.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateCategoryRequest {

    @Size(max = 100, message = "카테고리명은 최대 100자까지 입력 가능합니다.")
    private String name;

    @Size(max = 100, message = "슬러그는 최대 100자까지 입력 가능합니다.")
    private String slug;

    private Integer displayOrder;

    private Boolean isVisible;

    //부모 필드 변경을 위해 필드 추가
    private Long parentId;

}
