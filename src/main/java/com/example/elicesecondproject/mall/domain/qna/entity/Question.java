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
        this.answer = null;
        this.answered = false;
    }
}
/*
관리 페이지
관리자가 답변 질문에 답변을 하는 식
조회
질문 id/ 질문한 사람이랑, id/ 제목 / 상품 이름이랑 옵션, id / 등록일 / 숨김 여부 / 추가하면 답변여부? boolen?
- 상태 넣으면 상태 변경도 일괄, 단건 있나? -> 추후로 빼고

등록
- 사용자 등록 -Q

- 관리자 등록 -A
*/