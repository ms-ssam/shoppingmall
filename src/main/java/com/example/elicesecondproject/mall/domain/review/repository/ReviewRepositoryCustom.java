package com.example.elicesecondproject.mall.domain.review.repository;

import com.example.elicesecondproject.mall.domain.review.dto.request.ReviewSearchCondition;
import com.example.elicesecondproject.mall.domain.review.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface ReviewRepositoryCustom {
    Double calculateAverageRating(Long productId);

    Page<Review> searchReviews(ReviewSearchCondition condition, Pageable pageable);

    Page<Review> findMyReviewsByPeriod(Long memberId, LocalDate startDate, LocalDate endDate, Pageable pageable);
}
