package com.example.elicesecondproject.mall.domain.order.dto.request;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
public class AdminOrderSearchCondition {
    // 주문 상태별 조회 (예: PENDING, PAID, SHIPPING, COMPLETED, CANCELLED 등)
    private String orderStatus;

    // 키워드 검색(주문번호, 상품명, 고객명 등 통합 검색)
    private String keyword;

    // 시작일 (orderDate 기준)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    // 종료일
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
}
