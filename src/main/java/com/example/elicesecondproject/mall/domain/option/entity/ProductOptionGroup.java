package com.example.elicesecondproject.mall.domain.option.entity;

import com.example.elicesecondproject.mall.domain.product.entity.Product;
import com.example.elicesecondproject.mall.global.entity.SoftDeletableBaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;

import java.util.ArrayList;
import java.util.List;


@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "product_option_groups")
public class ProductOptionGroup extends SoftDeletableBaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @BatchSize(size = 100)
    @OneToMany(mappedBy = "productOptionGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OptionDetail> details = new ArrayList<>();
/*
구조적합성: ProductOptionGroup을 '색상' 단위로, OptionDetail을 '사이즈' 단위로 사용해서 계층형 선택 및 조합형 재고 관리
*/
    //색상' 단위 (예: Red, Blue)
    @NotBlank(message = "옵션명은 필수입니다.")
    @Column(nullable = false)
    private String name; //

    @NotNull(message = "정렬 순서는 필수입니다.")
    @Column(nullable = false)
    private Integer displayOrder;



    @Builder
    public ProductOptionGroup(String name, Integer displayOrder) {
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

    /* 소프트 delete 필요 없음 -> 대신 order 단에서 스냅샷 필요
    public void delete() {
        //this.deletedAt = LocalDateTime.now();
        this.softDelete();
        for (OptionDetail detail : details) {
            detail.delete();
        }

    }*/
    public void update(String name, Integer displayOrder) {
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
        if (displayOrder != null) {
            this.displayOrder = displayOrder;
        }
    }


    public void softDelete() {
        super.softDelete(); // 자신의 deletedAt 설정
        for (OptionDetail detail : details) {
            detail.softDelete(); // 자식(OptionDetail)들에게 전파
        }
    }

    public List<OptionDetail> getOptionDetails(){
        return this.details;
    }
}
