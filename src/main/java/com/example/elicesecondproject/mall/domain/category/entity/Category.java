package com.example.elicesecondproject.mall.domain.category.entity;

import com.example.elicesecondproject.mall.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    @OneToMany(mappedBy = "parent")
    private List<Category> children = new ArrayList<>();

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String path; // ex: /1/5/19/

    @Column(nullable = false)
    private Integer depth;

    @Column(nullable = false)
    private Integer displayOrder; // 같은 부모 내에서의 정렬 순서

    @Column(nullable = false)
    private Boolean isVisible; // 전시 여부 (단순 ON/OFF)

    private LocalDateTime deletedAt;

    @Builder
    public Category(Category parent, String name, String path, Integer depth, Integer displayOrder, Boolean isVisible) {
        this.parent = parent;
        this.name = name;
        this.path = path;
        this.depth = depth;
        this.displayOrder = displayOrder;
        this.isVisible = isVisible != null ? isVisible : true; // 기본값 True
    }

    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }

    public void updateDetails(String name, Boolean isVisible, Integer sortOrder) {
        this.name = name;
        this.isVisible = isVisible;
        this.displayOrder = sortOrder;
    }

    //Todo 서비스에서 카테고리 이동(부모 변경) - moveCategory() 고려해보기
    /*public void changeStructure(Category newParent, String newPath, Integer newDepth) {
        this.parent = newParent;
        this.path = newPath;
        this.depth = newDepth;
    }*/
}