package com.school.book.controller;

import com.school.book.entity.*;
import com.school.book.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 库存管理控制器
 */
@Controller
@RequestMapping("/inventory")
public class InventoryController {

    private final InventoryService inventoryService;
    private final BookService bookService;

    public InventoryController(InventoryService inventoryService, BookService bookService) {
        this.inventoryService = inventoryService;
        this.bookService = bookService;
    }

    private User checkLogin(HttpSession session) {
        return (User) session.getAttribute("currentUser");
    }

    /**
     * 库存列表
     */
    @GetMapping
    public String inventoryList(HttpSession session, Model model) {
        User currentUser = checkLogin(session);
        if (currentUser == null) return "redirect:/login";

        List<Inventory> inventories = inventoryService.getAllInventory();
        // 关联教材名称
        List<InventoryWithBook> inventoryWithBooks = inventories.stream()
                .map(inv -> {
                    Book book = bookService.getBookById(inv.getBookId());
                    return new InventoryWithBook(inv, book);
                })
                .collect(Collectors.toList());

        model.addAttribute("inventories", inventoryWithBooks);
        model.addAttribute("user", currentUser);
        return "inventory/list";
    }

    /**
     * 入库记录
     */
    @GetMapping("/inbound")
    public String inboundRecords(HttpSession session, Model model) {
        User currentUser = checkLogin(session);
        if (currentUser == null) return "redirect:/login";

        List<InboundRecord> records = inventoryService.getAllInboundRecords();
        List<InboundRecordWithBook> recordsWithBooks = records.stream()
                .map(r -> {
                    Book book = bookService.getBookById(r.getBookId());
                    return new InboundRecordWithBook(r, book);
                })
                .collect(Collectors.toList());

        model.addAttribute("records", recordsWithBooks);
        model.addAttribute("user", currentUser);
        return "inventory/inbound";
    }

    /**
     * 出库记录
     */
    @GetMapping("/outbound")
    public String outboundRecords(HttpSession session, Model model) {
        User currentUser = checkLogin(session);
        if (currentUser == null) return "redirect:/login";

        List<OutboundRecord> records = inventoryService.getAllOutboundRecords();
        List<OutboundRecordWithBook> recordsWithBooks = records.stream()
                .map(r -> {
                    Book book = bookService.getBookById(r.getBookId());
                    return new OutboundRecordWithBook(r, book);
                })
                .collect(Collectors.toList());

        model.addAttribute("records", recordsWithBooks);
        model.addAttribute("user", currentUser);
        return "inventory/outbound";
    }

    /**
     * 内部类：库存+教材信息
     */
    public record InventoryWithBook(Inventory inventory, Book book) {}
    public record InboundRecordWithBook(InboundRecord record, Book book) {}
    public record OutboundRecordWithBook(OutboundRecord record, Book book) {}
}
