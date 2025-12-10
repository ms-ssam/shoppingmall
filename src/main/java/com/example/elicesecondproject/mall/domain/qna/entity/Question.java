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
public class xQuestion extends SoftDeletableBaseEntity {
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