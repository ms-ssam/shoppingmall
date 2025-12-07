package com.example.elicesecondproject.mall.domain.cart.repository;

import com.example.elicesecondproject.mall.domain.cart.entity.Cart;
import com.example.elicesecondproject.mall.domain.cart.entity.QCart;
import com.example.elicesecondproject.mall.domain.cart.entity.QCartItem;
import com.example.elicesecondproject.mall.domain.option.entity.QOptionDetail;
import com.example.elicesecondproject.mall.domain.option.entity.QProductOptionGroup;
import com.example.elicesecondproject.mall.domain.product.entity.QProduct;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class CartRepositoryCustomImpl implements CartRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<Cart> findWithItemsByMemberId(Long memberId) {  // TODO: 성능 최적화 필요한지 확인하기
        QCart cart = QCart.cart;
        QCartItem cartItem = QCartItem.cartItem;
        QOptionDetail optionDetail = QOptionDetail.optionDetail;
        QProductOptionGroup optionGroup = QProductOptionGroup.productOptionGroup;
        QProduct product = QProduct.product;

        Cart result = queryFactory
                .selectFrom(cart)
                .distinct()
                .leftJoin(cart.cartItems, cartItem).fetchJoin()
                .leftJoin(cartItem.productOptionDetail, optionDetail).fetchJoin()
                .leftJoin(optionDetail.productOptionGroup, optionGroup).fetchJoin()
                .leftJoin(optionGroup.product, product).fetchJoin()
                .where(
                        cart.member.id.eq(memberId)  // 조건 추가 가능
                )
                .fetchOne();

        return Optional.ofNullable(result);
    }
}
