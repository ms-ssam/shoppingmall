package com.example.elicesecondproject.mall.domain.inventory.entity;

import com.example.elicesecondproject.mall.global.entity.BaseEntity;
import com.example.elicesecondproject.mall.domain.option.entity.OptionDetail;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;
import org.springframework.util.Assert;


@Entity
@Getter
@Immutable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "inventory_history",
        indexes = {
                @Index(name = "idx_option_detail_created",
                        columnList = "option_detail_id, created_at"),
                @Index(name = "idx_change_type_created",
                        columnList = "change_type, created_at"),
                @Index(name = "idx_related_order",
                        columnList = "related_order_id")
        }
)
public class InventoryHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_detail_id", nullable = false)
    private OptionDetail optionDetail;

    @Column(nullable = false)
    private Integer changeAmount; // 양수: 증가, 음수: 감소

    @Column(nullable = false)
    private Integer stockAfterChange; // 변경 후 재고 (스냅샷)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private InventoryChangeType changeType;

    @Column(length = 500)
    private String description;

    @Column(length = 100)
    private String performedBy; // 작업자 (userId 또는 "SYSTEM")

    @Column(length = 100)
    private String relatedOrderId; // 관련 주문 ID (주문/취소의 경우)

    @Builder
    public InventoryHistory(OptionDetail optionDetail,
                            Integer changeAmount,
                            Integer stockAfterChange,
                            InventoryChangeType changeType,
                            String description,
                            String performedBy,
                            String relatedOrderId) {
        // 필수 값 검증
        Assert.notNull(optionDetail, "상품 상세 정보는 필수입니다.");
        Assert.notNull(changeAmount, "변동 수량은 필수입니다.");
        Assert.notNull(stockAfterChange, "변경 후 재고는 필수입니다.");
        Assert.notNull(changeType, "변동 타입은 필수입니다.");

        // 비즈니스 규칙 검증
        if (stockAfterChange < 0) {
            throw new IllegalArgumentException(
                    String.format("변경 후 재고는 음수일 수 없습니다. (현재 값: %d)", stockAfterChange)
            );
        }

        this.optionDetail = optionDetail;
        this.changeAmount = changeAmount;
        this.stockAfterChange = stockAfterChange;
        this.changeType = changeType;
        this.description = description;
        this.performedBy = performedBy != null ? performedBy : "SYSTEM";
        this.relatedOrderId = relatedOrderId;
    }

    /**
     * 재고 이력 생성 (주문/취소용)
     *
     * @param optionDetail 옵션 상세 정보
     * @param changeType 변동 타입
     * @param changeAmount 변동 수량 (증가: 양수, 감소: 음수)
     * @param stockAfterChange 변경 후 재고 (명시적으로 전달)
     * @param description 변동 사유
     * @param performedBy 작업자 (userId 또는 "SYSTEM")
     * @param relatedOrderId 관련 주문 ID
     * @return 생성된 재고 이력
     */
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


    public static InventoryHistory createStockHistory(
            OptionDetail optionDetail,
            InventoryChangeType changeType,
            int changeAmount,
            int stockAfterChange,
            String description,
            String performedBy) {

        return createHistory(
                optionDetail,
                changeType,
                changeAmount,
                stockAfterChange,
                description,
                performedBy,
                null
        );
    }
}
