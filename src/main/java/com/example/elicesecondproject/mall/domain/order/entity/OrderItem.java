package com.example.elicesecondproject.mall.domain.order.entity;

import com.example.elicesecondproject.mall.domain.cart.entity.CartItem;
import com.example.elicesecondproject.mall.domain.option.entity.OptionDetail;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "order_items")
@Entity
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    private Long productId;
    private String productName;
    private String productThumbnailUrl;

    private Long optionDetailId;
    private String optionDetail;

    private String productOptionGroupName;
    private int unitPrice;
    private int quantity;
    private int subtotalPrice;

    // === 정적 팩토리 메서드 ===
    private static OrderItem of(OptionDetail optionDetail, int quantity) {

        OrderItem item = new OrderItem();

        item.productId = optionDetail.getProduct().getId();
        item.productName = optionDetail.getProduct().getName();     // 상품명
        item.productThumbnailUrl = optionDetail.getProduct().getThumbnailUrl();

        item.productOptionGroupName = optionDetail.getProductOptionGroup().getName();   // 색상

        item.optionDetailId = optionDetail.getId();
        item.optionDetail = optionDetail.getName();                 // 사이즈

        item.unitPrice = optionDetail.getSaleUnitPrice();           // 최종 판매가 (할인가 + 옵션 추가금)
        item.quantity = quantity;
        item.subtotalPrice = item.unitPrice * quantity;

        return item;
    }

    public static OrderItem fromCartItem(CartItem cartItem) {
        OptionDetail optionDetail = cartItem.getProductOptionDetail();
        int quantity = cartItem.getQuantity();

        return OrderItem.of(optionDetail, quantity);
    }

    //=== 연관 관계 편의 메서드 ===
    protected void setOrder(Order order) {
        this.order = order;
    }
}
