package com.example.elicesecondproject.mall.product.domain;

import com.example.elicesecondproject.mall.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductOptionGroup extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // [성능 최적화] 하위 상세 옵션 조회 시 BatchSize 적용
    @BatchSize(size = 100)
    // [수정] mappedBy 대상 일치화 (OptionDetail 클래스의 필드명 'productOptionGroup')
    @OneToMany(mappedBy = "productOptionGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OptionDetail> details = new ArrayList<>();

    @NotBlank(message = "옵션명은 필수입니다.")
    @Column(nullable = false)
    private String name; // 예: "Red", "Blue", "Standard"

    @NotNull(message = "정렬 순서는 필수입니다.")
    @Column(nullable = false)
    private Integer displayOrder;

    private LocalDateTime deletedAt;

    @Builder
    public ProductOptionGroup(String name, Integer displayOrder) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("옵션명은 필수입니다.");
        }
        this.name = name;
        this.displayOrder = displayOrder != null ? displayOrder : 0;
    }

    public void initProduct(Product product) {
        this.product = product;
    }

    // [안전성 강화] 양방향 연관관계 편의 메서드
    // [수정] 파라미터 타입 변경: ProductDetail -> OptionDetail
    public void addDetail(OptionDetail detail) {
        this.details.add(detail);
        if (detail.getProductOptionGroup() != this) {
            detail.initProductOptionGroup(this);
        }
    }

    // [비즈니스 로직] 옵션 삭제 (Cascade Soft Delete)
    public void delete() {
        this.deletedAt = LocalDateTime.now();
        for (OptionDetail detail : details) {
            detail.delete();
        }
    }
}
/*
@Entity
@Getter
@NoArgsConstructor
public class ProductOption extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private ProductOptionGroup optionGroup;

    @Column(nullable = false)
    private String name; // 예: "Red", "Large"

    @Column(nullable = false)
    private Integer addPrice; // 기본가 대비 추가 금액 (+500원 등)

    @Column(nullable = false)
    private Integer stockQuantity;

    @Column(nullable = false)
    private Integer displayOrder;

    // 동시성 제어를 위한 낙관적 락 버전 정보
    @Version
    private Long version;

    private LocalDateTime deletedAt;

    @Builder
    public ProductOption(String name, Integer addPrice, Integer stockQuantity, Integer displayOrder) {
        this.name = name;
        this.addPrice = addPrice;
        this.stockQuantity = stockQuantity;
        this.displayOrder = displayOrder;
    }

    public void initOptionGroup(ProductOptionGroup group) {
        this.optionGroup = group;
    }

    // 재고 감소 로직 (동시성 제어 포인트)
    public void removeStock(int quantity) {
        int restStock = this.stockQuantity - quantity;
        if (restStock < 0) {
            throw new BusinessException(ErrorCode.NOT_ENOUGH_STOCK);
        }
        this.stockQuantity = restStock;
    }

    public void addStock(int quantity) {
        this.stockQuantity += quantity;
    }
}
*/

/*
 [Design & Implementation Note]

 1. 동시성 문제 (Concurrency Issue)
    - 주문이 동시에 몰릴 때 `stockQuantity` 차감 시 경쟁 상태(Race Condition)가 발생합니다.
    - 해결책: `@Version`을 사용한 **낙관적 락(Optimistic Lock)**을 적용했습니다.
    - 동작 원리: 트랜잭션 커밋 시점에 `version`을 체크하여, 읽은 시점과 다르면 예외(OptimisticLockingFailureException)를 발생시키고 롤백합니다.
    - 장점: DB Lock을 걸지 않아 성능이 좋음.
    - 단점: 충돌이 잦으면 재시도 로직을 작성해야 함. (일반 쇼핑몰 수준에서는 적절)
    - 대안: 초고가 한정판 판매 등 충돌이 극심하면 `Pessimistic Lock (SELECT ... FOR UPDATE)` 사용 고려.

 2. 재고 관리 단위
    - 현재 구조는 '단일 옵션'별 재고 관리입니다. (색상 따로, 사이즈 따로)
    - 만약 "빨강/Large"라는 **조합(Combination)**에 대한 재고를 관리해야 한다면?
      -> 현재 구조로는 불가능합니다. 이 구조는 단순 옵션 나열형입니다.
      -> **조합형 재고(SKU)** 관리가 필요하다면, `Product -> ProductOption` (그룹 없이 Flatten) 구조나
         `OptionCombination` 테이블을 별도로 두어야 합니다.
      -> *User Requirement Check*: 질문자님께서 그룹 구조를 원하셨으므로 현재는 '각 옵션별' 관리 구조입니다.
         (예: 빨강 재고 10개, 사이즈 L 재고 10개 -> 빨강/L 주문 시 각각 차감되는 방식)
*/