package com.example.elicesecondproject.mall.domain.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthTokens {
    private final String accessToken;
    private final String refreshToken;
}
