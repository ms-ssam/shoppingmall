package com.example.elicesecondproject.mall.domain.order.controller;

import com.example.elicesecondproject.mall.domain.order.dto.request.AdminOrderSearchCondition;
import com.example.elicesecondproject.mall.domain.order.dto.response.AdminOrderInfoResponse;
import com.example.elicesecondproject.mall.domain.order.dto.response.AdminOrderDetailResponse;
import com.example.elicesecondproject.mall.domain.order.entity.OrderStatus;
import com.example.elicesecondproject.mall.domain.order.service.AdminOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
public class AdminOrderViewController {
    private final AdminOrderService adminOrderService;

    @GetMapping
    public String getOrders(@ModelAttribute AdminOrderSearchCondition searchCondition,
                            Pageable pageable,
                            Model model
    ) {
        Page<AdminOrderInfoResponse> responses = adminOrderService.searchOrders(searchCondition, pageable);

        model.addAttribute("orders", responses);
        model.addAttribute("searchCondition", searchCondition);

        return "admin/order/order-list";
    }

    @GetMapping("/{orderId}")
    public String getOrderDetail(@PathVariable Long orderId, Model model) {
        AdminOrderDetailResponse orderDetail = adminOrderService.getOrderDetail(orderId);

        model.addAttribute(("orderDetail"), orderDetail);

        // 사이드바 활성화 & 상단 타이틀용
        model.addAttribute("menu", "order");
        model.addAttribute("pageTitle", "주문 상세");

        return "admin/order/order-detail";
    }

    @PutMapping("/{orderId}")
    public String updateOrderStatus(@PathVariable Long orderId,
                                    @RequestParam OrderStatus status
    ) {
        adminOrderService.updateOrderStatus(orderId, status);
        return "redirect:/admin/orders";
    }

    @PutMapping("/selected")
    public String updateSelectedOrdersStatus(@RequestParam List<Long> orderIds,
                                             @RequestParam OrderStatus status
    ) {
        adminOrderService.updateOrdersStatus(orderIds, status);
        return "redirect:/admin/orders";
    }
}
