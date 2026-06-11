package com.school.book.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 寰呰喘鏁欐潗琛ㄥ疄锟? */
@Entity
@Table(name = "pending_purchases")
public class PendingPurchase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "book_id", nullable = false)
    private Integer bookId;

    @Column(nullable = false)
    private Integer quantity;

    /** 瀵瑰簲缂轰功鐧昏ID */
    @Column(name = "out_of_stock_id")
    private Integer outOfStockId;

    @Column(name = "purchase_date")
    private LocalDateTime purchaseDate;

    /** 閲囪喘锟?*/
    @Column(name = "purchaser_id")
    private Integer purchaserId;

    /** 鐘讹拷? PENDING-寰呰锟? ORDERED-宸茶锟? RECEIVED-宸插埌锟?*/
    @Column(nullable = false, length = 20)
    private String status = "PENDING";

    @Column(columnDefinition = "TEXT")
    private String remark;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (purchaseDate == null) {
            purchaseDate = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
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

    public Integer getOutOfStockId() {
        return outOfStockId;
    }
    public void setOutOfStockId(Integer outOfStockId) {
        this.outOfStockId = outOfStockId;
    }

    public LocalDateTime getPurchaseDate() {
        return purchaseDate;
    }
    public void setPurchaseDate(LocalDateTime purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public Integer getPurchaserId() {
        return purchaserId;
    }
    public void setPurchaserId(Integer purchaserId) {
        this.purchaserId = purchaserId;
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

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
