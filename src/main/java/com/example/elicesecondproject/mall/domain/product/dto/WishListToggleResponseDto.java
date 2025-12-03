package com.example.elicesecondproject.mall.domain.product.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class WishListToggleResponseDto {
    private boolean isWished;  // true: 현재 찜 상태, false: 현재 찜 해제(안 된) 상태
    private long wishListCount;  // 해당 상품의 전체 찜 개수
}
