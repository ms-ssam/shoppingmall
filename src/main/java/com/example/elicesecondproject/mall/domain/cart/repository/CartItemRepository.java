package com.example.elicesecondproject.mall.domain.cart.repository;

import com.example.elicesecondproject.mall.domain.cart.entity.CartItem;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
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

    // 카트 + 옵션 기준으로 장바구니 항목 1개 찾기 (CART-F-10에서 사용)
    Optional<CartItem> findByCartIdAndProductOptionDetailId(Long cartId, Long productOptionDetailId);

    List<CartItem> findAllByIdInAndCartMemberId(@NotEmpty(message = "주문할 상품을 선택해주세요.") List<Long> cartItemIds, Long memberId);
}
