package com.example.elicesecondproject.mall.domain.category.repository;

import com.example.elicesecondproject.mall.domain.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByIdAndDeletedAtIsNull(Long id);

    boolean existsBySlug(String slug);

    List<Category> findByParentIsNullAndDeletedAtIsNullAndIsVisibleTrue();

    List<Category> findByParentIsNullAndDeletedAtIsNull();

    List<Category> findByParentIdAndDeletedAtNull(Long parentId);
}
