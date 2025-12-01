package com.example.elicesecondproject.mall.domain.product.repository;

import com.example.elicesecondproject.mall.domain.product.entity.Product;
import com.example.elicesecondproject.mall.domain.product.entity.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // 삭제 안된거 조회
    Page<Product> findByDeletedAtIsNull(Pageable pageable);

    // 유저는 selling만 조회
    Page<Product> findByStatus(ProductStatus status, Pageable pageable);

}
