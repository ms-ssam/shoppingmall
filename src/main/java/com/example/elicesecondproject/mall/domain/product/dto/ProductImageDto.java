package com.example.elicesecondproject.mall.domain.product.dto;

import com.example.elicesecondproject.mall.domain.product.entity.ImageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductImageDto {

    private Long id;

    @NotBlank(message = "이미지 URL은 필수입니다.")
    private String imageUrl;

    @NotNull(message = "이미지 타입은 필수입니다.")
    private ImageType imageType;

    @NotNull(message = "정렬 순서는 필수입니다.")
    private Integer displayOrder;

    private Long productOptionGroupId;
}

