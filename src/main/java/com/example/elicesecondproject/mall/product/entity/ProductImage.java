package com.example.elicesecondproject.mall.product.entity;

import com.example.elicesecondproject.mall.global.entity.BaseEntity;
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
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 접근 제한자 추가
public class ProductImage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

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

    private LocalDateTime deletedAt;

    @Builder
    public ProductImage(String imageUrl, ImageType imageType, Integer displayOrder) {
        // 방어적 검증 로직 추가 [web:13]
        if (imageUrl == null || imageUrl.isBlank()) {
            throw new IllegalArgumentException("이미지 URL은 필수입니다.");
        }
        if (imageType == null) {
            throw new IllegalArgumentException("이미지 타입은 필수입니다.");
        }

        this.imageUrl = imageUrl;
        this.imageType = imageType;
        this.displayOrder = displayOrder != null ? displayOrder : 0;
    }

    public void initProduct(Product product) {
        this.product = product;
    }

    // 비즈니스 메서드 추가
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

    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }
}
