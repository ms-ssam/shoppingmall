package com.example.elicesecondproject.mall.domain.product.entity;


import com.example.elicesecondproject.mall.domain.category.entity.Category;
import com.example.elicesecondproject.mall.domain.option.entity.ProductOptionGroup;
import com.example.elicesecondproject.mall.domain.review.entity.Review;
import com.example.elicesecondproject.mall.global.entity.SoftDeletableBaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;

import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "product", indexes = {
        @Index(name = "idx_product_category_id", columnList = "category_id"),
        @Index(name = "idx_product_status", columnList = "status"),
        @Index(name = "idx_product_name", columnList = "name")
})
@Getter
@NoArgsConstructor
@Table(name = "products")
public class Product extends SoftDeletableBaseEntity { // Basetime -> sofrDeletable로 수정했습니다.

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<Review> reviews = new ArrayList<>();

    // [옵션 구조] 상품 -> 옵션 그룹 -> 상세 옵션]
    @BatchSize(size = 100)
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductOptionGroup> optionGroups = new ArrayList<>();

    // 썸네일 이미지
    private String thumbnailUrl;


    // [이미지 구조] 성능 최적화를 위한 BatchSize 적용
    @BatchSize(size = 100)
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL,orphanRemoval = true)
    private List<ProductImage> images;

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

    private double averageRating = 0.0;
    private int reviewCount = 0;
    private int wishListCount = 0;

    @Column(nullable = false)
    private Integer totalStock = 0;

    @Version
    private Long version; // 관리자 동시 수정 방지 낙관적 락

    // private LocalDateTime deletedAt; // softDeletableBaseEntity 상속받음.
    public Product(String name, int price, int discountRate, String description,
                   Category category, ProductStatus status) {
        this.name = name;
        this.price = price;
        this.discountRate = discountRate;
        this.description = description;
        this.category = category;
        this.status = status != null ? status : ProductStatus.SELLING;

        this.optionGroups = new ArrayList<>();
        this.images = new ArrayList<>();
    }

    // --- 비즈니스 메서드 ---

    public void addReview(Review review) {
        reviews.add(review);
        review.setProduct(this);
    }

    // 썸네일 이미지 업데이트
    public void updateThumbnailUrl(String thumbnailUrl){
        this.thumbnailUrl = thumbnailUrl;
    }



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
    public void updateDetails(String name, int price, int discountRate, String description, ProductStatus status) {
        this.name = name;
        this.price = price;
        this.discountRate = discountRate;
        this.description = description;
        this.status = status;
    }


    public void addOptionGroup(ProductOptionGroup group) {
        this.optionGroups.add(group);
        // 자식 엔티티가 이미 나를 참조하고 있지 않은 경우에만 설정 (무한 루프 방지)
        if (group.getProduct() != this) {
            group.initProduct(this);
        }
    }


    public void addImage(ProductImage image) {
        this.images.add(image);
        if (image.getProduct() != this) {
            image.initProduct(this);
        }
    }

    // 평점 업데이트
    public void updateRatingAndReviewCount(double averageRating, int reviewCount) {
        this.averageRating = Math.round(averageRating * 10.0) / 10.0;
        this.reviewCount = reviewCount;
    }

    public void updateRating(double averageRating) {
        this.averageRating = Math.round(averageRating * 10.0) / 10.0;
    }

    // 좋아요 수 업데이트
    public void updateWishListCount(int wishListCount) {
        this.wishListCount = wishListCount;
    }

    // 좋아요 개수 증가
    public void increaseWishListCount() {
        this.wishListCount++;
    }

    // 좋아요 수 차감
    public void decreaseWishListCount() {
        if(this.wishListCount > 0) {
            this.wishListCount--;
        }
    }

    // 대표 이미지 URL 추출
    public String getMainImageUrl() {
        return this.images.stream()
                .filter(img -> img.getImageType() == ImageType.MAIN)
                .findFirst()
                .map(ProductImage::getImageUrl)
                .orElse(null);
    }

    // 삭제 비즈니스 로직, 자식들도 같이 논리 삭제 처리
    public void delete() {
        //this.deletedAt = LocalDateTime.now();
        this.softDelete();
        this.status = ProductStatus.STOP;
        this.images.forEach(ProductImage::softDelete);
    }

    public int getSalePrice() {
        if (this.discountRate <= 0) return this.price;
        return this.price - (int) (((long) this.price * this.discountRate) / 100);
    }

    // 품절 여부 반환
    public boolean isSoldOut() {
        return this.status == ProductStatus.SOLD_OUT;
    }

    // 판매중인 상품인지 여부 반환
    public boolean isOnSale() {
        return this.status == ProductStatus.SELLING;
    }

    // 판매 중단 상품인지 여부 반환
    public boolean isNotOnSale() {
        return this.status == ProductStatus.STOP;
    }

    public void updateCategory(Category category){
        this.category = category;
    }
    // 상태 변경
    public void updateStatus(ProductStatus status) {
        this.status = status;
    }

    //재고 합계 재계산 + 자동 품절 처리
    public void recalculateTotalStock() {
        this.totalStock = optionGroups.stream()
                .filter(group -> group.getDeletedAt() == null)
                .flatMap(group -> group.getDetails().stream())
                .filter(detail -> detail.getDeletedAt() == null)
                .mapToInt(detail -> detail.getStockQuantity())
                .sum();

        // 자동 품절 처리
        if (this.totalStock == 0 && this.status == ProductStatus.SELLING) {
            this.status = ProductStatus.SOLD_OUT;
        }
        // 재고 복구 시 판매중으로 자동 전환
        else if (this.totalStock > 0 && this.status == ProductStatus.SOLD_OUT) {
            this.status = ProductStatus.SELLING;
        }

    }

    public List<ProductOptionGroup> getProductOptionGroups(){
        return this.optionGroups;
    }
}
