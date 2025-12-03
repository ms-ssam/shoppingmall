package com.example.elicesecondproject.mall.domain.product.repository;

import com.example.elicesecondproject.mall.domain.product.entity.ProductImage;

import java.util.List;

public interface ProductImageRepositoryCustom {

    List<ProductImage> findSliderImagesByProductId(Long productId);



}
