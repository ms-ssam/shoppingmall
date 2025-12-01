package com.example.elicesecondproject.mall.domain.option.mapper;

import com.example.elicesecondproject.mall.domain.option.dto.OptionDetailDto;
import com.example.elicesecondproject.mall.domain.option.dto.ProductOptionGroupDto;
import com.example.elicesecondproject.mall.domain.option.entity.OptionDetail;
import com.example.elicesecondproject.mall.domain.option.entity.ProductOptionGroup;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OptionMapper {

    ProductOptionGroupDto toDto(ProductOptionGroup group);
    OptionDetailDto toDto(OptionDetail detail);

    ProductOptionGroup toEntity(ProductOptionGroupDto dto);
    OptionDetail toEntity(OptionDetailDto dto);
}