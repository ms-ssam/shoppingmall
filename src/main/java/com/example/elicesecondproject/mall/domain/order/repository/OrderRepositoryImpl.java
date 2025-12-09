package com.example.elicesecondproject.mall.domain.order.repository;

import com.example.elicesecondproject.mall.domain.member.entity.QMember;
import com.example.elicesecondproject.mall.domain.order.dto.request.AdminOrderSearchCondition;
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
    private final QMember member = QMember.member;

    @Override
    public Page<Order> searchOrders(AdminOrderSearchCondition condition, Pageable pageable) {

        List<Order> contents = queryFactory
                .selectDistinct(order)
                .from(order)
                .leftJoin(order.member, member).fetchJoin()
                .leftJoin(order.orderItems, orderItem)
                .where(
                        eqStatus(condition.getOrderStatus()),
                        containsKeyword(condition.getKeyword()),
                        betweenOrderDate(condition.getStartDate(), condition.getEndDate()),
                        isNotDeleted() // SoftDeletableBaseEntity 사용 시
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(order.id.desc())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(order.countDistinct())
                .from(order)
                .leftJoin(order.member, member)
                .leftJoin(order.orderItems, orderItem)
                .where(
                        eqStatus(condition.getOrderStatus()),
                        containsKeyword(condition.getKeyword()),
                        betweenOrderDate(condition.getStartDate(), condition.getEndDate()),
                        isNotDeleted()
                );

        // PageableExecutionUtils를 쓰면 count 쿼리를 최적화해서 덜 날릴 수 있음
        return PageableExecutionUtils.getPage(contents, pageable, countQuery::fetchOne);
        // 단순하게 쓰고 싶으면 아래도 가능
        // long total = countQuery.fetchOne();
        // return new PageImpl<>(contents, pageable, total);
    }

    /* ===============================
        BooleanExpression helpers
       =============================== */

    // 주문 상태 필터 (String -> Enum 매핑)
    private BooleanExpression eqStatus(String status) {
        if (!StringUtils.hasText(status)) {
            return null;
        }
        try {
            OrderStatus enumStatus = OrderStatus.valueOf(status);
            return order.orderStatus.eq(enumStatus);
        } catch (IllegalArgumentException e) {
            // 잘못된 status 값이 온 경우: 필터 적용 안 함 or 항상 false
            return null;
        }
    }

    // 키워드 검색: 상품명 + 고객명 (주문번호 필드는 엔티티에 생기면 여기 추가)
    private BooleanExpression containsKeyword(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return null;
        }

        return orderItem.productName.containsIgnoreCase(keyword)
                .or(member.name.containsIgnoreCase(keyword));
        // 주문번호 필드를 나중에 Order에 추가하면 예:
        // .or(order.orderNumber.containsIgnoreCase(keyword))
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

    // Soft 삭제 컬럼(deletedAt) 필터 (SoftDeletableBaseEntity 기준)
    private BooleanExpression isNotDeleted() {
        // SoftDeletableBaseEntity에 deletedAt 필드가 있다고 가정
        return order.deletedAt.isNull();
    }
}
