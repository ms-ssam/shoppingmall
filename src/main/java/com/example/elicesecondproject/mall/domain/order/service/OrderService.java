package com.example.elicesecondproject.mall.domain.order.service;

import com.example.elicesecondproject.mall.domain.cart.entity.CartItem;
import com.example.elicesecondproject.mall.domain.cart.repository.CartItemRepository;
import com.example.elicesecondproject.mall.domain.member.entity.Member;
import com.example.elicesecondproject.mall.domain.member.repositorty.MemberRepository;
import com.example.elicesecondproject.mall.domain.order.dto.request.OrderCreateRequest;
import com.example.elicesecondproject.mall.domain.order.dto.request.OrderSheetFromCartRequest;
import com.example.elicesecondproject.mall.domain.order.dto.response.OrderSheetItemResponse;
import com.example.elicesecondproject.mall.domain.order.dto.response.OrderSheetResponse;
import com.example.elicesecondproject.mall.domain.order.entity.DeliveryInfo;
import com.example.elicesecondproject.mall.domain.order.entity.Order;
import com.example.elicesecondproject.mall.domain.order.entity.OrderItem;
import com.example.elicesecondproject.mall.domain.order.repository.OrderRepository;
import com.example.elicesecondproject.mall.global.error.ErrorCode;
import com.example.elicesecondproject.mall.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final CartItemRepository cartItemRepository;
    private final MemberRepository memberRepository;
    private final OrderRepository orderRepository;

    /**
     * 장바구니 → 주문서 진입
     * - 선택된 cartItemIds에 대해
     * - 본인 장바구니 것만 조회
     * - 주문서에 뿌릴 DTO + 금액/배송비 계산
     */
    @Transactional(readOnly = true)
    public OrderSheetResponse createOrderSheet(Long memberId, OrderSheetFromCartRequest request) {

        // 1) 선택된 장바구니 항목 조회 (본인 것만)
        List<CartItem> selectedCartItems = cartItemRepository
                .findAllByIdInAndCartMemberId(request.getCartItemIds(), memberId);

        if (selectedCartItems.isEmpty()) {
            //throw new BusinessException(ErrorCode.CART_ITEM_NOT_FOUND);
        }

        // 2) 화면용 DTO로 변환
        List<OrderSheetItemResponse> items = selectedCartItems.stream()
                .map(OrderSheetItemResponse::from)
                .toList();

        // 3) 상품 총액
        int totalPrice = items.stream()
                .mapToInt(OrderSheetItemResponse::getSubtotalPrice)
                .sum();

        // 4) 배송비 (규칙은 Order 쪽 메서드가 가진다고 가정)
        int deliveryFee = Order.calculateShippingFee(totalPrice);

        // 5) 화면용 합계 DTO 반환
        return new OrderSheetResponse(items, deliveryFee);
    }

    /**
     * 주문서 → 주문 생성
     * - 배송정보 + cartItemIds 기반으로 Order/OrderItem 한 번에 생성
     */
    @Transactional
    public Long placeOrder(Long memberId, OrderCreateRequest request) {

        // 1) 회원 확인
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 2) cartItem 재조회 (보안상 memberId 함께 체크)
        List<CartItem> cartItems = cartItemRepository
                .findAllByIdInAndCartMemberId(request.getCartItemIds(), memberId);

        if (cartItems.isEmpty()) {
            throw new BusinessException(ErrorCode.CART_ITEM_NOT_FOUND);
        }

        // 3) CartItem → OrderItem 스냅샷 변환
        List<OrderItem> orderItems = cartItems.stream()
                .map(OrderItem::fromCartItem)
                .toList();

        // 4) 배송정보 생성
        DeliveryInfo deliveryInfo = DeliveryInfo.of(
                request.getReceiverName(),
                request.getReceiverPhone(),
                request.getReceiverAddress()
        );

        // 5) Order 한 번에 생성 (총액/배송비/대표상품명은 Order 내부에서 계산)
        Order order = Order.create(member, deliveryInfo, orderItems);

        orderRepository.save(order);

        // 6) 주문에 사용된 장바구니 항목 삭제
        cartItemRepository.deleteAll(cartItems);

        return order.getId();
    }
}
