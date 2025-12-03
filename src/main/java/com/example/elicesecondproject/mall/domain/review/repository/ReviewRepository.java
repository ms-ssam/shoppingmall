package com.example.elicesecondproject.mall.domain.review.repository;

import com.example.elicesecondproject.mall.domain.review.entity.Review;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long>, ReviewRepositoryCustom {


    Optional<Review> findByIdAndDeletedAtIsNull(Long id);

    Page<Review> findByProductIdAndDeletedAtIsNull(Long productId, Pageable pageable);

    Page<Review> findByMemberIdAndDeletedAtIsNull(Long memberId, Pageable pageable);

    Page<Review> findAllByDeletedAtIsNull(Pageable pageable);

    Long countByProductIdAndDeletedAtIsNull(Long productId);
}
