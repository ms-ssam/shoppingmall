package com.example.elicesecondproject.mall.domain.qna.repository;

import com.example.elicesecondproject.mall.domain.qna.entity.Question;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface QuestionRepository extends JpaRepository<Question,Long>, QuestionRepositoryCustom {
    Page<Question> findByMemberIdAndDeletedAtIsNull(Long memberId, Pageable pageable);
    @Query("""
        select q from Question q
        left join fetch q.member
        left join fetch q.product
        left join fetch q.answer
        where q.id = :questionId
          and q.deletedAt is null
    """)
    Optional<Question> findDetailById(@Param("questionId") Long questionId);

    @Query("""
        select q from Question q
        left join fetch q.answer a
        left join fetch a.admin
        where q.id = :questionId
          and q.deletedAt is null
    """)
    Optional<Question> findForAnswerById(@Param("questionId") Long questionId);
}
