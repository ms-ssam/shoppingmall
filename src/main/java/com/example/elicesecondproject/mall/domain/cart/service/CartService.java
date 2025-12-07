package com.example.elicesecondproject.mall.domain.cart.service;

import com.example.elicesecondproject.mall.domain.cart.dto.request.AddCartItemRequest;
import com.example.elicesecondproject.mall.domain.cart.entity.Cart;
import com.example.elicesecondproject.mall.domain.cart.entity.CartItem;
import com.example.elicesecondproject.mall.domain.cart.repository.CartItemRepository;
import com.example.elicesecondproject.mall.domain.cart.repository.CartRepository;
import com.example.elicesecondproject.mall.domain.member.entity.Member;
import com.example.elicesecondproject.mall.domain.member.repositorty.MemberRepository;
import com.example.elicesecondproject.mall.domain.option.entity.OptionDetail;
import com.example.elicesecondproject.mall.domain.option.repository.OptionDetailRepository;
import com.example.elicesecondproject.mall.global.common.PermissionValidator;
import com.example.elicesecondproject.mall.global.error.ErrorCode;
import com.example.elicesecondproject.mall.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class CartService {
    private final CartRepository cartRepository;
    private final OptionDetailRepository optionalDetailRepository;
    private final MemberRepository memberRepository;
    private final CartItemRepository cartItemRepository;
    private final PermissionValidator permissionValidator;

//    public CartInfoResponseDto getCartInfo(Long memberId) {
//        Cart cart = cartRepository.findWithItemsByMemberId(memberId)
//                .orElseThrow(() -> new BusinessException(ErrorCode.CART_NOT_FOUND));
//
//
//    }

    // 카트에 아이템 추가(카트아이템 생성)
    @Transactional
    public void addItemToCart(Long memberId, AddCartItemRequest request) {
        // 더미데이터로 테스트 시 카트 없어서 만들어줘야함. cart accesslevel도 수정해야 테스트 가능
        /*Cart cart = cartRepository.findByMemberId(memberId)
                .orElseGet(() -> {
                    Member member = memberRepository.findById(memberId)
                            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
                    Cart newCart = new Cart();
                    newCart.setMember(member);
                    return cartRepository.save(newCart);
                });*/
        // 회원의 카트 조회
        Cart cart = cartRepository.findByMemberId(memberId);

        // 옵션 조회(+품절여부)
        OptionDetail optionDetail = optionalDetailRepository
                .findById(request.getOptionDetailId())
                .orElseThrow(() -> new BusinessException(ErrorCode.OPTION_SIZE_NOT_FOUND));

        if(optionDetail.isSoldOut()){
            throw new BusinessException(ErrorCode.NOT_ENOUGH_STOCK);
        }

        // 이미 같은 옵션이 장바구니에 있는지 체크 -> 있으면 수량 증가
        CartItem cartItem = cartItemRepository
                .findByCartIdAndProductOptionDetailId(cart.getId(), request.getOptionDetailId())
                .orElse(null);
        if(cartItem != null){
            cartItem.increaseQuantity(request.getQuantity());
        } else {
            CartItem newItem = CartItem.of(optionDetail, request.getQuantity());
            cart.addItem(newItem);

        }
    }
}



/*
    실제 소유주 검증 방법 - PermissionValidator 사용법
    public ... ...() {
        permissionValidator.validate(wantsToValidateObject, actualOwner);
    }
    */
