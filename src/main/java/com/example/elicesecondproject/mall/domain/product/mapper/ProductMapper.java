package com.example.elicesecondproject.mall.domain.product.mapper;

import com.example.elicesecondproject.mall.domain.option.mapper.OptionMapper;
import com.example.elicesecondproject.mall.domain.product.dto.CreateProductRequest;
import com.example.elicesecondproject.mall.domain.product.dto.ProductDetailResponse;
import com.example.elicesecondproject.mall.domain.product.dto.ProductImageDto;
import com.example.elicesecondproject.mall.domain.product.dto.ProductSummaryDto;
import com.example.elicesecondproject.mall.domain.product.entity.Product;
import com.example.elicesecondproject.mall.domain.product.entity.ProductImage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

// uses = {OptionMapper.class} 필수!
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {OptionMapper.class})
public interface ProductMapper {

    @Mapping(target = "salePrice", expression = "java(product.getSalePrice())")
    @Mapping(target = "mainImageUrl", expression = "java(product.getMainImageUrl())")
    ProductSummaryDto toSummaryDto(Product product);

    @Mapping(target = "salePrice", expression = "java(product.getSalePrice())")
    @Mapping(target = "mainImageUrl", source = "thumbnailUrl")
    ProductDetailResponse toDetailResponse(Product product);

    Product toEntity(CreateProductRequest request);



    ProductImageDto toImageDto(ProductImage image);

    ProductImage toImageEntity(ProductImageDto dto);
}