package com.example.elicesecondproject.mall.domain.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Builder
public class ReviewProductInfoDto {
    private Long id;
    private String name;
    private String thumbnailUrl;
}
