package com.example.elicesecondproject.mall.domain.product.repository;

import com.example.elicesecondproject.mall.domain.product.entity.ProductImage;

import java.util.List;

public interface ProductImageRepositoryCustom {

    /**
     * 슬라이더 이미지 조회 (MAIN + SLIDER)
     * MAIN은 displayOrder=0으로 먼저 오고, SLIDER는 displayOrder 순으로 정렬
     */
    List<ProductImage> findSliderImagesByProductId(Long productId);


    /**
     * 상품의 모든 이미지 Soft Delete
     */
    long softDeleteByProductId(Long productId);
}
