package com.example.elicesecondproject.mall.domain.cart.entity;

import com.example.elicesecondproject.mall.domain.option.entity.OptionDetail;
import com.example.elicesecondproject.mall.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "cart_items")
@Entity
public class CartItem extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)  //!❗️ N+1 터질 가능성 높다고 판단됨. 조회할 때 fetch join 하거나 EntityGraph 고려 (장바구니 항목 조회하는 경우 보통 제품 옵션 및 정보 필요)
    @JoinColumn(name = "product_option_detail_id", nullable = false)
    private OptionDetail productOptionDetail;

    @Column(nullable = false)
    private int quantity;

    // === 정적 팩토리 메서드 ===
    public static CartItem of(OptionDetail optionDetail, int quantity) {
        CartItem cartItem = new CartItem();
        cartItem.productOptionDetail = optionDetail;
        cartItem.quantity = quantity;

        return cartItem;
    }

    // === 연관관계 편의 메서드 ===
    protected void setCart(Cart cart) {
        this.cart = cart;
    }

    public void updateQuantity(int quantity) {
        this.quantity = quantity;
    }
}
