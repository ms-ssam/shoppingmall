package com.example.elicesecondproject.mall.domain.product.repository;

import com.example.elicesecondproject.mall.domain.product.entity.WishList;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;


public interface WishListRepository extends JpaRepository<WishList, Long> {

    Optional<WishList> findByMemberIdAndProductId(Long memberId, Long productId);

    // 특정 회원이 특정 상품에 찜 했는지 여부 조회
    boolean existsByMemberIdAndProductId(Long memberId, Long productId);

    @Query(
            value = "select w from WishList w join fetch w.product where w.member.id = :memberId",
            countQuery = "select count(w) from WishList w where w.member.id = :memberId"
    )
    Page<WishList> findAllByMemberId(@Param("memberId") Long memberId, Pageable pageable);
}
