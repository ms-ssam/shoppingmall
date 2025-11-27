package com.example.elicesecondproject.mall.product.domain;


import com.example.elicesecondproject.mall.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
public class Product extends BaseTimeEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductOption> options = new ArrayList<>();


    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false) // 기본 가격 -> 옵션마다 추가 금액이 있는 방식
    @Min(0)
    private int price;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    @Min(0)
    @Max(5)
    private double averageRating = 0.0;

    @Column(nullable = false)
    @Min(0)
    private int reviewCount = 0;

    @Version
    private Long version;

    private String mainImageUrl;

    private LocalDateTime deletedAt;

    @Builder
    public Product(String name, int price, String description, Category category, String mainImageUrl) {
        this.name = name;
        this.price = price;
        this.description = description;
        this.category = category;
        this.mainImageUrl = mainImageUrl;
    }

    // 이름, 가격, 설명, 카테고리, 메인 이미지 설정 변경 시 사용
    public void updateDetails(String name, int price, String description, Category category, String mainImageUrl) {
        this.name = name;
        this.price = price;
        this.description = description;
        this.category = category;
        this.mainImageUrl = mainImageUrl;
    }

    // 옵션 부분 데이터 동기화(주인 부분에서 initProduct 정의)
    public void addOption(ProductOption option) {
        this.options.add(option);        // Product 쪽 컬렉션 추가
        option.initProduct(this);         // 주인 쪽 필드 설정
    }



    public void delete() { // soft delete
        this.deletedAt = LocalDateTime.now();
    }

    public void restore() { // soft delete 상품 복구
        this.deletedAt = null;
    }

    // 별점이랑 리뷰수 최신화 시 사용
    public void updateRating(double averageRating, int reviewCount) {
        // 소수점 첫째 자리까지 반올림
        this.averageRating = Math.round(averageRating * 10.0) / 10.0;
        this.reviewCount = reviewCount;
    }

    //todo:  equals, hashCode 오버라이드 (식별성 보장) 내용 고려하기  -> x
    //todo: 동시성 문제 -> 일단 낙관적 락으로 구현 후 레디스 캐싱으로 해결해보기
    //todo: 실시간 수정이랑 Batch 같이 사용하는 방식으로 repo 작성

}
