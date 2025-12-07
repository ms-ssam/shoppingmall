package com.example.elicesecondproject.mall.domain.cart.repository;

import com.example.elicesecondproject.mall.domain.cart.entity.CartItem;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    @EntityGraph(attributePaths = {                                     // LazyInitializationException 방지
            "productOptionDetail",                                      // CartItem.productOptionDetail
            "productOptionDetail.productOptionGroup",                   // OptionDetail.productOptionGroup
            "productOptionDetail.productOptionGroup.product",           // ProductOptionGroup.product
            "productOptionDetail.productOptionGroup.product.optionGroups", // Product.optionGroups
            "productOptionDetail.productOptionGroup.details"            // ProductOptionGroup.details (현재 색상의 사이즈들)
    })
    Optional<CartItem> findWithProductAndOptionsById(Long id);
}
