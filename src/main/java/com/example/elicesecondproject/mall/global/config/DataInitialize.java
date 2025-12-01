package com.example.elicesecondproject.mall.global.config;

import com.example.elicesecondproject.mall.domain.member.entity.Member;
import com.example.elicesecondproject.mall.domain.member.entity.Role;
import com.example.elicesecondproject.mall.domain.member.repositorty.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitialize implements CommandLineRunner {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {

        // 1. 관리자 계정 생성
        if (memberRepository.findByEmail("admin@test.com").isEmpty()) {
            Member admin = Member.builder()
                    .email("admin@test.com")
                    .password(passwordEncoder.encode("1234"))
                    .name("관리자")
                    .nickname("AdminUser")
                    .phone("010-0000-0000")
                    .role(Role.ADMIN)
                    .build();

            memberRepository.save(admin);
            System.out.println(">>> 관리자 계정 생성 완료: admin@test.com / 1234");
        }

        // 2. 일반 사용자 계정 생성
        if (memberRepository.findByEmail("user@test.com").isEmpty()) {
            Member user = Member.builder()
                    .email("user@test.com")
                    .password(passwordEncoder.encode("1234"))
                    .name("일반유저")
                    .nickname("GeneralUser")
                    .phone("010-1111-1111")
                    .role(Role.USER)
                    .build();

            memberRepository.save(user);
            System.out.println(">>> 일반 사용자 계정 생성 완료: user@test.com / 1234");
        }
    }
}