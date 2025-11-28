package com.example.elicesecondproject.mall.product.domain;

import com.example.elicesecondproject.mall.global.entity.BaseTimeEntity;
import com.example.elicesecondproject.mall.global.exception.BusinessException;
import com.example.elicesecondproject.mall.global.exception.ErrorCode;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OptionDetail extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // [수정] 부모 엔티티 타입 및 필드명 변경 (ProductOption -> ProductOptionGroup)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_option_group_id", nullable = false)
    private ProductOptionGroup productOptionGroup;

    @NotBlank(message = "상세 옵션명은 필수입니다.")
    @Column(nullable = false)
    private String name; // 예: "Large", "270", "Free"

    @NotNull(message = "추가 금액은 필수입니다.")
    @Min(value = 0, message = "추가 금액은 0원 이상이어야 합니다.")
    @Column(nullable = false)
    private Integer addPrice;

    @NotNull(message = "재고 수량은 필수입니다.")
    @Min(value = 0, message = "재고 수량은 0개 이상이어야 합니다.")
    @Column(nullable = false)
    private Integer stockQuantity;

    @NotNull(message = "정렬 순서는 필수입니다.")
    @Column(nullable = false)
    private Integer displayOrder;

    @Version
    private Long version; // 낙관적 락

    private LocalDateTime deletedAt;

    // [수정] 생성자 이름 변경 (ProductDetail -> OptionDetail)
    @Builder
    public OptionDetail(String name, Integer addPrice, Integer stockQuantity, Integer displayOrder) {
        if (addPrice < 0) throw new IllegalArgumentException("추가 금액은 0원 이상이어야 합니다.");
        if (stockQuantity < 0) throw new IllegalArgumentException("재고 수량은 0개 이상이어야 합니다.");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("상세 옵션명은 필수입니다.");

        this.name = name;
        this.addPrice = addPrice;
        this.stockQuantity = stockQuantity;
        this.displayOrder = displayOrder != null ? displayOrder : 0;
    }

    // [수정] 부모 설정 편의 메서드 변경
    public void initProductOptionGroup(ProductOptionGroup productOptionGroup) {
        this.productOptionGroup = productOptionGroup;
    }

    public void removeStock(int quantity) {
        int restStock = this.stockQuantity - quantity;
        if (restStock < 0) {
            throw new BusinessException(ErrorCode.NOT_ENOUGH_STOCK);
        }
        this.stockQuantity = restStock;
    }

    public void addStock(int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("재고 증가량은 0보다 커야 합니다.");
        this.stockQuantity += quantity;
    }

    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }
}