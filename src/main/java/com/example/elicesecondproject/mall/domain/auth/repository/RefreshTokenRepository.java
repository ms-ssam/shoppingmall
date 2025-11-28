package com.example.elicesecondproject.mall.domain.auth.repository;

import com.example.elicesecondproject.mall.domain.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByRefreshTokenHashAndIsRevokedFalse(String hash);
}
