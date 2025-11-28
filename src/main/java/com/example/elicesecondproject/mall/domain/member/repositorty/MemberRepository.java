package com.example.elicesecondproject.mall.domain.member.repositorty;

import com.example.elicesecondproject.mall.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);
}
