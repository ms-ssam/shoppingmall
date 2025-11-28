package com.example.elicesecondproject.mall.domain.product.entity;


import com.example.elicesecondproject.mall.domain.category.entity.Category;
import com.example.elicesecondproject.mall.global.entity.BaseEntity;
import com.example.elicesecondproject.mall.domain.option.entity.ProductOptionGroup;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    // [옵션 구조] 상품 -> 옵션 그룹 -> 상세 옵션
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductOptionGroup> optionGroups = new ArrayList<>();

    // [이미지 구조] 성능 최적화를 위한 BatchSize 적용
    @BatchSize(size = 100)
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductImage> images = new ArrayList<>();

    @NotBlank(message = "상품명은 필수입니다.")
    @Column(nullable = false, length = 200)
    private String name;

    @NotNull(message = "가격은 필수입니다.")
    @Min(value = 0, message = "가격은 0원 이상이어야 합니다.")
    @Column(nullable = false)
    private int price; // 정가 기본 가격 -> 옵션마다 추가 금액이 있는 방식

    @Min(value = 0, message = "할인율은 0% 이상이어야 합니다.")
    @Max(value = 100, message = "할인율은 100% 이하여야 합니다.")
    @Column(nullable = false)
    private int discountRate; // 할인율 (%)

    @NotNull(message = "판매 상태는 필수입니다.")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status;

    @Column(columnDefinition = "TEXT")
    private String description;

    // 반정규화 필드 (성능용)
    private double averageRating = 0.0;
    private int reviewCount = 0;
    private int likeCount = 0;

    @Version
    private Long version; // 관리자 동시 수정 방지 낙관적 락

    private LocalDateTime deletedAt;

    @Builder
    public Product(String name, int price, int discountRate, String description,
                   Category category, ProductStatus status) {
        this.name = name;
        this.price = price;
        this.discountRate = discountRate;
        this.description = description;
        this.category = category;
        this.status = status != null ? status : ProductStatus.SELLING;
    }

    // --- 비즈니스 메서드 ---

    // 상품 정보 수정
    /* [수정 전: Before]
    public void updateDetails(String name, int price, int discountRate, String description, ProductStatus status) {
        this.name = name;
        this.price = price;
        this.discountRate = discountRate;
        this.description = description;
        this.status = status;
    }
    */
    // [수정 후: After] 유효성 검증 로직(Defensive Logic) 추가
    public void updateDetails(String name, int price, int discountRate, String description, ProductStatus status) {
        // 엔티티 내부 방어 로직으로 데이터 무결성 보호
        if (price < 0) {
            throw new IllegalArgumentException("가격은 0원 이상이어야 합니다.");
        }
        if (discountRate < 0 || discountRate > 100) {
            throw new IllegalArgumentException("할인율은 0~100 사이여야 합니다.");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("상품명은 필수입니다.");
        }

        this.name = name;
        this.price = price;
        this.discountRate = discountRate;
        this.description = description;
        this.status = status;
    }

    // 옵션 그룹 추가 (연관관계 편의 메서드)
    /* [수정 전: Before]
    public void addOptionGroup(ProductOptionGroup group) {
        this.optionGroups.add(group);
        group.initProduct(this);
    }
    */
    // [수정 후: After] 양방향 관계 안전성 강화
    public void addOptionGroup(ProductOptionGroup group) {
        this.optionGroups.add(group);
        // 자식 엔티티가 이미 나를 참조하고 있지 않은 경우에만 설정 (무한 루프 방지)
        if (group.getProduct() != this) {
            group.initProduct(this);
        }
    }

    // 이미지 추가 (연관관계 편의 메서드)
    /* [수정 전: Before]
    public void addImage(ProductImage image) {
        this.images.add(image);
        image.initProduct(this);
    }
    */
    // [수정 후: After] 양방향 관계 안전성 강화
    public void addImage(ProductImage image) {
        this.images.add(image);
        if (image.getProduct() != this) {
            image.initProduct(this);
        }
    }

    // 평점 업데이트
    public void updateRating(double averageRating, int reviewCount) {
        this.averageRating = Math.round(averageRating * 10.0) / 10.0;
        this.reviewCount = reviewCount;
    }

    // 좋아요 수 업데이트
    public void updateLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    // 대표 이미지 URL 추출 (편의 메서드)
    public String getMainImageUrl() {
        return this.images.stream()
                .filter(img -> img.getImageType() == ImageType.MAIN)
                .findFirst()
                .map(ProductImage::getImageUrl)
                .orElse(null);
    }

    // [추가] 삭제 비즈니스 로직
    public void delete() {
        this.deletedAt = LocalDateTime.now();
        this.status = ProductStatus.STOP; // 삭제 시 판매 중지 상태로 변경 (선택 사항)
    }

    // [추가] 실제 판매가 계산 로직
    public int getSalePrice() {
        if (this.discountRate <= 0) return this.price;
        return this.price - (this.price * this.discountRate / 100);
    }

    //todo:  equals, hashCode 오버라이드 (식별성 보장) 내용 고려하기  -> x
    //todo: 동시성 문제 -> 일단 낙관적 락으로 구현 후 레디스 캐싱으로 해결해보기
    //todo: 실시간 수정이랑 Batch 같이 사용하는 방식 공부 ->  오늘부터 전 품목 10% 할인 같은 경우 JdbcTemplate을 이용한 Bulk Update나 JPA Bulk Update (@Modifying) 쿼리를 사용하는 것이 좋습니다.
    //
    //요약

}
