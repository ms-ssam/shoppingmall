package com.example.elicesecondproject.mall.domain.cart.entity;

import com.example.elicesecondproject.mall.domain.member.entity.Member;
import com.example.elicesecondproject.mall.global.common.Ownable;
import com.example.elicesecondproject.mall.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

// QueryDSL 설정이랑 BaseTimeEntity 필요
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "carts")
@Entity
public class Cart extends BaseEntity implements Ownable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "member_id", nullable = false, unique = true)
    private Member member;

    //❗️ 보통 장바구니 조회하면 장바구니 항목들도 같이 불러오니까 N+1 방지 위해 반드시 fetch join 혹은 EntityGraph 설정 필요!
    @OneToMany(mappedBy = "cart", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> cartItems = new ArrayList<>();

    private int totalCount;



    // 정적 팩토리 메서드
    public static Cart create() {
        return new Cart();
    }

    // === entity 상태 수정 메서드  ===
    public void removeItems(List<CartItem> itemsToRemove) {  // 장바구니 일부 비우기
        for (CartItem item : itemsToRemove) {
            removeItem(item);
        }
    }

    public void clearItems() {  // 장바구니 전체 비우기
        removeItems(new ArrayList<>(cartItems));
    }

    // === 연관관계 편의 메서드 ===
    public void addItem(CartItem item) {  // 장바구니에서 장바구니 항목 추가
        cartItems.add(item);
        item.setCart(this);
        totalCount++;
    }

    public void removeItem(CartItem item) {  // 장바구니에서 장바구니 항목 삭제
        cartItems.remove(item);
        item.setCart(null);  // casecade + orphanRemoval 때문에 flush 시점에 DB에서 삭제됨
    }

    public void setMember(Member member) {
        this.member = member;
    }

    @Override
    public Long getOwnerId() {
        return member.getId();
    }
}

/*
 장바구니
    - 장바구니에는 여러 개의 장바구니 항목(상품)들이 들어갈 수 있음 -> 연관괸계 매핑 ✅



    - 장바구니 항목과 상품은 같이 변경되도록. (같은 객체) ✅?
        - 상품 정보가 변경되면 장바구니에 있던 해당 상품도 자동으로 같이 변경되도록 설계
        - 무신사 써보면 장바구니에 담아뒀던 상품의 가격이 할인률에 따라 변경되면 장바구니에도 변경된 가격으로 나와있음
        - ++) 최근 생긴 기능?인지는 모르겠지만 내가 장바구니에 담았던 시점보다 현재 가격이 얼마나 더 싸졌는지 알려주는 기능 있음.
            이거 구현해주려면 장바구니도 이력 관리 필요? 이력 관리는 어떻게?

       - 이렇게 구현하면 장바구니에 있는 장바구니 항목에 대한 validation은 필요 X? (장바구니 항목 정보 != 실제 해당 상품 정보)
            - validate 메서드 만드는 대신 테스트 코드로?



    - 사용자 한명 당 장바구니 1개 ✅
        - 사용자 생성 시점에 장바구니 같이 생성 + 사용자 삭제되면 장바구니도 같이 삭제? -> 사용자 계정 삭제 시 장바구니도 같이 사라짐
        @OneToOne(mappedBy="...", cascade=CascadeType.ALL, orphanRemoval=true)
        private Cart cart;

        public void assignCart(Cart cart) { this.cart = cart;   cart.setMember(this); }

        service layer signUp 로직 안에서 사용자와 장바구니 객체 생성 후 member.assignCart(cart) 해주고 member를 repo에 저장
            -> Cart의 FK 갖고 있으므로 같이 persist 된다?



    - 장바구니에 있는 상품을 통해 주문을 생성 (주문서)
        - 무신사 앱 장바구니에서 [주문하기] 버튼 클릭하고 넘어가는 화면 생각해보기
            - 장바구니 안에 있던 각 장바구니 항목들, 총 가격, 결제 수단 선택 등등 보여줌
            - 근데 여기서 만약 뒤로가기 하면? -> 장바구니 비워져 있으면 안됨
            - 장바구니 안의 장바구니 항목들이 비워지는 시점은 해당 주문서가 실제로 '결제되었을 때'
 */