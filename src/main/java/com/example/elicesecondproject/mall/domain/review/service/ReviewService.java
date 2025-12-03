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
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;
    private final ReviewMapper reviewMapper;

    public Page<ReviewResponse> getReviewsByProduct(Long productId, Pageable pageable){
        if (!productRepository.existsByIdAndDeletedAtIsNull(productId)) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        Page<Review> reviews = reviewRepository.findByProductIdAndDeletedAtIsNull(productId, pageable);
        return reviews.map(reviewMapper::toResponse);
    }

    @Transactional
    public ReviewResponse createReview(Long productId, CreateReviewRequest request, Long memberId) {
        Product product = productRepository.findByIdAndDeletedAtIsNull(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        Member member = getActiveMember(memberId);

        Review review = Review.builder()
                .product(product)
                .member(member)
                .rating(request.getRating())
                .content(request.getContent())
                .imageUrl(request.getImageUrl())
                .build();

        reviewRepository.save(review);

        // product 리뷰 수 증가, 평균 평점 갱신
        updateProductRatingAndCount(product);

        return reviewMapper.toResponse(review);
    }

    @Transactional
    public ReviewResponse updateReview(Long reviewId, UpdateReviewRequest request, Long memberId){
        Review review = reviewRepository.findByIdAndDeletedAtIsNull(reviewId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_NOT_FOUND));

        Member member = getActiveMember(memberId);

        validateReviewAccess(review, memberId, member.getRole());

        Product product = review.getProduct();

        Integer oldRating = review.getRating();
        Integer newRating = request.getRating();

        review.update(request.getRating(), request.getContent(), request.getImageUrl());

        if (!oldRating.equals(newRating)) {
            Double newAvg = reviewRepository.calculateAverageRating(product.getId());
            product.updateRating(newAvg);
        }

        return reviewMapper.toResponse(review);
    }

    @Transactional
    public void softDeleteReview(Long reviewId, Long memberId) {
        Review review = reviewRepository.findByIdAndDeletedAtIsNull(reviewId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_NOT_FOUND));

        Member member = getActiveMember(memberId);

        validateReviewAccess(review, memberId, member.getRole());

        review.softDelete();

        Product product = review.getProduct();

        updateProductRatingAndCount(product);
    }

    public Page<ReviewResponse> getReviewsByMember(Long memberId, Pageable pageable){
        // TODO: 회원 상태가 ACTIVE인 회원만 로그인 가능하도록 정책이 확정되면
        //  로그인 시점에서 이미 필터링되므로 이 검증 로직은 제거해도 됨.
        memberRepository.findByIdAndStatus(memberId, MemberStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Page<Review> reviews = reviewRepository.findByMemberIdAndDeletedAtIsNull(memberId, pageable);
        return reviews.map(reviewMapper::toResponse);
    }

    private void validateReviewAccess(Review review, Long memberId, Role role) {
        boolean isOwner = review.getMember().getId().equals(memberId);
        boolean isAdmin = role == Role.ADMIN;

        if (!isOwner && !isAdmin) {
            throw new BusinessException(ErrorCode.REVIEW_ACCESS_DENIED);
        }
    }

    private void updateProductRatingAndCount(Product product) {
        Double newAvg = reviewRepository.calculateAverageRating(product.getId());
        Long newCount = reviewRepository.countByProductIdAndDeletedAtIsNull(product.getId());

        product.updateRatingAndReviewCount(newAvg, newCount.intValue());
    }

    private Member getActiveMember(Long memberId) {
        return memberRepository.findByIdAndStatus(memberId, MemberStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
}
