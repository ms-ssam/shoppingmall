package com.example.elicesecondproject.mall.domain.cart.repository;

import com.example.elicesecondproject.mall.domain.cart.entity.CartItem;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Query("""
        select ci
        from CartItem ci
        join fetch ci.productOptionDetail od
        join fetch od.productOptionGroup pog
        join fetch pog.product
        where ci.id in :cartItemIds
          and ci.cart.member.id = :memberId
    """)
    List<CartItem> findAllWithDetailsByIdInAndCartMemberId(
            @Param("cartItemIds") List<Long> cartItemIds,
            @Param("memberId") Long memberId
    );

    // memberId에 해당하는 사용자의 장바구니에 담겨있는 cartItem들 중 대응되는 optionDetail이 param의 optionDetailIds에 포함되는 것들 조회
    List<CartItem> findAllByCartMemberIdAndProductOptionDetailIdIn(Long memberId, List<Long> optionDetailIds);
}
