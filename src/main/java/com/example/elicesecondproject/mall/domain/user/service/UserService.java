package com.example.elicesecondproject.mall.domain.user.service;

import com.example.elicesecondproject.mall.domain.user.entity.Role;
import com.example.elicesecondproject.mall.domain.user.entity.User;
import com.example.elicesecondproject.mall.domain.user.dto.AddUserRequest;
import com.example.elicesecondproject.mall.domain.user.repositorty.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Transactional
    public Long save(AddUserRequest dto) {
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }

        return userRepository.save(User.builder()
                .email(dto.getEmail())
                .password(bCryptPasswordEncoder.encode(dto.getPassword()))
                .role(Role.USER)
                .build()).getId();
    }
}
