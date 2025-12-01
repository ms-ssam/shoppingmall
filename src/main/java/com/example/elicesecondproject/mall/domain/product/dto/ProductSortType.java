package com.example.elicesecondproject.mall.domain.product.dto;

public enum ProductSortType {
    LATEST,        // 최신순 (productId DESC)
    PRICE_LOW,     // 가격 낮은순 (price ASC)
    PRICE_HIGH,    // 가격 높은순 (price DESC)
    FAVORITE_COUNT // 찜 많은순 (favoriteCount DESC)
}
