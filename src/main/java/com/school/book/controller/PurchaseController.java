package com.school.book.controller;

import com.school.book.entity.*;
import com.school.book.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * 采购系统控制器
 * 功能：缺书登记、采购管理、入库
 */
@Controller
@RequestMapping("/purchase")
public class PurchaseController {

    private final PurchaseService purchaseService;
    private final BookService bookService;
    private final InventoryService inventoryService;
    private final UserService userService;

    public PurchaseController(PurchaseService purchaseService, BookService bookService,
                              InventoryService inventoryService, UserService userService) {
        this.purchaseService = purchaseService;
        this.bookService = bookService;
        this.inventoryService = inventoryService;
        this.userService = userService;
    }

    private User checkLogin(HttpSession session) {
        return (User) session.getAttribute("currentUser");
    }

    /**
     * 缺书记录列表
     */
    @GetMapping("/out-of-stock")
    public String outOfStockList(HttpSession session, Model model) {
        User currentUser = checkLogin(session);
        if (currentUser == null) return "redirect:/login";

        List<OutOfStockRecord> records = purchaseService.getAllOutOfStockRecords();
        model.addAttribute("records", records);
        model.addAttribute("user", currentUser);
        return "purchase/out-of-stock-list";
    }

    /**
     * 登记缺书页面（教材发行人员）
     */
    @GetMapping("/out-of-stock/register")
    public String registerOutOfStockPage(HttpSession session, Model model) {
        User currentUser = checkLogin(session);
        if (currentUser == null) return "redirect:/login";

        if (!"DISTRIBUTOR".equals(currentUser.getRole()) && !"PURCHASER".equals(currentUser.getRole())) {
            return "redirect:/purchase/out-of-stock";
        }

        List<Book> books = bookService.getAllBooks();
        List<Inventory> outOfStock = inventoryService.getOutOfStock();

        model.addAttribute("books", books);
        model.addAttribute("outOfStockBooks", outOfStock);
        model.addAttribute("user", currentUser);
        return "purchase/register-out-of-stock";
    }

    /**
     * 执行缺书登记
     */
    @PostMapping("/out-of-stock/register")
    public String doRegisterOutOfStock(@RequestParam Integer bookId,
                                       @RequestParam Integer quantity,
                                       @RequestParam(required = false) String remark,
                                       HttpSession session,
                                       RedirectAttributes attr) {
        User currentUser = checkLogin(session);
        if (currentUser == null) return "redirect:/login";

        try {
            purchaseService.registerOutOfStock(bookId, quantity, currentUser.getId(), remark);
            attr.addFlashAttribute("success", "缺书登记成功");
        } catch (Exception e) {
            attr.addFlashAttribute("error", "登记失败: " + e.getMessage());
        }

        return "redirect:/purchase/out-of-stock";
    }

    /**
     * 采购单列表
     */
    @GetMapping("/purchases")
    public String purchaseList(HttpSession session, Model model) {
        User currentUser = checkLogin(session);
        if (currentUser == null) return "redirect:/login";

        List<PendingPurchase> purchases = purchaseService.getAllPendingPurchases();
        model.addAttribute("purchases", purchases);
        model.addAttribute("user", currentUser);
        return "purchase/purchase-list";
    }

    /**
     * 从缺书记录创建采购单
     */
    @PostMapping("/purchases/create/{outOfStockId}")
    public String createPurchaseFromOutOfStock(@PathVariable Integer outOfStockId,
                                                HttpSession session,
                                                RedirectAttributes attr) {
        User currentUser = checkLogin(session);
        if (currentUser == null) return "redirect:/login";

        try {
            PendingPurchase purchase = purchaseService.createPurchaseFromOutOfStock(outOfStockId, currentUser.getId());
            attr.addFlashAttribute("success", "采购单已创建，采购单ID: " + purchase.getId());
        } catch (Exception e) {
            attr.addFlashAttribute("error", "创建采购单失败: " + e.getMessage());
        }

        return "redirect:/purchase/purchases";
    }

    /**
     * 直接创建采购单页面
     */
    @GetMapping("/purchases/create")
    public String createPurchasePage(HttpSession session, Model model) {
        User currentUser = checkLogin(session);
        if (currentUser == null) return "redirect:/login";

        List<Book> books = bookService.getAllBooks();
        List<OutOfStockRecord> pendingRecords = purchaseService.getPendingOutOfStockRecords();

        model.addAttribute("books", books);
        model.addAttribute("pendingRecords", pendingRecords);
        model.addAttribute("user", currentUser);
        return "purchase/create-purchase";
    }

    /**
     * 执行直接采购
     */
    @PostMapping("/purchases/create")
    public String doCreatePurchase(@RequestParam Integer bookId,
                                   @RequestParam Integer quantity,
                                   @RequestParam(required = false) String remark,
                                   HttpSession session,
                                   RedirectAttributes attr) {
        User currentUser = checkLogin(session);
        if (currentUser == null) return "redirect:/login";

        try {
            purchaseService.createPurchaseDirectly(bookId, quantity, currentUser.getId(), remark);
            attr.addFlashAttribute("success", "采购单已创建");
        } catch (Exception e) {
            attr.addFlashAttribute("error", "创建采购单失败: " + e.getMessage());
        }

        return "redirect:/purchase/purchases";
    }

    /**
     * 采购到货入库
     */
    @PostMapping("/purchases/{id}/receive")
    public String receivePurchase(@PathVariable Integer id,
                                   HttpSession session,
                                   RedirectAttributes attr) {
        User currentUser = checkLogin(session);
        if (currentUser == null) return "redirect:/login";

        try {
            InboundRecord record = purchaseService.receivePurchase(id, currentUser.getId());
            attr.addFlashAttribute("success", "采购到货入库成功！入库数量: " + record.getQuantity());
        } catch (Exception e) {
            attr.addFlashAttribute("error", "入库失败: " + e.getMessage());
        }

        return "redirect:/purchase/purchases";
    }
}
