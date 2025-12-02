package com.example.elicesecondproject.mall.domain.review.service;

import com.example.elicesecondproject.mall.domain.member.entity.Member;
import com.example.elicesecondproject.mall.domain.member.repositorty.MemberRepository;
import com.example.elicesecondproject.mall.domain.product.entity.Product;
import com.example.elicesecondproject.mall.domain.product.repository.ProductRepository;
import com.example.elicesecondproject.mall.domain.review.dto.request.CreateReviewRequest;
import com.example.elicesecondproject.mall.domain.review.dto.response.ReviewResponse;
import com.example.elicesecondproject.mall.domain.review.entity.Review;
import com.example.elicesecondproject.mall.domain.review.mapper.ReviewMapper;
import com.example.elicesecondproject.mall.domain.review.repository.ReviewRepository;
import com.example.elicesecondproject.mall.global.exception.BusinessException;
import com.example.elicesecondproject.mall.global.exception.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public ReviewResponse createReview(Long productId, CreateReviewRequest request, Long MemberId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        Member member = memberRepository.findById(MemberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Review review = Review.builder()
                .product(product)
                .member(member)
                .rating(request.getRating())
                .content(request.getContent())
                .imageUrl(request.getImageUrl())
                .build();

        reviewRepository.save(review);

        // product 리뷰 수 증가, 평균 평점 갱신
        int oldCount = product.getReviewCount();
        int newCount = oldCount + 1;
        double oldAvg = product.getAverageRating();
        double newAvg = ((oldAvg * oldCount) + request.getRating()) / newCount; //TODO: 계산 하는 방법을 바꿔야 할듯 // 계산 한 값을 그대로 데이터베이스에 넣고  보여줄 때 소수점 제거

        product.updateRating(newAvg, newCount);

        return ReviewMapper.toResponse(review);
    }
}
