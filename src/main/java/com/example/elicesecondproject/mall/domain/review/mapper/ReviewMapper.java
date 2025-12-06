package com.example.elicesecondproject.mall.domain.review.mapper;

import com.example.elicesecondproject.mall.domain.product.entity.Product;
import com.example.elicesecondproject.mall.domain.review.dto.response.MyReviewDetailResponse;
import com.example.elicesecondproject.mall.domain.review.dto.response.MyReviewResponse;
import com.example.elicesecondproject.mall.domain.review.dto.response.ReviewAdminResponse;
import com.example.elicesecondproject.mall.domain.review.dto.response.ReviewResponse;
import com.example.elicesecondproject.mall.domain.review.entity.Review;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface ReviewMapper {

    @Mapping(target = "memberNickname", source = "member.nickname")
    ReviewResponse toReviewResponse(Review review);

    @Mapping(target = "memberNickname", source = "member.nickname")
    @Mapping(target = "productName", source = "product.name")
    ReviewAdminResponse toReviewAdminResponse(Review review);

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "productImageUrl", ignore = true)
    MyReviewDetailResponse toMyDetailReviewResponse(Review review);

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "productImageUrl", ignore = true)
    MyReviewResponse toMyReviewResponse(Review review);

    default String mapProductImage(Product product) {
        if (product.getImages() == null || product.getImages().isEmpty()) {
            return "/images/default-product.jpg"; // 기본 이미지 넣기
        }
        return product.getImages().get(0).getImageUrl();
    }

    @AfterMapping
    default void setProductImage(Review review, @MappingTarget MyReviewResponse.MyReviewResponseBuilder response) {
        response.productImageUrl(mapProductImage(review.getProduct()));
    }
}