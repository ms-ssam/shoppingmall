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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


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

        validateCartOwner(cart);

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

        OptionDetail newOptionDetail = getOptionDetailOrThrow(request.getOptionDetailId());

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

        Cart cart = getCartOrThrow(memberId);

        List<Long> optionIds = request.getOptionDetailIds();
        List<Integer> quantities = request.getQuantities();

        if (optionIds.size() != quantities.size()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        // 1) 요청 정규화: optionId별 수량 합산 (중복 optionId 처리)
        Map<Long, Integer> qtyByOptionId = new LinkedHashMap<>();
        for (int i = 0; i < optionIds.size(); i++) {
            qtyByOptionId.merge(optionIds.get(i), quantities.get(i), Integer::sum);
        }

        List<Long> normalizedOptionIds = new ArrayList<>(qtyByOptionId.keySet()); // 유니크 optionId

        // 2) 옵션 디테일 벌크 조회 + 존재 검증
        List<OptionDetail> optionDetails = optionalDetailRepository.findByIdIn(normalizedOptionIds);
        if (optionDetails.size() != normalizedOptionIds.size()) {
            throw new BusinessException(ErrorCode.OPTION_SIZE_NOT_FOUND);
        }

        Map<Long, OptionDetail> optionMap = optionDetails.stream()
                .collect(Collectors.toMap(OptionDetail::getId, Function.identity()));

        // 3) 카트 아이템 벌크 조회
        List<CartItem> cartItems = cartItemRepository
                .findByCartIdAndProductOptionDetailIdIn(cart.getId(), normalizedOptionIds);

        Map<Long, CartItem> cartItemMap = cartItems.stream()
                .collect(Collectors.toMap(
                        ci -> ci.getProductOptionDetail().getId(),
                        Function.identity()
                ));

        // 4) 유니크 optionId 기준으로 재고 체크 + 담기 (루프에서 쿼리 없음)
        for (Map.Entry<Long, Integer> entry : qtyByOptionId.entrySet()) {
            Long optionId = entry.getKey();
            int addQty = entry.getValue();

            OptionDetail optionDetail = optionMap.get(optionId);
            CartItem cartItem = cartItemMap.get(optionId);

            int totalQty = addQty + (cartItem == null ? 0 : cartItem.getQuantity());
            if (optionDetail.getStockQuantity() < totalQty) {
                throw new BusinessException(ErrorCode.NOT_ENOUGH_STOCK);
            }

            if (cartItem != null) {
                cartItem.increaseQuantity(addQty);
            } else {
                CartItem newItem = CartItem.of(optionDetail, addQty);
                cart.addItem(newItem);
            }
        }
    }


    // 개별 장바구니 항목 삭제
    @Transactional
    public void deleteCartItem(Long memberId, Long cartItemId) {
        Cart cart = getCartWithItemsAndValidateOrThrow(memberId);

        CartItem target = cart.getCartItems().stream()
                .filter(item -> item.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.CART_ITEM_NOT_FOUND));

        cart.removeItem(target);  // 연관관계 편의 메서드 -> flush 시점에 orphanRemoval에 의해 DB delete
    }

    // 선택 상품 삭제
    @Transactional
    public void deleteSelectedCartItems(Long memberId, List<Long> cartItemIds) {
        Cart cart = getCartWithItemsAndValidateOrThrow(memberId);

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
        Cart cart = getCartOrThrow(memberId);
        return cart.getCartItems().size();
    }

    // =========================
    // 공통 로직
    // =========================
    private Cart getCartOrThrow(Long memberId) {
        return cartRepository.findByMemberId(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CART_NOT_FOUND));
    }

    private Cart getCartWithItemsOrThrow(Long memberId) {
        return cartRepository.findWithItemsByMemberId(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CART_NOT_FOUND));
    }

    private OptionDetail getOptionDetailOrThrow(Long optionDetailId) {
        return optionalDetailRepository.findById(optionDetailId)
                .orElseThrow(() -> new BusinessException(ErrorCode.OPTION_SIZE_NOT_FOUND));
    }

    private Cart getCartWithItemsAndValidateOrThrow(Long memberId) {
        Cart cart = getCartWithItemsOrThrow(memberId);
        validateCartOwner(cart);
        return cart;
    }

    private void validateCartOwner(Cart cart) {
        permissionValidator.validate(cart, cart.getMember());
    }



}