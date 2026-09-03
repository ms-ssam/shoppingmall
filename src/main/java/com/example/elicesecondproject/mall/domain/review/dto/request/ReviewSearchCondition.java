package com.example.elicesecondproject.mall.domain.review.dto.request;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
public class ReviewSearchCondition {
    private Long productId;
    private String productName;

    private String memberNickname;

    private Integer minRating;
    private Integer maxRating;

    private Boolean deleted;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;
}
