package com.example.elicesecondproject.mall.domain.cart.service;

import com.example.elicesecondproject.mall.domain.cart.dto.request.AddCartItemRequest;
import com.example.elicesecondproject.mall.domain.cart.dto.response.CartInfoResponseDto;
import com.example.elicesecondproject.mall.domain.cart.entity.Cart;
import com.example.elicesecondproject.mall.domain.cart.entity.CartItem;
import com.example.elicesecondproject.mall.domain.cart.repository.CartItemRepository;
import com.example.elicesecondproject.mall.domain.cart.repository.CartRepository;
import com.example.elicesecondproject.mall.domain.option.entity.OptionDetail;
import com.example.elicesecondproject.mall.domain.option.repository.OptionDetailRepository;
import com.example.elicesecondproject.mall.global.common.PermissionValidator;
import com.example.elicesecondproject.mall.global.error.ErrorCode;
import com.example.elicesecondproject.mall.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class CartService {
    private final CartRepository cartRepository;
    private final OptionDetailRepository optionalDetailRepository;
    private final CartItemRepository cartItemRepository;
    private final PermissionValidator permissionValidator;

    public CartInfoResponseDto getCartInfo(Long memberId) {
        Cart cart = cartRepository.findWithItemsAndProductInfoByMemberId(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CART_NOT_FOUND));

        permissionValidator.validate(cart, cart.getMember());

        return CartInfoResponseDto.of(cart);
    }

    // 카트에 아이템 추가(카트아이템 생성)
    @Transactional
    public void addItemToCart(Long memberId, AddCartItemRequest request) {

        Cart cart = cartRepository.findByMemberId(memberId);

        if (request.getOptionDetailIds().size() != request.getQuantities().size()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        Long optionDetailId;
        int quantity;
        for (int i = 0; i < request.getOptionDetailIds().size(); i++) {

            optionDetailId = request.getOptionDetailIds().get(i);
            quantity = request.getQuantities().get(i);

            OptionDetail optionDetail = optionalDetailRepository
                    .findById(optionDetailId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.OPTION_SIZE_NOT_FOUND));

            if (optionDetail.isSoldOut()) {
                throw new BusinessException(ErrorCode.NOT_ENOUGH_STOCK);
            }

            CartItem cartItem = cartItemRepository
                    .findByCartIdAndProductOptionDetailId(cart.getId(), optionDetailId)
                    .orElse(null);

            if (cartItem != null) {
                cartItem.increaseQuantity(quantity);
            } else {
                CartItem newItem = CartItem.of(optionDetail, quantity);
                cart.addItem(newItem);
            }
        }
    }

    // 개별 장바구니 항목 삭제
    @Transactional
    public void deleteCartItem(Long memberId, Long cartItemId) {
        Cart cart = cartRepository.findWithItemsByMemberId(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CART_NOT_FOUND));

        permissionValidator.validate(cart, cart.getMember());

        CartItem target = cart.getCartItems().stream()
                .filter(item -> item.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.CART_ITEM_NOT_FOUND));

        cart.removeItem(target);  // 연관관계 편의 메서드 -> flush 시점에 orphanRemoval에 의해 DB delete
    }

    // 선택 상품 삭제
    @Transactional
    public void deleteSelectedCartItems(Long memberId, List<Long> cartItemIds) {
        Cart cart = cartRepository.findWithItemsByMemberId(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CART_NOT_FOUND));

        permissionValidator.validate(cart, cart.getMember());

        List<CartItem> targets = cart.getCartItems().stream()
                .filter(item -> cartItemIds.contains(item.getId()))
                .toList();

        if(targets.isEmpty()) {
            throw new BusinessException(ErrorCode.CART_ITEM_NOT_FOUND);
        }

        cart.removeItems(targets);
    }
}