package com.school.book.service;

import com.school.book.entity.*;
import com.school.book.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 采购系统核心服务
 *
 * 工作流程:
 * 1. 教材发行人员登记缺书
 * 2. 发缺书单给采购人员
 * 3. 采购人员采购教材
 * 4. 新书入库后发进书通知给教材发行人员
 */
@Service
public class PurchaseService {

    private final OutOfStockRecordRepository outOfStockRecordRepository;
    private final PendingPurchaseRepository pendingPurchaseRepository;
    private final InventoryService inventoryService;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final InboundRecordRepository inboundRecordRepository;

    public PurchaseService(OutOfStockRecordRepository outOfStockRecordRepository,
                          PendingPurchaseRepository pendingPurchaseRepository,
                          InventoryService inventoryService,
                          BookRepository bookRepository,
                          UserRepository userRepository,
                          InboundRecordRepository inboundRecordRepository) {
        this.outOfStockRecordRepository = outOfStockRecordRepository;
        this.pendingPurchaseRepository = pendingPurchaseRepository;
        this.inventoryService = inventoryService;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
        this.inboundRecordRepository = inboundRecordRepository;
    }

    /**
     * 登记缺书（教材发行人员操作）
     * 当发现教材脱销时，登记缺书记录
     */
    @Transactional
    public OutOfStockRecord registerOutOfStock(Integer bookId, Integer quantity,
                                                Integer registerId, String remark) {
        // 验证教材
        if (!bookRepository.existsById(bookId)) {
            throw new RuntimeException("教材不存在");
        }

        // 验证登记人角色
        User register = userRepository.findById(registerId)
                .orElseThrow(() -> new RuntimeException("登记人不存在"));
        if (!"DISTRIBUTOR".equals(register.getRole())) {
            throw new RuntimeException("仅教材发行人员可以登记缺书");
        }

        OutOfStockRecord record = new OutOfStockRecord();
        record.setBookId(bookId);
        record.setQuantity(quantity);
        record.setRegisterDate(LocalDateTime.now());
        record.setRegisterId(registerId);
        record.setStatus("PENDING");
        record.setRemark(remark);

        return outOfStockRecordRepository.save(record);
    }

    /**
     * 根据缺书记录生成采购单（自动或手动）
     * 发送缺书单给采购人员
     */
    @Transactional
    public PendingPurchase createPurchaseFromOutOfStock(Integer outOfStockId, Integer purchaserId) {
        OutOfStockRecord outOfStock = outOfStockRecordRepository.findById(outOfStockId)
                .orElseThrow(() -> new RuntimeException("缺书记录不存在"));

        if (!"PENDING".equals(outOfStock.getStatus())) {
            throw new RuntimeException("该缺书记录已处理");
        }

        // 验证采购人角色
        User purchaser = userRepository.findById(purchaserId)
                .orElseThrow(() -> new RuntimeException("采购人不存在"));
        if (!"PURCHASER".equals(purchaser.getRole())) {
            throw new RuntimeException("仅采购人员可以创建采购单");
        }

        PendingPurchase purchase = new PendingPurchase();
        purchase.setBookId(outOfStock.getBookId());
        purchase.setQuantity(outOfStock.getQuantity());
        purchase.setOutOfStockId(outOfStockId);
        purchase.setPurchaseDate(LocalDateTime.now());
        purchase.setPurchaserId(purchaserId);
        purchase.setStatus("PENDING");
        purchase.setRemark("来源于缺书登记 #" + outOfStockId);

        // 更新缺书记录状态
        outOfStock.setStatus("PURCHASED");
        outOfStockRecordRepository.save(outOfStock);

        return pendingPurchaseRepository.save(purchase);
    }

    /**
     * 直接创建采购单（采购人员操作）
     */
    @Transactional
    public PendingPurchase createPurchaseDirectly(Integer bookId, Integer quantity,
                                                   Integer purchaserId, String remark) {
        if (!bookRepository.existsById(bookId)) {
            throw new RuntimeException("教材不存在");
        }

        User purchaser = userRepository.findById(purchaserId)
                .orElseThrow(() -> new RuntimeException("采购人不存在"));
        if (!"PURCHASER".equals(purchaser.getRole())) {
            throw new RuntimeException("仅采购人员可以创建采购单");
        }

        PendingPurchase purchase = new PendingPurchase();
        purchase.setBookId(bookId);
        purchase.setQuantity(quantity);
        purchase.setPurchaseDate(LocalDateTime.now());
        purchase.setPurchaserId(purchaserId);
        purchase.setStatus("PENDING");
        purchase.setRemark(remark);

        return pendingPurchaseRepository.save(purchase);
    }

    /**
     * 采购到货 - 入库操作
     * 新书入库后发进书通知给教材发行人员
     */
    @Transactional
    public InboundRecord receivePurchase(Integer purchaseId, Integer operatorId) {
        PendingPurchase purchase = pendingPurchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new RuntimeException("采购单不存在"));

        if (!"PENDING".equals(purchase.getStatus())) {
            throw new RuntimeException("该采购单已处理");
        }

        // 执行入库
        InboundRecord inboundRecord = new InboundRecord();
        inboundRecord.setBookId(purchase.getBookId());
        inboundRecord.setQuantity(purchase.getQuantity());
        inboundRecord.setOperatorId(operatorId);
        inboundRecord.setSource("PURCHASE");
        inboundRecord.setPurchaseId(purchaseId);
        inboundRecord.setRemark("采购到货入库，关联采购单 #" + purchaseId);

        InboundRecord saved = inventoryService.inbound(inboundRecord);

        // 更新采购单状态
        purchase.setStatus("RECEIVED");
        pendingPurchaseRepository.save(purchase);

        return saved;
    }

    // ========== 查询方法 ==========

    /**
     * 获取所有缺书记录
     */
    public List<OutOfStockRecord> getAllOutOfStockRecords() {
        return outOfStockRecordRepository.findAllOrderByCreatedAtDesc();
    }

    /**
     * 获取待处理的缺书记录
     */
    public List<OutOfStockRecord> getPendingOutOfStockRecords() {
        return outOfStockRecordRepository.findByStatus("PENDING");
    }

    /**
     * 获取所有采购单
     */
    public List<PendingPurchase> getAllPendingPurchases() {
        return pendingPurchaseRepository.findAllOrderByCreatedAtDesc();
    }

    /**
     * 获取待处理的采购单
     */
    public List<PendingPurchase> getPendingPurchases() {
        return pendingPurchaseRepository.findByStatus("PENDING");
    }

    /**
     * 根据ID获取缺书记录
     */
    public OutOfStockRecord getOutOfStockById(Integer id) {
        return outOfStockRecordRepository.findById(id).orElse(null);
    }

    /**
     * 根据ID获取采购单
     */
    public PendingPurchase getPendingPurchaseById(Integer id) {
        return pendingPurchaseRepository.findById(id).orElse(null);
    }
}
