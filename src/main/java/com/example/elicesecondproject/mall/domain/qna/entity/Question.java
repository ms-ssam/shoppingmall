package com.example.elicesecondproject.mall.domain.qna.entity;

import com.example.elicesecondproject.mall.domain.member.entity.Member;
import com.example.elicesecondproject.mall.domain.product.entity.Product;
import com.example.elicesecondproject.mall.global.entity.SoftDeletableBaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Question extends SoftDeletableBaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(length = 100,  nullable = false)
    private  String title;

    @Column(length = 100,  nullable = false)
    private  String content;

    @Column(nullable = false)
    private boolean isSecret;

    @OneToOne(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private Answer answer;

    @Builder
    public Question(Member member, Product product, String title, String content, boolean isSecret) {
        this.member = member;
        this.product = product;
        this.title = title;
        this.content = content;
        this.isSecret = isSecret;
    }


    public void registerAnswer(Answer answer) {
        this.answer = answer;
        answer.setQuestion(this);
    }
}
