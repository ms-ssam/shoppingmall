package com.example.elicesecondproject.mall.domain.cart.service;

import com.example.elicesecondproject.mall.domain.cart.dto.response.CartInfoResponseDto;
import com.example.elicesecondproject.mall.domain.cart.entity.Cart;
import com.example.elicesecondproject.mall.domain.cart.repository.CartRepository;
import com.example.elicesecondproject.mall.global.common.PermissionValidator;
import com.example.elicesecondproject.mall.global.error.ErrorCode;
import com.example.elicesecondproject.mall.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class CartService {
    private final CartRepository cartRepository;
    private final PermissionValidator permissionValidator;

    public CartInfoResponseDto getCartInfo(Long memberId) {
        Cart cart = cartRepository.findByMemberId(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CART_NOT_FOUND));
        // fetch join해서 CartItemList 들고 있는 상태
        //  -> 각 cartItem을 CartInfoResDto로 stream map (여기서
        //   ->
    }
}



/*
    실제 소유주 검증 방법 - PermissionValidator 사용법
    public ... ...() {
        permissionValidator.validate(wantsToValidateObject, actualOwner);
    }
    */
