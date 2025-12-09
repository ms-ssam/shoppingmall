package com.example.elicesecondproject.mall.domain.order.repository;

import com.example.elicesecondproject.mall.domain.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order,Long>, OrderRepositoryCustom{
}
