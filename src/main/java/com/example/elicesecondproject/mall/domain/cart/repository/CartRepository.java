package com.example.elicesecondproject.mall.domain.cart.repository;

import com.example.elicesecondproject.mall.domain.cart.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart,Long>, CartRepositoryCustom {
    //Optional<Cart> findByMemberId(Long memberId);

    Cart findByMemberId(Long memberId);
}
