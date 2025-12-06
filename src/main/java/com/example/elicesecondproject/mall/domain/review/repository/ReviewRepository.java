package com.example.elicesecondproject.mall.domain.review.repository;

import com.example.elicesecondproject.mall.domain.review.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long>, ReviewRepositoryCustom {


    Optional<Review> findByIdAndDeletedAtIsNull(Long id);

    Page<Review> findByProductIdAndDeletedAtIsNullOrderByIdDesc(Long productId, Pageable pageable);

    Page<Review> findByMemberIdAndDeletedAtIsNullOrderByIdDesc(Long memberId, Pageable pageable);

    Long countByProductIdAndDeletedAtIsNull(Long productId);
}
