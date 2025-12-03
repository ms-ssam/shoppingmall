package com.example.elicesecondproject.mall.domain.review.service;

import com.example.elicesecondproject.mall.domain.product.repository.ProductRepository;
import com.example.elicesecondproject.mall.domain.review.dto.response.ReviewAdminResponse;
import com.example.elicesecondproject.mall.domain.review.entity.Review;
import com.example.elicesecondproject.mall.domain.review.mapper.ReviewMapper;
import com.example.elicesecondproject.mall.domain.review.repository.ReviewRepository;
import com.example.elicesecondproject.mall.global.exception.BusinessException;
import com.example.elicesecondproject.mall.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewAdminService {
    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final ReviewMapper reviewMapper;
    private final ReviewService reviewService;


    public Page<ReviewAdminResponse> getAllReviews(Pageable pageable){
        Page<Review> reviews = reviewRepository.findAllByDeletedAtIsNull(pageable);
        return reviews.map(reviewMapper::toAdminResponse);
    }

    @Transactional
    public void deleteReviewAsAdmin(Long reviewId, Long adminId) {
        reviewService.softDeleteReview(reviewId, adminId);
    }
}
