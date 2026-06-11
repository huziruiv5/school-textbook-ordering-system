package com.school.book.service;

import com.school.book.entity.Book;
import com.school.book.repository.BookRepository;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * 教材信息服务
 */
@Service
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    /**
     * 获取所有教材
     */
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    /**
     * 根据ID获取教材
     */
    public Book getBookById(Integer id) {
        return bookRepository.findById(id).orElse(null);
    }

    /**
     * 根据ISBN获取教材
     */
    public Book getBookByIsbn(String isbn) {
        return bookRepository.findByIsbn(isbn).orElse(null);
    }

    /**
     * 搜索教材（按名称模糊匹配）
     */
    public List<Book> searchBooks(String keyword) {
        return bookRepository.findByNameContaining(keyword);
    }

    /**
     * 添加教材
     */
    public Book createBook(Book book) {
        if (bookRepository.existsByIsbn(book.getIsbn())) {
            throw new RuntimeException("该ISBN已存在");
        }
        return bookRepository.save(book);
    }

    /**
     * 更新教材
     */
    public Book updateBook(Book book) {
        return bookRepository.save(book);
    }
}
