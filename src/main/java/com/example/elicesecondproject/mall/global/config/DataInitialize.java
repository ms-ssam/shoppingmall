package com.example.elicesecondproject.mall.global.config;

import com.example.elicesecondproject.mall.domain.member.entity.Member;
import com.example.elicesecondproject.mall.domain.member.entity.Role;
import com.example.elicesecondproject.mall.domain.member.repositorty.MemberRepository;
import com.example.elicesecondproject.mall.domain.product.entity.Product;
import com.example.elicesecondproject.mall.domain.product.entity.WishList;
import com.example.elicesecondproject.mall.domain.product.repository.ProductRepository;
import com.example.elicesecondproject.mall.domain.product.repository.WishListRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitialize implements CommandLineRunner {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProductRepository productRepository;
    private final WishListRepository wishListRepository;

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

        // 3. WishList 초기 데이터 생성
        if (wishListRepository.count() == 0) {

            // --- 회원 조회 ---
            Member admin = memberRepository.findByEmail("admin@test.com")
                    .orElseThrow(() -> new IllegalStateException("admin@test.com 없음"));
            Member user = memberRepository.findByEmail("user@test.com")
                    .orElseThrow(() -> new IllegalStateException("user@test.com 없음"));

            // --- 상품 조회 ---
            Product p1 = productRepository.findById(1L).orElseThrow();
            Product p2 = productRepository.findById(2L).orElseThrow();
            Product p3 = productRepository.findById(3L).orElseThrow();
            Product p4 = productRepository.findById(4L).orElseThrow();
            Product p5 = productRepository.findById(5L).orElseThrow();
            Product p6 = productRepository.findById(6L).orElseThrow();
            Product p7 = productRepository.findById(7L).orElseThrow();
            Product p8 = productRepository.findById(8L).orElseThrow();
            Product p9 = productRepository.findById(9L).orElseThrow();

            // --- 위시리스트 더미 데이터 ---
            // 관리자(admin@test.com)
            wishListRepository.save(new WishList(admin, p1)); // 관리자 → 상품 1
            wishListRepository.save(new WishList(admin, p4)); // 관리자 → 상품 4
            wishListRepository.save(new WishList(admin, p9)); // 관리자 → 상품 9

            // 일반 사용자(user@test.com)
            wishListRepository.save(new WishList(user, p2)); // 유저 → 상품 2
            wishListRepository.save(new WishList(user, p3)); // 유저 → 상품 3
            wishListRepository.save(new WishList(user, p6)); // 유저 → 상품 6
            wishListRepository.save(new WishList(user, p7)); // 유저 → 상품 7
            wishListRepository.save(new WishList(user, p8)); // 유저 → 상품 8
            wishListRepository.save(new WishList(user, p5)); // 유저 → 상품 5

            System.out.println(">>> WishList 더미 데이터 생성 완료");
        }
    }
}