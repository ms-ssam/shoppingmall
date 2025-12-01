package com.example.elicesecondproject.mall.domain.category.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CategoryResponse {
    private Long id;
    private String name;
    private String slug;
    private Integer displayOrder;
    private Boolean isVisible;
    private String description;
}
