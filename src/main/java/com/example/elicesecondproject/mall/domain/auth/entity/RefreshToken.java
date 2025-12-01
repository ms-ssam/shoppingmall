package com.example.elicesecondproject.mall.domain.auth.entity;

import com.example.elicesecondproject.mall.domain.member.entity.Member;
import com.example.elicesecondproject.mall.global.entity.BaseCreatedEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_tokens")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class RefreshToken extends BaseCreatedEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id",  updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "refresh_token_hash",  nullable = false, length = 255)
    private String refreshTokenHash;

    @Column(name = "member_agent",   nullable = false, length = 255)
    private String memberAgent;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "is_revoked",  nullable = false)
    private boolean isRevoked;

    public boolean isExpired() {
        return expiresAt.isBefore(LocalDateTime.now());
    }

    public void revoke() {
        this.isRevoked = true;
    }
}
