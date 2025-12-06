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
    @Mapping(target = "mainImageUrl", expression = "java(product.getMainImageUrl())") // DTO에 필드가 생겼으므로 에러 해결됨
    @Mapping(target = "images", source = "images")
    @Mapping(target = "optionGroups", source = "optionGroups")
        // category 필드는 Product 엔티티의 category를 CategoryResponse로 변환해야 함.
        // 만약 CategoryMapper도 uses에 추가되어 있다면 자동 변환되지만,
        // 없다면 @Mapping(target = "category", ignore = true) 로 임시 처리하거나 매핑 로직 추가 필요
    ProductDetailResponse toDetailResponse(Product product);

    Product toEntity(CreateProductRequest request);

    ProductImageDto toImageDto(ProductImage image);
    ProductImage toImageEntity(ProductImageDto dto);
}