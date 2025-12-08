package com.example.elicesecondproject.mall.domain.option.repository;

import com.example.elicesecondproject.mall.domain.option.entity.OptionDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OptionDetailRepository extends JpaRepository<OptionDetail, Long> {

    // SKU 중복 체크
    boolean existsBySku(String sku);

    boolean existsBySkuAndIdNot(String sku, Long id);

}
