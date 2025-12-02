package com.example.elicesecondproject.mall.domain.product.mapper;

import com.example.elicesecondproject.mall.domain.category.mapper.CategoryMapper;
import com.example.elicesecondproject.mall.domain.option.mapper.OptionMapper;
import com.example.elicesecondproject.mall.domain.product.dto.*;
import com.example.elicesecondproject.mall.domain.product.entity.Product;
import com.example.elicesecondproject.mall.domain.product.entity.ProductImage;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductMapper {

    // ===== Product -> DTO 변환 =====

    /**
     * Product Entity -> ProductSummaryDto (목록용)
     */
    @Mapping(target = "salePrice", expression = "java(product.getSalePrice())")
    @Mapping(target = "mainImageUrl", expression = "java(product.getMainImageUrl())")
    ProductSummaryDto toSummaryDto(Product product);



    @Mapping(target = "salePrice", expression = "java(product.getSalePrice())")
    ProductDetailResponse toDetailResponse(Product product);

    Product toEntity(CreateProductRequest request);



    ProductImageDto toImageDto(ProductImage image);

    ProductImage toImageEntity(ProductImageDto dto);



}
