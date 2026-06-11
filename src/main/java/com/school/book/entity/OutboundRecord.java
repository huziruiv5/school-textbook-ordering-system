package com.school.book.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 鍑哄簱璁板綍琛ㄥ疄锟? */
@Entity
@Table(name = "outbound_records")
public class OutboundRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "book_id", nullable = false)
    private Integer bookId;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "outbound_date")
    private LocalDateTime outboundDate;

    /** 鎿嶄綔锟?*/
    @Column(name = "operator_id")
    private Integer operatorId;

    /** 鍏宠仈璐功鍗曟槑锟?*/
    @Column(name = "order_item_id")
    private Integer orderItemId;

    /** 鍏宠仈璐功鍗曞彿 */
    @Column(name = "order_no", length = 50)
    private String orderNo;

    @Column(columnDefinition = "TEXT")
    private String remark;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (outboundDate == null) {
            outboundDate = LocalDateTime.now();
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

    public LocalDateTime getOutboundDate() {
        return outboundDate;
    }
    public void setOutboundDate(LocalDateTime outboundDate) {
        this.outboundDate = outboundDate;
    }

    public Integer getOperatorId() {
        return operatorId;
    }
    public void setOperatorId(Integer operatorId) {
        this.operatorId = operatorId;
    }

    public Integer getOrderItemId() {
        return orderItemId;
    }
    public void setOrderItemId(Integer orderItemId) {
        this.orderItemId = orderItemId;
    }

    public String getOrderNo() {
        return orderNo;
    }
    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
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
