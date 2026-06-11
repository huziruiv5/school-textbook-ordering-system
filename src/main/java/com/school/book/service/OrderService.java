package com.school.book.service;

import com.school.book.dto.OrderSubmitDTO;
import com.school.book.entity.*;
import com.school.book.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

/**
 * 销售系统核心服务 - 购书单管理
 *
 * 工作流程:
 * 1. 教师/学生提交购书单
 * 2. 教材发行人员审核购书单
 * 3. 审核通过后开发票
 * 4. 教师/学生凭领书单去书库领书
 */
@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final BookRepository bookRepository;
    private final InventoryService inventoryService;
    private final UserRepository userRepository;
    private final InvoiceRepository invoiceRepository;

    public OrderService(OrderRepository orderRepository,
                       OrderItemRepository orderItemRepository,
                       BookRepository bookRepository,
                       InventoryService inventoryService,
                       UserRepository userRepository,
                       InvoiceRepository invoiceRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.bookRepository = bookRepository;
        this.inventoryService = inventoryService;
        this.userRepository = userRepository;
        this.invoiceRepository = invoiceRepository;
    }

    /**
     * 提交购书单
     * 教师/学生提交购书请求，状态为待审核
     */
    @Transactional
    public Order submitOrder(OrderSubmitDTO dto) {
        // 验证提交人
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 创建购书单
        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(dto.getUserId());
        order.setStatus("PENDING");
        order.setSubmitDate(LocalDateTime.now());
        order.setRemark(dto.getRemark());

        // 计算总金额
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (OrderSubmitDTO.OrderItemDTO itemDTO : dto.getItems()) {
            Book book = bookRepository.findById(itemDTO.getBookId())
                    .orElseThrow(() -> new RuntimeException("教材不存在: " + itemDTO.getBookId()));

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setBookId(itemDTO.getBookId());
            orderItem.setQuantity(itemDTO.getQuantity());
            orderItem.setPrice(book.getPrice());

            BigDecimal subtotal = book.getPrice().multiply(BigDecimal.valueOf(itemDTO.getQuantity()));
            orderItem.setSubtotal(subtotal);
            totalAmount = totalAmount.add(subtotal);

            order.getOrderItems().add(orderItem);
        }

        order.setTotalAmount(totalAmount);
        return orderRepository.save(order);
    }

    /**
     * 审核购书单（教材发行人员操作）
     */
    @Transactional
    public Order approveOrder(Integer orderId, Integer approverId, boolean approved, String remark) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("购书单不存在"));

        if (!"PENDING".equals(order.getStatus())) {
            throw new RuntimeException("购书单已审核，不能重复操作");
        }

        // 验证审核人角色
        User approver = userRepository.findById(approverId)
                .orElseThrow(() -> new RuntimeException("审核人不存在"));
        if (!"DISTRIBUTOR".equals(approver.getRole())) {
            throw new RuntimeException("仅教材发行人员可以审核购书单");
        }

        order.setApproverId(approverId);
        order.setApproveDate(LocalDateTime.now());
        order.setRemark(remark);

        if (approved) {
            order.setStatus("APPROVED");
        } else {
            order.setStatus("REJECTED");
        }

        return orderRepository.save(order);
    }

    /**
     * 开发票（教材发行人员操作）
     */
    @Transactional
    public Invoice createInvoice(Integer orderId, Integer issuerId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("购书单不存在"));

        if (!"APPROVED".equals(order.getStatus())) {
            throw new RuntimeException("仅已通过的购书单可以开发票");
        }

        // 验证开票人角色
        User issuer = userRepository.findById(issuerId)
                .orElseThrow(() -> new RuntimeException("开票人不存在"));
        if (!"DISTRIBUTOR".equals(issuer.getRole())) {
            throw new RuntimeException("仅教材发行人员可以开发票");
        }

        // 检查是否已开发票
        List<Invoice> existingInvoices = invoiceRepository.findByOrderId(orderId);
        if (!existingInvoices.isEmpty()) {
            throw new RuntimeException("该购书单已开发票");
        }

        Invoice invoice = new Invoice();
        invoice.setInvoiceNo("INV-" + order.getOrderNo());
        invoice.setOrderId(orderId);
        invoice.setAmount(order.getTotalAmount());
        invoice.setIssueDate(LocalDateTime.now());
        invoice.setIssuerId(issuerId);

        return invoiceRepository.save(invoice);
    }

    /**
     * 领取教材（出库操作）
     * 教师/学生凭领书单去书库领书
     */
    @Transactional
    public void fulfillOrder(Integer orderId, Integer operatorId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("购书单不存在"));

        if (!"APPROVED".equals(order.getStatus())) {
            throw new RuntimeException("仅已通过的购书单可以领书");
        }

        // 验证发票已开具
        List<Invoice> invoices = invoiceRepository.findByOrderId(orderId);
        if (invoices.isEmpty()) {
            throw new RuntimeException("尚未开发票，请先开具发票");
        }

        // 逐项出库
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        for (OrderItem item : items) {
            OutboundRecord outboundRecord = new OutboundRecord();
            outboundRecord.setBookId(item.getBookId());
            outboundRecord.setQuantity(item.getQuantity());
            outboundRecord.setOperatorId(operatorId);
            outboundRecord.setOrderItemId(item.getId());
            outboundRecord.setOrderNo(order.getOrderNo());
            outboundRecord.setRemark("购书单领书: " + order.getOrderNo());

            inventoryService.outbound(outboundRecord);
        }

        // 更新订单状态
        order.setStatus("FULFILLED");
        orderRepository.save(order);
    }

    /**
     * 获取所有购书单
     */
    public List<Order> getAllOrders() {
        return orderRepository.findAllOrderByCreatedAtDesc();
    }

    /**
     * 根据ID获取购书单
     */
    public Order getOrderById(Integer id) {
        return orderRepository.findById(id).orElse(null);
    }

    /**
     * 根据订单号获取购书单
     */
    public Order getOrderByOrderNo(String orderNo) {
        return orderRepository.findByOrderNo(orderNo).orElse(null);
    }

    /**
     * 根据用户获取购书单
     */
    public List<Order> getOrdersByUserId(Integer userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * 根据状态获取购书单
     */
    public List<Order> getOrdersByStatus(String status) {
        return orderRepository.findByStatus(status);
    }

    /**
     * 获取购书单明细
     */
    public List<OrderItem> getOrderItems(Integer orderId) {
        return orderItemRepository.findByOrderId(orderId);
    }

    /**
     * 获取发票信息
     */
    public List<Invoice> getInvoicesByOrderId(Integer orderId) {
        return invoiceRepository.findByOrderId(orderId);
    }

    /**
     * 获取所有发票
     */
    public List<Invoice> getAllInvoices() {
        return invoiceRepository.findAll();
    }

    /**
     * 生成订单号
     */
    private String generateOrderNo() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int randomPart = new Random().nextInt(1000);
        return "ORD-" + datePart + "-" + String.format("%03d", randomPart);
    }
}
