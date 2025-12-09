package com.example.elicesecondproject.mall.domain.order.service;

import com.example.elicesecondproject.mall.domain.cart.entity.CartItem;
import com.example.elicesecondproject.mall.domain.cart.repository.CartItemRepository;
import com.example.elicesecondproject.mall.domain.member.entity.Member;
import com.example.elicesecondproject.mall.domain.member.repositorty.MemberRepository;
import com.example.elicesecondproject.mall.domain.option.entity.OptionDetail;
import com.example.elicesecondproject.mall.domain.order.dto.request.OrderCreateRequest;
import com.example.elicesecondproject.mall.domain.order.dto.request.OrderSheetFromCartRequest;
import com.example.elicesecondproject.mall.domain.order.dto.request.UserOrderSearchCondition;
import com.example.elicesecondproject.mall.domain.order.dto.response.OrderSheetItemResponse;
import com.example.elicesecondproject.mall.domain.order.dto.response.OrderSheetResponse;
import com.example.elicesecondproject.mall.domain.order.dto.response.UserOrderInfoResponse;
import com.example.elicesecondproject.mall.domain.order.entity.DeliveryInfo;
import com.example.elicesecondproject.mall.domain.order.entity.Order;
import com.example.elicesecondproject.mall.domain.order.entity.OrderItem;
import com.example.elicesecondproject.mall.domain.order.mapper.OrderMapper;
import com.example.elicesecondproject.mall.domain.order.repository.OrderRepository;
import com.example.elicesecondproject.mall.domain.product.entity.Product;
import com.example.elicesecondproject.mall.global.error.ErrorCode;
import com.example.elicesecondproject.mall.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {
    private final CartItemRepository cartItemRepository;
    private final MemberRepository memberRepository;
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    /**
     * 장바구니 → 주문서 진입
     */
    public OrderSheetResponse createOrderSheet(Long memberId, OrderSheetFromCartRequest request) {

        Member member = getMemberOrThrow(memberId);

        List<CartItem> selectedCartItems = getValidCartItems(memberId, request.getCartItemIds());

        // 주문서아이템DTO로 변환 - 단가*수량 계산 db기준으로
        List<OrderSheetItemResponse> items = selectedCartItems.stream()
                .map(OrderSheetItemResponse::from)
                .toList();

        // 상품 총 가격 계산
        int totalPrice = items.stream()
                .mapToInt(OrderSheetItemResponse::getSubtotalPrice)
                .sum();

        // 배송비 (규칙은 Order 쪽 메서드가 가진다고 가정)
        int deliveryFee = Order.calculateShippingFee(totalPrice);

        // 화면용 합계 DTO 반환
        return new OrderSheetResponse(items, deliveryFee);
    }

    /**
     * 주문서 → 주문 생성
     * - 배송정보 + cartItemIds 기반으로 Order/OrderItem 한 번에 생성
     */
    @Transactional
    public Long createOrder(Long memberId, OrderCreateRequest request) {

        Member member = getMemberOrThrow(memberId);

        List<CartItem> cartItems = getValidCartItems(memberId, request.getCartItemIds());

        // cartItem → OrderItem 스냅샷 변환
        List<OrderItem> orderItems = cartItems.stream()
                .map(OrderItem::fromCartItem)
                .toList();

        // 배송정보 생성
        DeliveryInfo deliveryInfo = request.toDeliveryInfo();

        // Order 한 번에 생성 (총액/배송비/대표상품명은 Order 내부에서 계산)
        Order order = Order.create(member, deliveryInfo, orderItems);

        // TODO : 결제 구현 후 수정.
        order.markAsPaid();

        orderRepository.save(order);

        // FIXME 주문에 사용된 장바구니 항목 삭제
        cartItemRepository.deleteAll(cartItems);
        // FIXME: 이렇게 삭제하면 장바구니의 cartItems에도 수정사항이 반영되나? 안 되지 않나? ++totalCount 반영 X

        return order.getId();
    }

    public Page<UserOrderInfoResponse> getMyOrders(UserOrderSearchCondition condition,
                                                   Long memberId,
                                                   Pageable pageable
    ){

        Page<Order> orders = orderRepository.searchMyOrders(condition, memberId, pageable);
        return orders.map(orderMapper::toUserOrderInfoResponse);
    }

    @Transactional(readOnly = true)
    public Order getOrderForMember(Long orderId, Long memberId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.getOwnerId().equals(memberId)) {
            throw new BusinessException(ErrorCode.ORDER_ACCESS_DENIED);
        }

        return order;
    }

    // ============ 공통 메서드(조회, 유효성) ===============

    private Member getMemberOrThrow(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private List<CartItem> getValidCartItems(Long memberId, List<Long> cartItemIds) {
        List<CartItem> items = cartItemRepository
                .findAllByIdInAndCartMemberId(cartItemIds, memberId);

        if(items.isEmpty()){
            throw new BusinessException(ErrorCode.ORDER_CART_ITEMS_EMPTY);
        }
        if(cartItemIds.size() != items.size()){
            throw new BusinessException(ErrorCode.ORDER_CART_ITEMS_INVALID);
        }

        for(CartItem cartItem : items){
            validateCartItem(cartItem);
        }

        return items;
    }

    private void validateCartItem(CartItem cartItem) {

        OptionDetail optionDetail = cartItem.getProductOptionDetail();
        if(optionDetail == null || optionDetail.isSoldOut()) {
            throw new BusinessException(ErrorCode.ORDER_OPTION_INVALID);
        }

        Product product = optionDetail.getProduct();
        if(product == null) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        if(!product.isOnSale()) {
            throw new BusinessException(ErrorCode.PRODUCT_STOPPED);
        }

        // 재고체크
        if(optionDetail.getStockQuantity() < cartItem.getQuantity()) {
            throw new BusinessException(ErrorCode.NOT_ENOUGH_STOCK);
        }
    }

}
