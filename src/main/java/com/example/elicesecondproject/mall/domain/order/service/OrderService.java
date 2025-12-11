package com.example.elicesecondproject.mall.domain.order.service;

import com.example.elicesecondproject.mall.domain.cart.entity.CartItem;
import com.example.elicesecondproject.mall.domain.cart.repository.CartItemRepository;
import com.example.elicesecondproject.mall.domain.cart.service.CartService;
import com.example.elicesecondproject.mall.domain.member.entity.Member;
import com.example.elicesecondproject.mall.domain.member.entity.MemberStatus;
import com.example.elicesecondproject.mall.domain.member.repositorty.MemberRepository;
import com.example.elicesecondproject.mall.domain.option.entity.OptionDetail;
import com.example.elicesecondproject.mall.domain.order.dto.request.OrderCreateRequest;
import com.example.elicesecondproject.mall.domain.order.dto.request.OrderSheetFromCartRequest;
import com.example.elicesecondproject.mall.domain.order.dto.request.UserOrderSearchCondition;
import com.example.elicesecondproject.mall.domain.order.dto.response.OrderSheetItemResponse;
import com.example.elicesecondproject.mall.domain.order.dto.response.OrderSheetResponse;
import com.example.elicesecondproject.mall.domain.order.dto.response.UserOrderDetailResponse;
import com.example.elicesecondproject.mall.domain.order.dto.response.UserOrderInfoResponse;
import com.example.elicesecondproject.mall.domain.order.entity.DeliveryInfo;
import com.example.elicesecondproject.mall.domain.order.entity.Order;
import com.example.elicesecondproject.mall.domain.order.entity.OrderItem;
import com.example.elicesecondproject.mall.domain.order.entity.OrderStatus;
import com.example.elicesecondproject.mall.domain.order.mapper.OrderMapper;
import com.example.elicesecondproject.mall.domain.order.repository.OrderRepository;
import com.example.elicesecondproject.mall.domain.product.entity.Product;
import com.example.elicesecondproject.mall.global.common.PermissionValidator;
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
    private final CartService cartService;
    private final MemberRepository memberRepository;
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final PermissionValidator permissionValidator;

    // 장바구니 -> 주문서
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

    // 주문서 -> 주문 생성
    @Transactional
    public Long createOrder(Long memberId, OrderCreateRequest request) {

        Member member = getMemberOrThrow(memberId);

        List<Long> cartItemIds = request.getCartItemIds();
        List<CartItem> cartItems = getValidCartItems(memberId, cartItemIds);

        // cartItem → OrderItem 스냅샷 변환
        List<OrderItem> orderItems = cartItems.stream()
                .map(OrderItem::fromCartItem)
                .toList();

        // 배송정보 생성
        DeliveryInfo deliveryInfo = request.toDeliveryInfo();

        // Order 한 번에 생성 (총액/배송비/대표상품명은 Order 내부에서 계산)
        Order order = Order.create(member, deliveryInfo, orderItems);

        orderRepository.save(order);

        return order.getId();
    }

    public Page<UserOrderInfoResponse> getMyOrders(UserOrderSearchCondition condition,
                                                   Long memberId,
                                                   Pageable pageable
    ){

        Page<Order> orders = orderRepository.searchMyOrders(condition, memberId, pageable);
        return orders.map(orderMapper::toUserOrderInfoResponse);
    }

    public UserOrderInfoResponse getOrderForMember(Long orderId, Long memberId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.getOwnerId().equals(memberId)) {
            throw new BusinessException(ErrorCode.ORDER_ACCESS_DENIED);
        }

        return orderMapper.toUserOrderInfoResponse(order);
    }

    public UserOrderDetailResponse getMyOrderDetail(Long orderId, Long memberId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
        Member member = getMemberOrThrow(memberId);
        permissionValidator.validate(order, member);
        return orderMapper.toUserOrderDetailResponse(order);
    }

    @Transactional
    public void requestCancel(Long orderId, Member member) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        existsActiveMember(member.getId());

        permissionValidator.validate(order, member);

        if(!order.getOrderStatus().canChangeTo(OrderStatus.CANCEL_REQUESTED)) {
            throw new BusinessException(ErrorCode.INVALID_ORDER_STATUS_CHANGE);
        }

        order.updateOrderStatus(OrderStatus.CANCEL_REQUESTED);
    }

    public List<UserOrderInfoResponse> getRecentOrdersForMyPage(Long memberId) {

        existsActiveMember(memberId);

        return orderRepository.findTop5ByMemberIdOrderByIdDesc(memberId)
                .stream()
                .map(orderMapper::toUserOrderInfoResponse)
                .toList();
    }

    // ============ 공통 메서드(조회, 유효성) ===============

    private void existsActiveMember(Long memberId) {
        if(!memberRepository.existsByIdAndStatus(memberId, MemberStatus.ACTIVE)) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
    }

    private Member getMemberOrThrow(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private List<CartItem> getValidCartItems(Long memberId, List<Long> cartItemIds) {

        if(cartItemIds == null || cartItemIds.isEmpty()) {
            throw new BusinessException(ErrorCode.ORDER_CART_ITEMS_EMPTY);
        }

        // fetch join 조회
        List<CartItem> items = cartItemRepository
                .findAllWithDetailsByIdInAndCartMemberId(cartItemIds, memberId);

        if(items.isEmpty()){
            throw new BusinessException(ErrorCode.ORDER_CART_ITEMS_EMPTY);
        }
        if(cartItemIds.size() != items.size()){
            throw new BusinessException(ErrorCode.ORDER_CART_ITEMS_INVALID);
        }

        items.forEach(this::validateCartItem);

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
