package com.example.elicesecondproject.mall.domain.product.entity;


import com.example.elicesecondproject.mall.domain.member.entity.Member;
import com.example.elicesecondproject.mall.global.entity.BaseCreatedEntity;
import com.example.elicesecondproject.mall.global.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class WishList extends BaseCreatedEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //     member 객체와 연결
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Member member;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Product product;

    @Builder
    public WishList(Member member, Product product){
        this.member = member;
        this.product = product;
    }

}