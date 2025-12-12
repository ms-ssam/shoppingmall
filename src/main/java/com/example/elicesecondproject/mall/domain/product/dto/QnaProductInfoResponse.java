package com.example.elicesecondproject.mall.domain.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class QnaProductInfoResponse {
    private Long id;
    private String name;
    private String thumbnailUrl;
}

