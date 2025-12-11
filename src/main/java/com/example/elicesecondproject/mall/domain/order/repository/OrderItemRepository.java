package com.example.elicesecondproject.mall.domain.order.repository;

import com.example.elicesecondproject.mall.domain.order.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    @Query("""
        select oi from OrderItem oi
        join fetch oi.order o
        where oi.id = :orderItemId
    """)
    Optional<OrderItem> findWithOrder(Long orderItemId);
}
