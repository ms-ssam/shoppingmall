package com.example.elicesecondproject.mall.domain.option.mapper;

import com.example.elicesecondproject.mall.domain.option.dto.OptionDetailDto;
import com.example.elicesecondproject.mall.domain.option.dto.OptionDetailIdNameResponse;
import com.example.elicesecondproject.mall.domain.option.dto.ProductOptionGroupDto;
import com.example.elicesecondproject.mall.domain.option.dto.ProductOptionGroupIdNameResponse;
import com.example.elicesecondproject.mall.domain.option.entity.OptionDetail;
import com.example.elicesecondproject.mall.domain.option.entity.ProductOptionGroup;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OptionMapper {

    ProductOptionGroupDto toDto(ProductOptionGroup group);
    OptionDetailDto toDto(OptionDetail detail);


    ProductOptionGroup toEntity(ProductOptionGroupDto dto);
    OptionDetail toEntity(OptionDetailDto dto);

    OptionDetailIdNameResponse toOptionDetailIdNameResponse(OptionDetail detail);
    ProductOptionGroupIdNameResponse toProductOptionGroupIdNameResponse(ProductOptionGroup group);

    default List<OptionDetailIdNameResponse> toOptionDetailIdNameList(List<OptionDetail> details) {
        return details.stream()
                .map(this::toOptionDetailIdNameResponse)
                .toList();
    }

    default List<ProductOptionGroupIdNameResponse> toOptionGroupIdNameList(List<ProductOptionGroup> group) {
        return group.stream()
                .map(this::toProductOptionGroupIdNameResponse)
                .toList();
    }

}