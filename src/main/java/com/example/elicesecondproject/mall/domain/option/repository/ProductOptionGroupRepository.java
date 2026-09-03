package com.example.elicesecondproject.mall.domain.option.repository;

import com.example.elicesecondproject.mall.domain.option.entity.ProductOptionGroup;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductOptionGroupRepository extends JpaRepository<ProductOptionGroup, Long> {
    @EntityGraph(attributePaths = "details")
    Optional<ProductOptionGroup> findWithDetailsById(Long id);
}
