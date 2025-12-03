package com.example.elicesecondproject.mall.domain.product.repository;

import com.example.elicesecondproject.mall.domain.product.entity.ImageType;
import com.example.elicesecondproject.mall.domain.product.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long>, ProductImageRepositoryCustom {

    //상품 ID로 이미지 조회 (삭제되지 않은 것만)
    List<ProductImage> findByProductIdAndDeletedAtIsNull(Long productId);

    /**
     * 상품 ID와 이미지 타입으로 조회 (삭제되지 않은 것만)
     */
    List<ProductImage> findByProductIdAndImageTypeAndDeletedAtIsNull(
            Long productId, ImageType imageType);

    /**
     * 상품 ID와 이미지 타입으로 조회 + displayOrder 정렬
     */
    List<ProductImage> findByProductIdAndImageTypeAndDeletedAtIsNullOrderByDisplayOrderAsc(
            Long productId, ImageType imageType);



    Optional<ProductImage> findFirstByProductIdAndImageTypeAndDeletedAtIsNull(
            Long productId, ImageType imageType);


}
