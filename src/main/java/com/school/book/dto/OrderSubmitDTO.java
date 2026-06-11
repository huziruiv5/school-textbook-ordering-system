package com.school.book.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;

/**
 * 提交购书订单 DTO
 */
public class OrderSubmitDTO {
    /** 提交人ID */
    @NotNull(message = "提交人不能为空")
    private Integer userId;

    /** 备注 */
    private String remark;

    /** 购书明细 */
    @NotEmpty(message = "购书明细不能为空")
    @Valid
    private List<OrderItemDTO> items;

    public static class OrderItemDTO {
        @NotNull(message = "教材ID不能为空")
        private Integer bookId;

        @NotNull(message = "数量不能为空")
        @Positive(message = "数量必须大于0")
        private Integer quantity;

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
    }

    public Integer getUserId() {
        return userId;
    }
    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getRemark() {
        return remark;
    }
    public void setRemark(String remark) {
        this.remark = remark;
    }

    public List<OrderItemDTO> getItems() {
        return items;
    }
    public void setItems(List<OrderItemDTO> items) {
        this.items = items;
    }
}
