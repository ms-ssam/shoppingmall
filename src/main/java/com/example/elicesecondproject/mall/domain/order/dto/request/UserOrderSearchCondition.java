package com.example.elicesecondproject.mall.domain.order.dto.request;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
public class UserOrderSearchCondition {
    // 키워드 검색(상품명)
    private String keyword;

    // 시작일 (orderDate 기준)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    // 종료일
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
}
