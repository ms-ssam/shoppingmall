package com.example.elicesecondproject.mall.domain.product.entity;

public enum ProductStatus {
    SELLING,    // 판매중
    STOP,       // 판매중지 (관리자에 의해 숨김 처리됨)
    SOLD_OUT    // 품절 (재고가 0이거나 수동 품절 처리)
}

/*
 [Design & Implementation Note]
 - SELLING: 정상 노출.
 - STOP: 상품 데이터는 유효하나, 쇼핑몰 목록 및 상세 페이지 접근을 막아야 함.
 - SOLD_OUT: 상품 상세 페이지 접근은 가능하나, '구매하기' 버튼을 비활성화해야 함.
*/