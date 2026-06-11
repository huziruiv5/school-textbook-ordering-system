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
 * 教材信息管理控制器
 */
@Controller
@RequestMapping("/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    private User checkLogin(HttpSession session) {
        return (User) session.getAttribute("currentUser");
    }

    /**
     * 教材列表
     */
    @GetMapping
    public String bookList(HttpSession session, Model model) {
        User currentUser = checkLogin(session);
        if (currentUser == null) return "redirect:/login";

        List<Book> books = bookService.getAllBooks();
        model.addAttribute("books", books);
        model.addAttribute("user", currentUser);
        return "book/list";
    }

    /**
     * 添加教材页面
     */
    @GetMapping("/add")
    public String addBookPage(HttpSession session, Model model) {
        User currentUser = checkLogin(session);
        if (currentUser == null) return "redirect:/login";

        model.addAttribute("book", new Book());
        model.addAttribute("user", currentUser);
        return "book/add";
    }

    /**
     * 添加教材
     */
    @PostMapping("/add")
    public String addBook(Book book, HttpSession session, RedirectAttributes attr) {
        User currentUser = checkLogin(session);
        if (currentUser == null) return "redirect:/login";

        try {
            bookService.createBook(book);
            attr.addFlashAttribute("success", "教材添加成功");
        } catch (Exception e) {
            attr.addFlashAttribute("error", "添加失败: " + e.getMessage());
        }

        return "redirect:/books";
    }

    /**
     * 编辑教材页面
     */
    @GetMapping("/edit/{id}")
    public String editBookPage(@PathVariable Integer id, HttpSession session, Model model) {
        User currentUser = checkLogin(session);
        if (currentUser == null) return "redirect:/login";

        Book book = bookService.getBookById(id);
        if (book == null) return "redirect:/books";

        model.addAttribute("book", book);
        model.addAttribute("user", currentUser);
        return "book/edit";
    }

    /**
     * 更新教材
     */
    @PostMapping("/edit/{id}")
    public String updateBook(@PathVariable Integer id, Book book,
                              HttpSession session, RedirectAttributes attr) {
        User currentUser = checkLogin(session);
        if (currentUser == null) return "redirect:/login";

        try {
            book.setId(id);
            bookService.updateBook(book);
            attr.addFlashAttribute("success", "教材更新成功");
        } catch (Exception e) {
            attr.addFlashAttribute("error", "更新失败: " + e.getMessage());
        }

        return "redirect:/books";
    }
}
