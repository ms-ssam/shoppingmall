package com.example.elicesecondproject.mall.domain.product.dto;

public enum ProductSortType {
    LATEST,          // 최신순
    PRICE_HIGH,      // 가격 높은순
    PRICE_LOW,       // 가격 낮은순
    REVIEW_COUNT,    // 리뷰 많은순
    WISHLIST_COUNT,  // 찜 많은순
    RATING,           // 평점 높은순

    // 재고순
    STOCK_HIGH,      // 재고 많은순
    STOCK_LOW        // 재고 적은순
}