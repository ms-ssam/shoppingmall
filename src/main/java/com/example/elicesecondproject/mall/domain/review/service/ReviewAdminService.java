package com.example.elicesecondproject.mall.domain.review.service;

import com.example.elicesecondproject.mall.domain.review.dto.request.ReviewSearchCondition;
import com.example.elicesecondproject.mall.domain.review.dto.response.ReviewAdminResponse;
import com.example.elicesecondproject.mall.domain.review.entity.Review;
import com.example.elicesecondproject.mall.domain.review.mapper.ReviewMapper;
import com.example.elicesecondproject.mall.domain.review.repository.ReviewRepository;
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
    private final ReviewMapper reviewMapper;
    private final ReviewService reviewService;


    public Page<ReviewAdminResponse> searchReviews(ReviewSearchCondition condition, Pageable pageable){
        Page<Review> reviews = reviewRepository.searchReviews(condition, pageable);
        return reviews.map(reviewMapper::toReviewAdminResponse);
    }

    @Transactional
    public void deleteReviewAsAdmin(Long reviewId, Long adminId) {
        reviewService.softDeleteReview(reviewId, adminId);
    }
}
