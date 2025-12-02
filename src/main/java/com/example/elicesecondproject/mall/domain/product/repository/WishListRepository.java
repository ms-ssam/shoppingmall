package com.example.elicesecondproject.mall.domain.product.repository;

import com.example.elicesecondproject.mall.domain.product.entity.WishList;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


public interface WishListRepository extends JpaRepository<WishList, Long> {

    // 회원 별 위시리스트 조회용
    Page<WishList> findAllByMemberId(Long memberId, Pageable pageable);

    // 특정 회원이 특정 상품에 찜 했는지 여부 조회
    boolean existsByMemberIdAndProductId(Long memberId, Long productId);
}
