package com.example.elicesecondproject.mall.domain.category.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CategoryTreeResponse {
    private Long id;
    private String name;
    private String slug;
    private Integer displayOrder;
    private Boolean isVisible;
    private Integer depth;
    private List<CategoryTreeResponse> children;
}
