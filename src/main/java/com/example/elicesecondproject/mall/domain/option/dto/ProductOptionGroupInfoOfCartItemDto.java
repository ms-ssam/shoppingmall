package com.example.elicesecondproject.mall.domain.option.dto;

import com.example.elicesecondproject.mall.domain.option.entity.ProductOptionGroup;
import lombok.Builder;
import lombok.Getter;

// 장바구니 화면의 장바구니 항목에 보여줄 정보 용도입니다.
@Getter
public class ProductOptionGroupInfoOfCartItemDto {
    private long id;
    private String name;

    @Builder
    private ProductOptionGroupInfoOfCartItemDto(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public static ProductOptionGroupInfoOfCartItemDto of(ProductOptionGroup productOptionGroup) {
        return ProductOptionGroupInfoOfCartItemDto.builder()
                .id(productOptionGroup.getId())
                .name(productOptionGroup.getName())
                .build();
    }
}
