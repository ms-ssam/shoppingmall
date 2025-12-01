package com.example.elicesecondproject.mall.domain.Member.service;

import com.example.elicesecondproject.mall.domain.Member.entity.Member;
import com.example.elicesecondproject.mall.domain.Member.entity.MemberDetail;
import com.example.elicesecondproject.mall.domain.Member.repositorty.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class MemberDetailService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email));
        return new MemberDetail(member);

    }
}
