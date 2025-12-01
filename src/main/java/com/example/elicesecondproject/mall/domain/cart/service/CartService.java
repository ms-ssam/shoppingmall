package com.example.elicesecondproject.mall.domain.cart.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CartService {
//    private final CartRepository cartRepository;
//    private final ProductRepository productRepository;
//
//    // C (=회원가입)
//
//    // R
//
//    // U
//
//    // D
//
//    public void addItem(Long memberId, Long productId, int quantity) {
//        Cart cart = cartRepository.findByMemberId(memberId)
//                .orElseThrow(() -> new BusinessException(ErrorCode.CART_NOT_FOUND));
//
//        Product product = productRepository.findById(productId)
//                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
//
//        product.validate(quantity);  // 해당 상품이 장바구니에 담길 수 있는지 검증 (재고, 기타 등등...) -> ProductValidator 따로 만들고 거기에서 하는 게 낫나?
//
//        CartItem cartItem = CartItem.of(product, quantity);
//
//        cart.addItem(cartItem);
//    }
}
