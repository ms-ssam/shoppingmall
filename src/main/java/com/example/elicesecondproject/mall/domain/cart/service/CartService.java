package com.example.elicesecondproject.mall.domain.cart.service;

import com.example.elicesecondproject.mall.domain.cart.repository.CartRepository;
import com.example.elicesecondproject.mall.global.common.PermissionValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class CartService {
    private final CartRepository cartRepository;
    private final PermissionValidator permissionValidator;

//    public CartInfoResponseDto getCartInfo(Long memberId) {
//        Cart cart = cartRepository.findWithItemsByMemberId(memberId)
//                .orElseThrow(() -> new BusinessException(ErrorCode.CART_NOT_FOUND));
//
//
//    }
}



/*
    실제 소유주 검증 방법 - PermissionValidator 사용법
    public ... ...() {
        permissionValidator.validate(wantsToValidateObject, actualOwner);
    }
    */
