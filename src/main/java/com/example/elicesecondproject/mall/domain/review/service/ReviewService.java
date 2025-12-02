package com.example.elicesecondproject.mall.domain.review.service;

import com.example.elicesecondproject.mall.domain.member.entity.Member;
import com.example.elicesecondproject.mall.domain.member.entity.MemberStatus;
import com.example.elicesecondproject.mall.domain.member.entity.Role;
import com.example.elicesecondproject.mall.domain.member.repositorty.MemberRepository;
import com.example.elicesecondproject.mall.domain.product.entity.Product;
import com.example.elicesecondproject.mall.domain.product.repository.ProductRepository;
import com.example.elicesecondproject.mall.domain.review.dto.request.CreateReviewRequest;
import com.example.elicesecondproject.mall.domain.review.dto.request.UpdateReviewRequest;
import com.example.elicesecondproject.mall.domain.review.dto.response.ReviewResponse;
import com.example.elicesecondproject.mall.domain.review.entity.Review;
import com.example.elicesecondproject.mall.domain.review.mapper.ReviewMapper;
import com.example.elicesecondproject.mall.domain.review.repository.ReviewRepository;
import com.example.elicesecondproject.mall.global.exception.BusinessException;
import com.example.elicesecondproject.mall.global.exception.ErrorCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;
    private  final ReviewMapper reviewMapper;

    public Page<ReviewResponse> getReviewsByProduct(Long productId, Pageable pageable){
        productRepository.findByIdAndDeletedAtIsNull(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        Page<Review> reviews = reviewRepository.findByProductIdAndDeletedAtIsNull(productId, pageable);
        return reviews.map(reviewMapper::toResponse);
    }

    @Transactional
    public ReviewResponse createReview(Long productId, CreateReviewRequest request, Long memberId) {
        Product product = productRepository.findByIdAndDeletedAtIsNull(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        Member member = memberRepository.findByIdAndStatus(memberId, MemberStatus.ACTIVE)
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

        return reviewMapper.toResponse(review);
    }

    @Transactional
    public ReviewResponse updateReview(Long reviewId, UpdateReviewRequest request, Long memberId){
        Review review = reviewRepository.findByIdAndDeletedAtIsNull(reviewId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_NOT_FOUND));

        Member member = memberRepository.findByIdAndStatus(memberId, MemberStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!review.getMember().getId().equals(memberId) && member.getRole() != Role.ADMIN) {
            throw new BusinessException(ErrorCode.REVIEW_ACCESS_DENIED);
        }

        Product product = review.getProduct();

        if(!review.getRating().equals(request.getRating())){
            int oldCount = product.getReviewCount();
            int newCount = oldCount + 1;
            double oldAvg = product.getAverageRating();
            double newAvg = ((oldAvg * oldCount) + request.getRating()) / newCount;
            product.updateRating(newAvg, newCount);
        }

        review.update(request.getRating(), request.getContent(), request.getImageUrl());

        return reviewMapper.toResponse(review);
    }

    @Transactional
    public void softDeleteReview(Long reviewId, Long memberId) {
        Review review = reviewRepository.findByIdAndDeletedAtIsNull(reviewId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_NOT_FOUND));

        Member member = memberRepository.findByIdAndStatus(memberId, MemberStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!review.getMember().getId().equals(memberId) && member.getRole() != Role.ADMIN) {
            throw new BusinessException(ErrorCode.REVIEW_ACCESS_DENIED);
        }

        review.softDelete();
    }

    public Page<ReviewResponse> getReviewsByMember(Long memberId, Pageable pageable){
        // TODO: 회원 상태가 ACTIVE인 회원만 로그인 가능하도록 정책이 확정되면
        //  로그인 시점에서 이미 필터링되므로 이 검증 로직은 제거해도 됨.
        memberRepository.findByIdAndStatus(memberId, MemberStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Page<Review> reviews = reviewRepository.findByMemberIdAndDeletedAtIsNull(memberId, pageable);
        return reviews.map(reviewMapper::toResponse);
    }
}
