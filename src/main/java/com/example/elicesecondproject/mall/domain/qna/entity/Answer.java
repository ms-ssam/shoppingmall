package com.example.elicesecondproject.mall.domain.qna.entity;

import com.example.elicesecondproject.mall.domain.member.entity.Member;
import com.example.elicesecondproject.mall.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "answers")
public class Answer extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id",  nullable = false)
    private Question question;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id",  nullable = false)
    private Member admin; // 답변 관리자

    @Column(nullable = false, length = 255)
    private String content;

    @Builder
    private Answer(Member admin, String content) {
        this.admin = admin;
        this.content = content;
    }

    public static Answer of(Question question, Member admin, String content) {
        Answer answer = Answer.builder()
                .admin(admin)
                .content(content)
                .build();

        question.registerAnswer(answer); // 양방향 세팅

        return answer;
    }

    void setQuestion(Question question) {
        this.question = question;
    }
}