package com.school.book.controller;

import com.school.book.dto.OrderSubmitDTO;
import com.school.book.entity.*;
import com.school.book.service.*;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * 销售系统控制器
 * 功能：提交购书单、审核、开发票、领书
 */
@Controller
@RequestMapping("/sales")
public class SalesController {

    private final OrderService orderService;
    private final BookService bookService;
    private final UserService userService;

    public SalesController(OrderService orderService, BookService bookService, UserService userService) {
        this.orderService = orderService;
        this.bookService = bookService;
        this.userService = userService;
    }

    /**
     * 检查登录
     */
    private User checkLogin(HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        return user;
    }

    /**
     * 购书单列表（所有角色可见，按角色过滤）
     */
    @GetMapping("/orders")
    public String orderList(HttpSession session, Model model) {
        User currentUser = checkLogin(session);
        if (currentUser == null) return "redirect:/login";

        List<Order> orders;
        // 教师/学生只看到自己的购书单
        if ("TEACHER".equals(currentUser.getRole()) || "STUDENT".equals(currentUser.getRole())) {
            orders = orderService.getOrdersByUserId(currentUser.getId());
        } else {
            orders = orderService.getAllOrders();
        }

        model.addAttribute("orders", orders);
        model.addAttribute("user", currentUser);
        return "sales/order-list";
    }

    /**
     * 购书单详情
     */
    @GetMapping("/orders/{id}")
    public String orderDetail(@PathVariable Integer id, HttpSession session, Model model) {
        User currentUser = checkLogin(session);
        if (currentUser == null) return "redirect:/login";

        Order order = orderService.getOrderById(id);
        if (order == null) return "redirect:/sales/orders";

        List<OrderItem> items = orderService.getOrderItems(id);
        List<Invoice> invoices = orderService.getInvoicesByOrderId(id);
        User submitter = userService.getUserById(order.getUserId());

        model.addAttribute("order", order);
        model.addAttribute("items", items);
        model.addAttribute("invoices", invoices);
        model.addAttribute("submitter", submitter);
        model.addAttribute("user", currentUser);
        return "sales/order-detail";
    }

    /**
     * 提交购书单页面
     */
    @GetMapping("/orders/submit")
    public String submitOrderPage(HttpSession session, Model model) {
        User currentUser = checkLogin(session);
        if (currentUser == null) return "redirect:/login";

        if (!"TEACHER".equals(currentUser.getRole()) && !"STUDENT".equals(currentUser.getRole())) {
            return "redirect:/sales/orders";
        }

        List<Book> books = bookService.getAllBooks();
        model.addAttribute("books", books);
        model.addAttribute("user", currentUser);
        model.addAttribute("orderSubmit", new OrderSubmitDTO());
        return "sales/order-submit";
    }

    /**
     * 提交购书单
     */
    @PostMapping("/orders/submit")
    public String submitOrder(@Valid OrderSubmitDTO orderSubmit,
                              BindingResult result,
                              HttpSession session,
                              RedirectAttributes attr) {
        User currentUser = checkLogin(session);
        if (currentUser == null) return "redirect:/login";

        if (result.hasErrors()) {
            attr.addFlashAttribute("error", "请正确填写购书信息");
            return "redirect:/sales/orders/submit";
        }

        try {
            orderSubmit.setUserId(currentUser.getId());
            Order order = orderService.submitOrder(orderSubmit);
            attr.addFlashAttribute("success", "购书单提交成功！单号: " + order.getOrderNo());
        } catch (Exception e) {
            attr.addFlashAttribute("error", "提交失败: " + e.getMessage());
        }

        return "redirect:/sales/orders";
    }

    /**
     * 审核购书单页面（教材发行人员）
     */
    @GetMapping("/orders/{id}/approve")
    public String approvePage(@PathVariable Integer id, HttpSession session, Model model) {
        User currentUser = checkLogin(session);
        if (currentUser == null) return "redirect:/login";

        if (!"DISTRIBUTOR".equals(currentUser.getRole())) {
            return "redirect:/sales/orders";
        }

        Order order = orderService.getOrderById(id);
        if (order == null) return "redirect:/sales/orders";

        List<OrderItem> items = orderService.getOrderItems(id);
        User submitter = userService.getUserById(order.getUserId());

        model.addAttribute("order", order);
        model.addAttribute("items", items);
        model.addAttribute("submitter", submitter);
        model.addAttribute("user", currentUser);
        return "sales/order-approve";
    }

    /**
     * 执行审核
     */
    @PostMapping("/orders/{id}/approve")
    public String doApprove(@PathVariable Integer id,
                            @RequestParam boolean approved,
                            @RequestParam(required = false) String remark,
                            HttpSession session,
                            RedirectAttributes attr) {
        User currentUser = checkLogin(session);
        if (currentUser == null) return "redirect:/login";

        try {
            orderService.approveOrder(id, currentUser.getId(), approved, remark);
            if (approved) {
                attr.addFlashAttribute("success", "购书单已通过审核");
            } else {
                attr.addFlashAttribute("success", "购书单已驳回");
            }
        } catch (Exception e) {
            attr.addFlashAttribute("error", "审核失败: " + e.getMessage());
        }

        return "redirect:/sales/orders";
    }

    /**
     * 开发票（教材发行人员）
     */
    @PostMapping("/orders/{id}/invoice")
    public String createInvoice(@PathVariable Integer id,
                                HttpSession session,
                                RedirectAttributes attr) {
        User currentUser = checkLogin(session);
        if (currentUser == null) return "redirect:/login";

        try {
            Invoice invoice = orderService.createInvoice(id, currentUser.getId());
            attr.addFlashAttribute("success", "发票开具成功！发票号: " + invoice.getInvoiceNo());
        } catch (Exception e) {
            attr.addFlashAttribute("error", "开票失败: " + e.getMessage());
        }

        return "redirect:/sales/orders/" + id;
    }

    /**
     * 领取教材/出库（书库操作人员）
     */
    @PostMapping("/orders/{id}/fulfill")
    public String fulfillOrder(@PathVariable Integer id,
                               HttpSession session,
                               RedirectAttributes attr) {
        User currentUser = checkLogin(session);
        if (currentUser == null) return "redirect:/login";

        try {
            orderService.fulfillOrder(id, currentUser.getId());
            attr.addFlashAttribute("success", "教材领取成功！");
        } catch (Exception e) {
            attr.addFlashAttribute("error", "领书失败: " + e.getMessage());
        }

        return "redirect:/sales/orders/" + id;
    }

    /**
     * 发票列表
     */
    @GetMapping("/invoices")
    public String invoiceList(HttpSession session, Model model) {
        User currentUser = checkLogin(session);
        if (currentUser == null) return "redirect:/login";

        List<Invoice> invoices = orderService.getAllInvoices();
        model.addAttribute("invoices", invoices);
        model.addAttribute("user", currentUser);
        return "sales/invoice-list";
    }
}
