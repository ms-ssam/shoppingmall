package com.example.elicesecondproject.mall.domain.review.mapper;

import com.example.elicesecondproject.mall.domain.review.dto.response.ReviewResponse;
import com.example.elicesecondproject.mall.domain.review.entity.Review;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface ReviewMapper {

    @Mapping(target = "memberNickname", source = "member.nickname")
    ReviewResponse toResponse(Review review);
}