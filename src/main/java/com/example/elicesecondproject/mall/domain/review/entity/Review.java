package com.example.elicesecondproject.mall.domain.review.entity;

import com.example.elicesecondproject.mall.domain.member.entity.Member;
import com.example.elicesecondproject.mall.domain.product.entity.Product;
import com.example.elicesecondproject.mall.global.common.Ownable;
import com.example.elicesecondproject.mall.global.entity.SoftDeletableBaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review extends SoftDeletableBaseEntity implements Ownable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false)
    private Integer rating;

    @Column(nullable = false)
    private String content;

    @Column(name = "image_url")
    private String imageUrl;

    public void updateAll(Integer rating, String content, String imageUrl) {
        this.rating = rating;
        this.content = content;
        this.imageUrl = imageUrl;

    }

    @Override
    public Long getOwnerId() {
        return member.getId();
    }
}
