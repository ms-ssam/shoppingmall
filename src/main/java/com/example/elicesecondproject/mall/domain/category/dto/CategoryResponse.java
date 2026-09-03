package com.example.elicesecondproject.mall.domain.category.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CategoryResponse {
    private Long id;
    private String name;
    private String slug;
    private Long parentId;
    private String parentName;
    private Integer depth;
    private Integer displayOrder;
    private Boolean isVisible;
}
