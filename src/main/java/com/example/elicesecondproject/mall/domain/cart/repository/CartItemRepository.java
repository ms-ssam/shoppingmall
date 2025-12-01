package com.example.elicesecondproject.mall.domain.cart.repository;

import com.example.elicesecondproject.mall.domain.cart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem,Long> {
}
