package com.example.elicesecondproject.mall.domain.qna.entity;

import com.example.elicesecondproject.mall.domain.member.entity.Member;
import com.example.elicesecondproject.mall.domain.product.entity.Product;
import com.example.elicesecondproject.mall.global.common.Ownable;
import com.example.elicesecondproject.mall.global.entity.SoftDeletableBaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Question extends SoftDeletableBaseEntity implements Ownable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(length = 30,  nullable = false)
    private  String title;

    @Column(length = 255,  nullable = false)
    private  String content;

    @Column(nullable = false)
    private boolean secret;

    // isAnswer -> answered: javaBean 규약으로 라이브러리들의 인식 오류 방지
    @Column(nullable = false)
    private boolean answered = false;

    @OneToOne(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private Answer answer;

    @Builder
    private Question(Product product, String title, String content, boolean secret) {
        this.product = product;
        this.title = title;
        this.content = content;
        this.secret = secret;
    }

    public static Question of(Member member, Product product, String title, String content, boolean secret){
        Question question = Question.builder()
                .product(product)
                .title(title)
                .content(content)
                .secret(secret)
                .build();

        member.addQuestion(question);

        return question;
    }


    public void registerAnswer(Answer answer) {
        this.answer = answer;
        answer.setQuestion(this);
        // 필드 추가
        this.answered = true;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    // 답변 삭제시 매서드
    public void removeAnswer(){
        if (this.answer == null) return;

        Answer old = this.answer;
        this.answer = null;
        old.setQuestion(null); // 객체 그래프 정리
        this.answered = false;
    }

    @Override
    public Long getOwnerId() {
        return member.getId();
    }
}
