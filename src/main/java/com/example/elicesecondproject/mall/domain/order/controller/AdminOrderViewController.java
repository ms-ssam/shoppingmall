package com.example.elicesecondproject.mall.domain.order.controller;

import com.example.elicesecondproject.mall.domain.order.dto.request.AdminOrderSearchCondition;
import com.example.elicesecondproject.mall.domain.order.dto.response.OrderInfoResponse;
import com.example.elicesecondproject.mall.domain.order.entity.OrderStatus;
import com.example.elicesecondproject.mall.domain.order.service.AdminOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
        Page<OrderInfoResponse> responses = adminOrderService.searchOrders(searchCondition, pageable);

        model.addAttribute("orders", responses);
        model.addAttribute("searchCondition", searchCondition);

        return "admin/order/order-list";
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
