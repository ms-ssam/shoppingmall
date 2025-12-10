package com.example.elicesecondproject.mall.domain.order.repository;

import com.example.elicesecondproject.mall.domain.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order,Long>, OrderRepositoryCustom{
    Optional<Order> findByIdAndMemberId(Long orderId, Long memberId);

    List<Order> findTop5ByMemberIdOrderByIdDesc(Long memberId);
}
