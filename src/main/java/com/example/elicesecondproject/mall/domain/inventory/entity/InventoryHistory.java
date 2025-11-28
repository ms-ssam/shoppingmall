package com.example.elicesecondproject.mall.domain.inventory.entity;

import com.example.elicesecondproject.mall.global.entity.BaseEntity;
import com.example.elicesecondproject.mall.domain.option.entity.OptionDetail;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

@Entity
@Getter
@Immutable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "inventory_history",
        indexes = {
                @Index(name = "idx_option_detail_created", columnList = "option_detail_id, created_at"),
                @Index(name = "idx_change_type_created", columnList = "change_type, created_at"),
                @Index(name = "idx_related_order", columnList = "related_order_id")
        }
)
public class InventoryHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "옵션 정보는 필수입니다.")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_detail_id", nullable = false)
    private OptionDetail optionDetail;

    @NotNull(message = "변동 수량은 필수입니다.")
    @Column(nullable = false)
    private Integer changeAmount; // 양수: 입고/반품, 음수: 주문/폐기

    @NotNull(message = "변경 후 재고는 필수입니다.")
    @Min(value = 0, message = "재고는 0보다 작을 수 없습니다.")
    @Column(nullable = false)
    private Integer stockAfterChange; // 스냅샷

    @NotNull(message = "변동 타입은 필수입니다.")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private InventoryChangeType changeType;

    @Column(length = 500)
    private String description;

    @Column(length = 100)
    private String performedBy; // 작업자

    @Column(length = 100)
    private String relatedOrderId; // 관련 주문 ID

    @Builder
    public InventoryHistory(OptionDetail optionDetail,
                            Integer changeAmount,
                            Integer stockAfterChange,
                            InventoryChangeType changeType,
                            String description,
                            String performedBy,
                            String relatedOrderId) {
        this.optionDetail = optionDetail;
        this.changeAmount = changeAmount;
        this.stockAfterChange = stockAfterChange;
        this.changeType = changeType;
        this.description = description;
        this.performedBy = performedBy != null ? performedBy : "SYSTEM";
        this.relatedOrderId = relatedOrderId;
    }


    public static InventoryHistory createHistory(
            OptionDetail optionDetail,
            InventoryChangeType changeType,
            int changeAmount,
            int stockAfterChange,
            String description,
            String performedBy,
            String relatedOrderId) {

        return InventoryHistory.builder()
                .optionDetail(optionDetail)
                .changeType(changeType)
                .changeAmount(changeAmount)
                .stockAfterChange(stockAfterChange)
                .description(description)
                .performedBy(performedBy)
                .relatedOrderId(relatedOrderId)
                .build();
    }
}