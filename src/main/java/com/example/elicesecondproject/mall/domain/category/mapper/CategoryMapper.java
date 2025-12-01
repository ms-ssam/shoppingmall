package com.example.elicesecondproject.mall.domain.category.mapper;

import com.example.elicesecondproject.mall.domain.category.dto.*;
import com.example.elicesecondproject.mall.domain.category.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CategoryMapper {

    CategoryResponse toResponse(Category category);

    CategoryTreeResponse toTreeResponse(Category category);

    Category toEntity(CreateCategoryRequest request);
}
