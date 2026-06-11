package com.school.book.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 鍏ュ簱璁板綍琛ㄥ疄锟? */
@Entity
@Table(name = "inbound_records")
public class InboundRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "book_id", nullable = false)
    private Integer bookId;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "inbound_date")
    private LocalDateTime inboundDate;

    /** 鎿嶄綔锟?*/
    @Column(name = "operator_id")
    private Integer operatorId;

    /** 鏉ユ簮: PURCHASE-閲囪喘鍏ュ簱, RETURN-閫€鍥炲叆锟?*/
    @Column(length = 20)
    private String source = "PURCHASE";

    /** 鍏宠仈閲囪喘锟?*/
    @Column(name = "purchase_id")
    private Integer purchaseId;

    @Column(columnDefinition = "TEXT")
    private String remark;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (inboundDate == null) {
            inboundDate = LocalDateTime.now();
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

    public LocalDateTime getInboundDate() {
        return inboundDate;
    }
    public void setInboundDate(LocalDateTime inboundDate) {
        this.inboundDate = inboundDate;
    }

    public Integer getOperatorId() {
        return operatorId;
    }
    public void setOperatorId(Integer operatorId) {
        this.operatorId = operatorId;
    }

    public String getSource() {
        return source;
    }
    public void setSource(String source) {
        this.source = source;
    }

    public Integer getPurchaseId() {
        return purchaseId;
    }
    public void setPurchaseId(Integer purchaseId) {
        this.purchaseId = purchaseId;
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
