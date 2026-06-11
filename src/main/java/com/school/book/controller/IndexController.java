package com.school.book.controller;

import com.school.book.entity.User;
import com.school.book.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 首页和仪表盘控制器
 */
@Controller
public class IndexController {

    private final OrderService orderService;
    private final InventoryService inventoryService;
    private final PurchaseService purchaseService;

    public IndexController(OrderService orderService,
                          InventoryService inventoryService,
                          PurchaseService purchaseService) {
        this.orderService = orderService;
        this.inventoryService = inventoryService;
        this.purchaseService = purchaseService;
    }

    @GetMapping({"/", "/dashboard"})
    public String dashboard(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }
        model.addAttribute("user", currentUser);

        // 统计数据
        try {
            long orderCount = orderService.getAllOrders().size();
            long inStockCount = inventoryService.getInStock().size();
            long outOfStockCount = purchaseService.getPendingOutOfStockRecords().size();
            long pendingPurchaseCount = purchaseService.getPendingPurchases().size();

            model.addAttribute("orderCount", orderCount);
            model.addAttribute("inStockCount", inStockCount);
            model.addAttribute("outOfStockCount", outOfStockCount);
            model.addAttribute("pendingPurchaseCount", pendingPurchaseCount);
        } catch (Exception e) {
            // 忽略初始化时的错误
        }

        return "dashboard";
    }
}
