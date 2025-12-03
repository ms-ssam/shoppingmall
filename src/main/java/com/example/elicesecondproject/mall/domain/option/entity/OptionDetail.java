package com.example.elicesecondproject.mall.domain.option.entity;

import com.example.elicesecondproject.mall.global.entity.BaseEntity;
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
@NoArgsConstructor
public class OptionDetail extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_option_group_id", nullable = false)
    private ProductOptionGroup productOptionGroup;

    @NotBlank(message = "상세 옵션명은 필수입니다.") //사이즈 넣기
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "SKU는 필수입니다.") //sku 는 프론트에서 받아오기(중복 유효성 검사 O)
    @Column(nullable = false, unique = true, length = 100)
    private String sku;

    @NotNull(message = "추가 금액은 필수입니다.")
    @Min(value = 0, message = "추가 금액은 0원 이상이어야 합니다.")
    @Column(nullable = false)
    private Integer addPrice;

    @NotNull(message = "재고 수량은 필수입니다.")
    @Min(value = 0, message = "재고 수량은 0개 이상이어야 합니다.")
    @Column(nullable = false)
    private Integer stockQuantity; //TODO: 재고 이력 관리 하기

    @NotNull(message = "정렬 순서는 필수입니다.")
    @Column(nullable = false)
    private Integer displayOrder;

    @Version
    private Long version; // 낙관적 락

    private LocalDateTime deletedAt;

    @Builder
    public OptionDetail(String name, String sku, Integer addPrice, Integer stockQuantity, Integer displayOrder) {
        this.name = name;
        this.sku = sku;
        this.addPrice = addPrice;
        this.stockQuantity = stockQuantity;
        this.displayOrder = displayOrder != null ? displayOrder : 0;
    }



    // 부모 설정 편의 메서드 변경
    public void initProductOptionGroup(ProductOptionGroup productOptionGroup) {
        this.productOptionGroup = productOptionGroup;
    }



    public void removeStock(int quantity) {
        int restStock = this.stockQuantity - quantity;
        if (restStock < 0) {
            throw new BusinessException(ErrorCode.NOT_ENOUGH_STOCK);
        }
        this.stockQuantity = restStock;

        //상품의 전체 재고 자동 재계산
        if (this.productOptionGroup != null && this.productOptionGroup.getProduct() != null) {
            this.productOptionGroup.getProduct().recalculateTotalStock();
        }
    }

    public void addStock(int quantity) {
        if (quantity <= 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        this.stockQuantity += quantity;
        if (this.productOptionGroup != null && this.productOptionGroup.getProduct() != null) {
            this.productOptionGroup.getProduct().recalculateTotalStock();
        }
    }

    public void update(String name, String sku, Integer addPrice, Integer stockQuantity, Integer displayOrder) {
        this.name = name;
        this.sku = sku;
        this.addPrice = addPrice;
        this.stockQuantity = stockQuantity;
        this.displayOrder = displayOrder;
    }
    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }
}