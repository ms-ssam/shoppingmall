package com.example.elicesecondproject.mall.domain.product.entity;

import com.example.elicesecondproject.mall.domain.option.entity.ProductOptionGroup;
import com.example.elicesecondproject.mall.global.entity.SoftDeletableBaseEntity;
import com.example.elicesecondproject.mall.global.exception.BusinessException;
import com.example.elicesecondproject.mall.global.exception.ErrorCode;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
public class ProductImage extends SoftDeletableBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // 옵션 색상 이미지 삭제
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "product_option_group_id") // 색상별 이미지
//    private ProductOptionGroup productOptionGroup;

    @NotBlank(message = "이미지 URL은 필수입니다.") // 유효성 검증 추가
    @Column(nullable = false, length = 500) // URL 길이 제한
    private String imageUrl;

    @NotNull(message = "이미지 타입은 필수입니다.")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ImageType imageType;

    @NotNull(message = "정렬 순서는 필수입니다.")
    @Column(nullable = false)
    private Integer displayOrder;

    //private LocalDateTime deletedAt;

    @Builder
    public ProductImage(String imageUrl, ImageType imageType, Integer displayOrder,
                        ProductOptionGroup productOptionGroup) {
        if (imageUrl == null || imageUrl.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_IMAGE_URL);
        }
        if (imageType == null) {
            throw new BusinessException(ErrorCode.INVALID_IMAGE_FORMAT);
        }
        this.imageUrl = imageUrl;
        this.imageType = imageType;
        this.displayOrder = displayOrder != null ? displayOrder : 0;
//        this.productOptionGroup = productOptionGroup;
    }

    public void initProduct(Product product) {
        this.product = product;
    }


    public void updateImageInfo(String imageUrl, ImageType imageType, Integer displayOrder) {
        if (imageUrl != null && !imageUrl.isBlank()) {
            this.imageUrl = imageUrl;
        }
        if (imageType != null) {
            this.imageType = imageType;
        }
        if (displayOrder != null) {
            this.displayOrder = displayOrder;
        }
    }
    public String getThumbnailUrl() {
        return imageUrl;
    }
    public String getResizedUrl() {
        // DESCRIPTION은 DB에 이미 resized 경로 저장됨
        if (imageType == ImageType.DESCRIPTION) {
            return imageUrl;
        }

        // thumbnail → resized 변환
        if (imageUrl != null && imageUrl.contains("/thumbnail/")) {
            return imageUrl.replace("/thumbnail/", "/resized/");
        }

        return imageUrl;
    }
    public boolean isSliderImage() {
        return imageType == ImageType.MAIN || imageType == ImageType.SLIDER;
    }

}

/*    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }*/

