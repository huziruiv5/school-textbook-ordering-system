package com.school.book.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 缂轰功鐧昏琛ㄥ疄锟? */
@Entity
@Table(name = "out_of_stock_records")
public class OutOfStockRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "book_id", nullable = false)
    private Integer bookId;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "register_date")
    private LocalDateTime registerDate;

    /** 鐧昏锟?*/
    @Column(name = "register_id")
    private Integer registerId;

    /** 鐘讹拷? PENDING-寰呭锟? PURCHASED-宸查噰锟?*/
    @Column(nullable = false, length = 20)
    private String status = "PENDING";

    @Column(columnDefinition = "TEXT")
    private String remark;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (registerDate == null) {
            registerDate = LocalDateTime.now();
        }
    }








    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getBookId() {
        return bookId;
    }
    public void setBookId(Integer bookId) {
        this.bookId = bookId;
    }

    public Integer getQuantity() {
        return quantity;
    }
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public LocalDateTime getRegisterDate() {
        return registerDate;
    }
    public void setRegisterDate(LocalDateTime registerDate) {
        this.registerDate = registerDate;
    }

    public Integer getRegisterId() {
        return registerId;
    }
    public void setRegisterId(Integer registerId) {
        this.registerId = registerId;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    public String getRemark() {
        return remark;
    }
    public void setRemark(String remark) {
        this.remark = remark;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
