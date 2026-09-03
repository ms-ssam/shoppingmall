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
    """)
    Optional<Question> findDetailById(@Param("questionId") Long questionId);

    @Query("""
        select q from Question q
        left join fetch q.answer a
        left join fetch a.admin
        where q.id = :questionId
    """)
    Optional<Question> findForAnswerById(@Param("questionId") Long questionId);

    // 해당 productId를 가진 question들을 가져오기
    @Query("""
        select q
        from Question q
        where q.product.id = :productId
        order by q.createdAt desc
    """)
    Page<Question> findByProductId(@Param("productId") Long productId, Pageable pageable);


    // 가장 최근 문의한 건
    Optional<Question> findTopByMemberIdAndProductIdOrderByIdDesc(Long memberId, Long productId);

}
