package com.example.elicesecondproject.mall.domain.member.entity;

import com.example.elicesecondproject.mall.domain.address.entity.Address;
import com.example.elicesecondproject.mall.domain.review.entity.Review;
import com.example.elicesecondproject.mall.global.entity.BaseEntity;
import com.example.elicesecondproject.mall.global.entity.SoftDeletableBaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Table(name = "members")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Member extends SoftDeletableBaseEntity implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false)
    private Long id;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "nickname", nullable = false, length = 50)
    private String nickname;

    @Column(name = "phone", nullable = false, length = 50)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private MemberStatus status = MemberStatus.ACTIVE;

    @Column(name = "email_verified",  nullable = false)
    private boolean emailVerified = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 20)
    private AuthProvider provider = AuthProvider.LOCAL;

    @Column(name = "provider_id", length = 100)
    private String providerId;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    // --- 연관 관계 ---

    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY)
    private List<Address> addresses;

    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY)
    private List<Review> reviews;

    /*
    @OneToOne(mappedBy = "member", fetch = FetchType.LAZY)
    private Cart cart;
    */

    // ----------
    @Builder
    public Member(String email,
                  String password,
                  String name,
                  String nickname,
                  String phone,
                  Role role) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.nickname = nickname;
        this.phone = phone;
        this.role = (role != null) ? role : Role.USER;
    }

    // --- 도메인 메서드 ---

    public void markEmailVerified() {
        this.emailVerified = true;
    }

    public void updateLastLoginAt(LocalDateTime now) {
        this.lastLoginAt = now;
    }

    public void ban() {
        this.status = MemberStatus.BANNED;
    }

    public void activate() {
        this.status = MemberStatus.ACTIVE;
    }

    public void withdraw(LocalDateTime when) {
        this.status = MemberStatus.WITHDRAWN;
    }


    // --- UserDetails 구현 ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.getKey()));
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}
