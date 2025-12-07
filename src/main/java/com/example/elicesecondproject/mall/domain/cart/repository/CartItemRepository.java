package com.example.elicesecondproject.mall.domain.cart.repository;

import com.example.elicesecondproject.mall.domain.cart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem,Long> {
    // 카트 + 옵션 기준으로 장바구니 항목 1개 찾기 (CART-F-10에서 사용)
    Optional<CartItem> findByCartIdAndProductOptionDetailId(Long cartId, Long productOptionDetailId);
}
