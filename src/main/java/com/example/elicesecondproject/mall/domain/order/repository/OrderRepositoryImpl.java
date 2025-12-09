package com.example.elicesecondproject.mall.domain.order.repository;

import com.example.elicesecondproject.mall.domain.order.dto.request.AdminOrderSearchCondition;
import com.example.elicesecondproject.mall.domain.order.dto.request.UserOrderSearchCondition;
import com.example.elicesecondproject.mall.domain.order.entity.Order;
import com.example.elicesecondproject.mall.domain.order.entity.OrderStatus;
import com.example.elicesecondproject.mall.domain.order.entity.QOrder;
import com.example.elicesecondproject.mall.domain.order.entity.QOrderItem;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    private final QOrder order = QOrder.order;
    private final QOrderItem orderItem = QOrderItem.orderItem;

    @Override
    public Page<Order> searchOrders(AdminOrderSearchCondition condition, Pageable pageable) {

        List<Order> contents = queryFactory
                .selectDistinct(order)
                .from(order)
                .leftJoin(order.orderItems, orderItem)
                .where(
                        eqStatus(condition.getOrderStatus()),
                        containsKeyword(condition.getKeyword()),
                        betweenOrderDate(condition.getStartDate(), condition.getEndDate())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(order.id.desc())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(order.countDistinct())
                .from(order)
                .leftJoin(order.orderItems, orderItem)
                .where(
                        eqStatus(condition.getOrderStatus()),
                        containsKeyword(condition.getKeyword()),
                        betweenOrderDate(condition.getStartDate(), condition.getEndDate())
                );

        return PageableExecutionUtils.getPage(contents, pageable, countQuery::fetchOne);
    }

    /**
     * 마이페이지 - 나의 주문 목록 조회
     */
    @Override
    public Page<Order> searchMyOrders(UserOrderSearchCondition condition,
                                      Long memberId,
                                      Pageable pageable) {

        List<Order> contents = queryFactory
                .selectDistinct(order)
                .from(order)
                .leftJoin(order.orderItems, orderItem)
                .where(
                        order.memberId.eq(memberId),                         // 내 주문만
                        containsKeywordForUser(condition.getKeyword()),     // 상품명 키워드
                        betweenOrderDate(condition.getStartDate(), condition.getEndDate())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(order.id.desc())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(order.countDistinct())
                .from(order)
                .leftJoin(order.orderItems, orderItem)
                .where(
                        order.memberId.eq(memberId),
                        containsKeywordForUser(condition.getKeyword()),
                        betweenOrderDate(condition.getStartDate(), condition.getEndDate())
                );

        return PageableExecutionUtils.getPage(contents, pageable, countQuery::fetchOne);
    }

    // ====== 공통 필터들 ======

    // 주문 상태 필터 (String -> Enum 매핑)
    private BooleanExpression eqStatus(String status) {
        if (!StringUtils.hasText(status)) {
            return null;
        }
        try {
            OrderStatus enumStatus = OrderStatus.valueOf(status);
            return order.orderStatus.eq(enumStatus);
        } catch (IllegalArgumentException e) {
            // 잘못된 status 값이 온 경우: 필터 적용 안 함
            return null;
        }
    }

    // [관리자용] 키워드 검색: 상품명 + 고객명
    private BooleanExpression containsKeyword(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return null;
        }

        return orderItem.productName.containsIgnoreCase(keyword)
                .or(order.ordererName.containsIgnoreCase(keyword));
    }

    // [마이페이지용] 키워드 검색: 상품명만
    private BooleanExpression containsKeywordForUser(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return null;
        }

        // 필요하면 주문번호 등 추가 가능
        return orderItem.productName.containsIgnoreCase(keyword);
    }

    // 기간 필터: orderDate(LocalDateTime) 기준
    private BooleanExpression betweenOrderDate(LocalDate start, LocalDate end) {
        if (start == null && end == null) {
            return null;
        }

        LocalDateTime startDt = null;
        LocalDateTime endDt = null;

        if (start != null) {
            startDt = start.atStartOfDay();
        }
        if (end != null) {
            // end 날짜의 23:59:59까지 포함되도록 다음날 0시 미만으로 처리
            endDt = end.plusDays(1).atStartOfDay();
        }

        if (startDt != null && endDt != null) {
            return order.orderDate.goe(startDt).and(order.orderDate.lt(endDt));
        } else if (startDt != null) {
            return order.orderDate.goe(startDt);
        } else {
            return order.orderDate.lt(endDt);
        }
    }
}
