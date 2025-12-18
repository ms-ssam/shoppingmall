package com.example.elicesecondproject.mall.domain.category.entity;

import com.example.elicesecondproject.mall.global.entity.SoftDeletableBaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "categorys")
public class Category extends SoftDeletableBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    @BatchSize(size = 100)
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private List<Category> children;

    @NotBlank(message = "카테고리명은 필수입니다.")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "슬러그는 필수입니다.")
    @Column(nullable = false, unique = true, length = 100)
    private String slug;

    @NotNull(message = "깊이 정보는 필수입니다.")
    @Min(0)
    @Column(nullable = false)
    private Integer depth;

    @NotNull(message = "정렬 순서는 필수입니다.")
    @Column(nullable = false)
    private Integer displayOrder;

    @NotNull
    @Column(nullable = false)
    private Boolean isVisible;

    @Builder
    public Category(String name, String slug, Integer displayOrder, Boolean isVisible, Integer depth) {
        this.name = name;
        this.slug = slug;
        this.displayOrder = displayOrder;
        this.isVisible = isVisible != null ? isVisible : true;
        // 입력값이 있으면 사용하고, 없으면 기본값 설정
        this.depth = (depth != null) ? depth : 0;

        this.children = new ArrayList<>();
    }

    public void updateDetails(String name, String slug, Boolean isVisible, Integer displayOrder) {
        if (name != null && !name.isBlank()) { this.name = name; }
        if (slug != null && !slug.isBlank()) { this.slug = slug; }
        if (isVisible != null) { this.isVisible = isVisible; }
        if (displayOrder != null) { this.displayOrder = displayOrder; }
    }

    public void setParent(Category parent) {
        this.parent = parent;

        if (parent == null) {
            this.depth = 0;
        } else {
            this.depth = parent.getDepth() + 1;

            if (!parent.getChildren().contains(this)) {
                parent.getChildren().add(this);
            }
        }
    }


}