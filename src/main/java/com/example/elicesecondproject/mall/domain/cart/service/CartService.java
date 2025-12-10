package com.example.elicesecondproject.mall.domain.cart.service;

import com.example.elicesecondproject.mall.domain.cart.dto.request.AddCartItemRequest;
import com.example.elicesecondproject.mall.domain.cart.dto.request.CartItemOptionModifyRequest;
import com.example.elicesecondproject.mall.domain.cart.dto.response.CartInfoResponseDto;
import com.example.elicesecondproject.mall.domain.cart.dto.response.CartItemEditPopupResponse;
import com.example.elicesecondproject.mall.domain.cart.entity.Cart;
import com.example.elicesecondproject.mall.domain.cart.entity.CartItem;
import com.example.elicesecondproject.mall.domain.cart.repository.CartItemRepository;
import com.example.elicesecondproject.mall.domain.cart.repository.CartRepository;
import com.example.elicesecondproject.mall.domain.option.dto.OptionDetailIdNameResponse;
import com.example.elicesecondproject.mall.domain.option.dto.ProductOptionGroupIdNameResponse;
import com.example.elicesecondproject.mall.domain.option.entity.OptionDetail;
import com.example.elicesecondproject.mall.domain.option.entity.ProductOptionGroup;
import com.example.elicesecondproject.mall.domain.option.mapper.OptionMapper;
import com.example.elicesecondproject.mall.domain.option.repository.OptionDetailRepository;
import com.example.elicesecondproject.mall.domain.option.repository.ProductOptionGroupRepository;
import com.example.elicesecondproject.mall.domain.product.entity.Product;
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
    private final ProductOptionGroupRepository productOptionGroupRepository;
    private final PermissionValidator permissionValidator;
    private final OptionMapper optionMapper;

    public CartInfoResponseDto getCartInfo(Long memberId) {
        Cart cart = cartRepository.findWithItemsAndProductInfoByMemberId(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CART_NOT_FOUND));

        permissionValidator.validate(cart, cart.getMember());

        return CartInfoResponseDto.of(cart);
    }

    public CartItemEditPopupResponse getCartItemEditPopup(Long cartItemId) {

        // 1. CartItem 조회
        CartItem cartItem = cartItemRepository.findWithProductAndOptionsById(cartItemId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CART_ITEM_NOT_FOUND));

        // 선택된 옵션/그룹
        OptionDetail selectedDetail = cartItem.getProductOptionDetail();
        ProductOptionGroup selectedGroup = selectedDetail.getProductOptionGroup();

        // 2. Product 조회
        Product product = selectedDetail.getProduct();

        // 3. 상품의 전체 옵션 그룹 목록 변환 (N번 반복)
        List<ProductOptionGroupIdNameResponse> optionGroupResponses =
                optionMapper.toOptionGroupIdNameList(product.getProductOptionGroups());

        // 4. 상품의 전체 옵션 상세 목록 변환 (N번 반복)
        List<OptionDetailIdNameResponse> optionDetailResponses =
                optionMapper.toOptionDetailIdNameList(selectedGroup.getDetails());

        // 5. 응답 조립
        return CartItemEditPopupResponse.builder()
                .selectedQuantity(cartItem.getQuantity())
                .selectedProductOptionGroupId(selectedGroup.getId())
                .selectedOptionDetailId(selectedDetail.getId())
                .optionGroups(optionGroupResponses)
                .optionDetails(optionDetailResponses)
                .build();
    }

    public List<OptionDetailIdNameResponse> getOptionDetailsByGroup(Long groupId) {
        ProductOptionGroup productOptionGroup = productOptionGroupRepository.findWithDetailsById(groupId)
                .orElseThrow(() -> new BusinessException(ErrorCode.OPTION_COLOR_NOT_FOUND));

        return optionMapper.toOptionDetailIdNameList(productOptionGroup.getDetails());
    }

    @Transactional
    public void updateCartItemOption(Long cartItemId, CartItemOptionModifyRequest request){
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CART_ITEM_NOT_FOUND));

        OptionDetail newOptionDetail = optionalDetailRepository.findById(request.getOptionDetailId())
                .orElseThrow(() -> new BusinessException(ErrorCode.OPTION_SIZE_NOT_FOUND));

        OptionDetail currentOptionDetail = cartItem.getProductOptionDetail();

        if (!newOptionDetail.getProductOptionGroup().getProduct().equals(
                currentOptionDetail.getProductOptionGroup().getProduct())) {
            throw new BusinessException(ErrorCode.CART_ITEM_PRODUCT_MISMATCH);
        }

        int updatedQuantity = request.getUpdatedQuantity();

        if(newOptionDetail.getStockQuantity() < updatedQuantity){
            throw new BusinessException(ErrorCode.NOT_ENOUGH_STOCK);
        }

        cartItem.updateOption(newOptionDetail, request.getUpdatedQuantity());
    }

    // 카트에 아이템 추가(카트아이템 생성)
    @Transactional
    public void addItemToCart(Long memberId, AddCartItemRequest request) {

        Cart cart = cartRepository.findByMemberId(memberId)
                .orElseThrow(()-> new BusinessException(ErrorCode.CART_NOT_FOUND));

        if (request.getOptionDetailIds().size() != request.getQuantities().size()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        for (int i = 0; i < request.getOptionDetailIds().size(); i++) {

            Long optionDetailId = request.getOptionDetailIds().get(i);
            int quantity = request.getQuantities().get(i);

            OptionDetail optionDetail = optionalDetailRepository
                    .findById(optionDetailId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.OPTION_SIZE_NOT_FOUND));

            // 이미 장바구니에 있는지 조회
            CartItem cartItem = cartItemRepository
                    .findByCartIdAndProductOptionDetailId(cart.getId(), optionDetailId)
                    .orElse(null);

            // 이번에 장바구니에 들어가게 될 총 수량 = 기존수량 + 새로 담는 수량
            int totalQuantity = quantity;
            if (cartItem != null) {
                totalQuantity += cartItem.getQuantity();
            }

            // 재고보다 많이 담으려고 하면 에러
            if (optionDetail.getStockQuantity() < totalQuantity) {
                throw new BusinessException(ErrorCode.NOT_ENOUGH_STOCK);
            }

            // 실제 담기
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

    // 헤더에서 사용할 장바구니 아이템 카운트
    public int getCartCount(Long memberId) {
        Cart cart = cartRepository.findByMemberId(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CART_NOT_FOUND));
        return cart.getCartItems().size();
    }
}