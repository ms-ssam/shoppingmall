package com.example.elicesecondproject.mall.domain.inventory.mapper;

import com.example.elicesecondproject.mall.domain.inventory.dto.InventoryHistoryDto;
import com.example.elicesecondproject.mall.domain.inventory.entity.InventoryHistory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;


public interface InventoryMapper {

    /**
     * InventoryHistory(Entity) -> InventoryHistoryDto(DTO)
     * 깊은 객체 탐색(Deep Mapping)을 통해 연관된 상품 및 옵션 정보를 평탄화(Flatten)하여 매핑합니다.
     */

    // 1. [상품 정보] History -> OptionDetail -> OptionGroup -> Product
    @Mapping(source = "optionDetail.productOptionGroup.product.id", target = "productId")
    @Mapping(source = "optionDetail.productOptionGroup.product.name", target = "productName")

    // 2. [옵션 그룹 정보] History -> OptionDetail -> OptionGroup
    @Mapping(source = "optionDetail.productOptionGroup.name", target = "optionGroupName")

    // 3. [옵션 상세 정보] History -> OptionDetail
    @Mapping(source = "optionDetail.id", target = "optionDetailId")
    @Mapping(source = "optionDetail.name", target = "optionDetailName")
    @Mapping(source = "optionDetail.sku", target = "sku")
    InventoryHistoryDto toDto(InventoryHistory history);


}