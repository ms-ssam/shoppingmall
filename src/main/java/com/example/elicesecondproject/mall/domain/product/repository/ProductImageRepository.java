package com.example.elicesecondproject.mall.domain.product.repository;

import com.example.elicesecondproject.mall.domain.product.entity.ImageType;
import com.example.elicesecondproject.mall.domain.product.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long>{

    //상품 ID로 이미지 조회 (삭제되지 않은 것만)
    List<ProductImage> findByProductIdAndDeletedAtIsNull(Long productId);




}
