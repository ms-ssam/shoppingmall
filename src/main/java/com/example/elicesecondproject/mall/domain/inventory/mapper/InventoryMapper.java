package com.example.elicesecondproject.mall.domain.inventory.mapper;

import com.example.elicesecondproject.mall.domain.inventory.dto.InventoryHistoryDto;
import com.example.elicesecondproject.mall.domain.inventory.entity.InventoryHistory;
import org.mapstruct.Mapping;


public interface InventoryMapper {



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