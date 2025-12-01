package com.example.elicesecondproject.mall.domain.inventory.dto;

import com.example.elicesecondproject.mall.domain.inventory.entity.InventoryChangeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryHistoryDto {

    private Long id;

    // 상품 식별 정보
    private Long productId;
    private String productName;
    private String optionGroupName;

    // 옵션 식별 정보
    private Long optionDetailId;
    private String optionDetailName;
    private String sku;

    // 변동 내용
    private Integer changeAmount;
    private Integer stockAfterChange;

    private InventoryChangeType changeType;
    private String description;
    private String performedBy;
    private String relatedOrderId;
    private LocalDateTime createdAt;
}