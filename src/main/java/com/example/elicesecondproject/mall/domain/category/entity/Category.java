package com.example.elicesecondproject.mall.domain.category.entity;

import com.example.elicesecondproject.mall.global.entity.BaseEntity;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category extends SoftDeletableBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    // [중요] @Builder 사용 시 필드 초기화(= new ArrayList<>())는 무시될 수 있음 -> 생성자에서 직접 초기화해야 함
    @BatchSize(size = 100)
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private List<Category> children; // 여기서 초기화하지 말고 생성자에서 함

    @NotBlank(message = "카테고리명은 필수입니다.")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "슬러그는 필수입니다.")
    @Column(nullable = false, unique = true, length = 100)
    private String slug;

    // 초기값은 setParent 혹은 completePath에서 설정되므로 생성 시점엔 빈 값 허용
    @Column(nullable = false)
    private String path;

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
    public Category(String name, String slug, Integer displayOrder, Boolean isVisible) {
        this.name = name;
        this.slug = slug;
        this.displayOrder = displayOrder;
        this.isVisible = isVisible != null ? isVisible : true;
        this.depth = 0;
        this.path = "/";
        this.children = new ArrayList<>();
    }


    // 서비스 대신 엔티티가 스스로 수정 (객체지향적 설계)
    public void updateDetails(String name, String slug, Boolean isVisible, Integer displayOrder) {
        if (name != null && !name.isBlank()) { this.name = name; }
        if (slug != null && !slug.isBlank()) { this.slug = slug; }
        if (isVisible != null) { this.isVisible = isVisible; }
        if (displayOrder != null) { this.displayOrder = displayOrder; }
    }

    // [핵심] 구조 설정 로직 (생성 시 필수 호출)
    public void setParent(Category parent) {
        this.parent = parent;

        if (parent == null) {
            this.depth = 0;
            this.path = "/";
        } else {
            this.depth = parent.getDepth() + 1;
            this.path = parent.getPath();

            // 부모 리스트에 내가 없으면 추가 (NPE 방지를 위해 children 초기화 필수)
            if (!parent.getChildren().contains(this)) {
                parent.getChildren().add(this);
            }
        }
    }

    // 저장 후 ID가 생긴 뒤 경로 완성용 메서드
    public void completePath() {
        this.path = (this.parent == null)
                ? "/" + this.id + "/"
                : this.parent.getPath() + this.id + "/";
    }

}