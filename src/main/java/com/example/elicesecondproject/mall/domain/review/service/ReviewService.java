package com.example.elicesecondproject.mall.domain.review.service;

import com.example.elicesecondproject.mall.domain.member.entity.Member;
import com.example.elicesecondproject.mall.domain.member.entity.MemberStatus;
import com.example.elicesecondproject.mall.domain.member.entity.Role;
import com.example.elicesecondproject.mall.domain.member.repositorty.MemberRepository;
import com.example.elicesecondproject.mall.domain.product.entity.Product;
import com.example.elicesecondproject.mall.domain.product.repository.ProductRepository;
import com.example.elicesecondproject.mall.domain.review.dto.request.CreateReviewRequest;
import com.example.elicesecondproject.mall.domain.review.dto.request.UpdateReviewRequest;
import com.example.elicesecondproject.mall.domain.review.dto.response.MyReviewDetailResponse;
import com.example.elicesecondproject.mall.domain.review.dto.response.MyReviewResponse;
import com.example.elicesecondproject.mall.domain.review.dto.response.ReviewResponse;
import com.example.elicesecondproject.mall.domain.review.entity.Review;
import com.example.elicesecondproject.mall.domain.review.mapper.ReviewMapper;
import com.example.elicesecondproject.mall.domain.review.repository.ReviewRepository;
import com.example.elicesecondproject.mall.global.common.PermissionValidator;
import com.example.elicesecondproject.mall.global.error.exception.BusinessException;
import com.example.elicesecondproject.mall.global.error.ErrorCode;
import com.example.elicesecondproject.mall.global.service.GlobalImageFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;
    private final ReviewMapper reviewMapper;
    private final PermissionValidator permissionValidator;
    private final GlobalImageFileService globalImageFileService;

    public Page<ReviewResponse> getReviewsByProduct(Long productId, Pageable pageable){
        if (!productRepository.existsByIdAndDeletedAtIsNull(productId)) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        Page<Review> reviews = reviewRepository.findByProductIdAndDeletedAtIsNullOrderByIdDesc(productId, pageable);
        return reviews.map(reviewMapper::toReviewResponse);
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

        return reviewMapper.toReviewResponse(review);
    }

    //TODO: 이미지 등록 공부 및 최적화 (이미시 설정도 확인)
    @Transactional
    public void updateMyReview(Long reviewId,
                               Long memberId,
                               UpdateReviewRequest request,
                               MultipartFile imageFile,
                               boolean deleteImage) {

        // 1. 리뷰 조회 (소프트 삭제된 건 제외)
        Review review = reviewRepository.findByIdAndDeletedAtIsNull(reviewId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_NOT_FOUND));

        // 2. 회원 검증 + 권한 체크 (본인 or 관리자만 수정 가능)
        Member member = getActiveMember(memberId);
        permissionValidator.validate(review, member);

        Product product = review.getProduct();
        Long productId = product.getId();

        Integer oldRating = review.getRating();
        Integer newRating = request.getRating();

        // 3. 이미지 URL 결정 로직
        String oldImageUrl = review.getImageUrl();
        String newImageUrl = oldImageUrl;

        // 1) 삭제 체크박스가 눌린 경우 → 최종적으로 이미지 없음
        if (deleteImage) {
            newImageUrl = null;
        }

        // 2) 새 파일이 업로드된 경우 → 최종적으로 새 이미지로 교체
        if (imageFile != null && !imageFile.isEmpty()) {
            newImageUrl = globalImageFileService.saveReviewImage(productId, imageFile);
        }

        // 3) 최종 URL이 바뀌었으면, old 파일은 삭제
        if (oldImageUrl != null && !oldImageUrl.equals(newImageUrl)) {
            globalImageFileService.deleteImage(oldImageUrl);
        }

        // 4. 리뷰 내용/평점/이미지 갱신
        review.updateAll(
                request.getRating(),
                request.getContent(),
                newImageUrl
        );

        // 5. 평점이 바뀐 경우에만 상품 평균 평점 재계산
        if (!oldRating.equals(newRating)) {
            Double newAvg = reviewRepository.calculateAverageRating(product.getId());
            product.updateRating(newAvg);
        }
    }


    @Transactional
    public void softDeleteReview(Long reviewId, Long memberId) {
        Review review = reviewRepository.findByIdAndDeletedAtIsNull(reviewId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_NOT_FOUND));

        Member member = getActiveMember(memberId);

        permissionValidator.validate(review, member);

        review.softDelete();

        Product product = review.getProduct();

        updateProductRatingAndCount(product);
    }

    public Page<MyReviewResponse> getReviewsByMember(Long memberId, Pageable pageable){
        // TODO: 회원 상태가 ACTIVE인 회원만 로그인 가능하도록 정책이 확정되면
        //  로그인 시점에서 이미 필터링되므로 이 검증 로직은 제거해도 됨.
        memberRepository.findByIdAndStatus(memberId, MemberStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Page<Review> reviews = reviewRepository.findByMemberIdAndDeletedAtIsNullOrderByIdDesc(memberId, pageable);
        return reviews.map(reviewMapper::toMyReviewResponse);
    }

    public MyReviewDetailResponse getMyReviewDetail(Long reviewId, Long memberId) {
        Review review = reviewRepository.findByIdAndDeletedAtIsNull(reviewId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_NOT_FOUND));

        Member member = getActiveMember(memberId);

        permissionValidator.validate(review, member);

        return reviewMapper.toMyDetailReviewResponse(review);
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
