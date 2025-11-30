package com.example.elicesecondproject.mall.domain.category.entity;

import com.example.elicesecondproject.mall.global.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    @BatchSize(size = 100)
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private List<Category> children = new ArrayList<>();

    @NotBlank(message = "카테고리명은 필수입니다.")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "슬러그는 필수입니다.")
    @Column(nullable = false, unique = true, length = 100)
    private String slug; // URL용 고유 문자열 (예: mens-outer)

    // [경로] 검색 성능을 위한 반정규화 필드 (Service에서 생성 로직 관리 필요)
    @Column(nullable = false)
    private String path; // ex: /1/5/19/

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

    private LocalDateTime deletedAt;

    @Builder
    public Category(Category parent, String name, String slug, String path, Integer depth, Integer displayOrder, Boolean isVisible) {
        this.parent = parent;
        this.name = name;
        this.slug = slug;
        this.path = path;
        this.depth = depth;
        this.displayOrder = displayOrder;
        this.isVisible = isVisible != null ? isVisible : true;
    }

    public void delete() {
        this.deletedAt = LocalDateTime.now();
        for (Category child : children) {
            child.delete();
        }
    }

    public void updateDetails(String name, String slug, Boolean isVisible, Integer displayOrder) {
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
        if (slug != null && !slug.isBlank()) {
            this.slug = slug;
        }
        if (isVisible != null) {
            this.isVisible = isVisible;
        }
        if (displayOrder != null) {
            this.displayOrder = displayOrder;
        }
    }

    // 편의 메서드: 자식 추가
    public void addChild(Category child) {
        this.children.add(child);

    }
}
    //Todo 서비스에서 카테고리 이동(부모 변경) - moveCategory() 고려해보기
    /*public void changeStructure(Category newParent, String newPath, Integer newDepth) {
        this.parent = newParent;
        this.path = newPath;
        this.depth = newDepth;
    }*/
