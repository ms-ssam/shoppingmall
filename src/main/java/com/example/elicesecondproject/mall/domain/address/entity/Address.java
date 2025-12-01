package com.example.elicesecondproject.mall.domain.address.entity;

import com.example.elicesecondproject.mall.domain.member.entity.Member;
import com.example.elicesecondproject.mall.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "addresses")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Address extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 회원 ID (members.id 참조)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    // 수령자 이름
    @Column(name = "receiver_name", nullable = false, length = 50)
    private String receiverName;

    // 수령자 전화번호
    @Column(name = "receiver_phone", nullable = false, length = 20)
    private String receiverPhone;

    // 우편번호
    @Column(name = "zipcode", nullable = false, length = 10)
    private String zipcode;

    // 주소 식별 이름 (집, 회사, 학교 등)
    @Column(name = "address_label", nullable = false, length = 10)
    private String addressLabel;

    // 기본 주소 (도로명/지번)
    @Column(name = "address1", nullable = false, length = 255)
    private String address;

    // 상세 주소 (동/호수 등)
    @Column(name = "address2", length = 255)
    private String address2;

    // 기본 배송지 여부 (TINYINT(1) ↔ boolean)
    @Column(name = "is_default", nullable = false)
    private boolean isDefault;


    // --- 도메인 메서드 ---
    
    public void markDefault() {
        this.isDefault = true;
    }

    public void unmarkDefault() {
        this.isDefault = false;
    }


}
