package com.school.book.service;

import com.school.book.entity.*;
import com.school.book.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 库存管理服务
 */
@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InboundRecordRepository inboundRecordRepository;
    private final OutboundRecordRepository outboundRecordRepository;
    private final BookRepository bookRepository;

    public InventoryService(InventoryRepository inventoryRepository,
                           InboundRecordRepository inboundRecordRepository,
                           OutboundRecordRepository outboundRecordRepository,
                           BookRepository bookRepository) {
        this.inventoryRepository = inventoryRepository;
        this.inboundRecordRepository = inboundRecordRepository;
        this.outboundRecordRepository = outboundRecordRepository;
        this.bookRepository = bookRepository;
    }

    /**
     * 获取所有库存信息（带教材名称）
     */
    public List<Inventory> getAllInventory() {
        return inventoryRepository.findAll();
    }

    /**
     * 根据教材ID获取库存
     */
    public Inventory getInventoryByBookId(Integer bookId) {
        return inventoryRepository.findByBookId(bookId).orElse(null);
    }

    /**
     * 获取有库存的教材
     */
    public List<Inventory> getInStock() {
        return inventoryRepository.findAllWithStock();
    }

    /**
     * 获取缺货教材
     */
    public List<Inventory> getOutOfStock() {
        return inventoryRepository.findAllOutOfStock();
    }

    /**
     * 获取低库存教材（低于阈值）
     */
    public List<Inventory> getLowStock(int threshold) {
        return inventoryRepository.findLowStock(threshold);
    }

    /**
     * 入库操作：增加库存并记录入库单
     */
    @Transactional
    public InboundRecord inbound(InboundRecord record) {
        // 验证教材存在
        if (!bookRepository.existsById(record.getBookId())) {
            throw new RuntimeException("教材不存在");
        }

        // 更新或创建库存记录
        Inventory inventory = inventoryRepository.findByBookId(record.getBookId())
                .orElseGet(() -> {
                    Inventory newInv = new Inventory();
                    newInv.setBookId(record.getBookId());
                    newInv.setQuantity(0);
                    return newInv;
                });

        inventory.setQuantity(inventory.getQuantity() + record.getQuantity());
        inventoryRepository.save(inventory);

        // 保存入库记录
        record.setInboundDate(LocalDateTime.now());
        return inboundRecordRepository.save(record);
    }

    /**
     * 出库操作：减少库存并记录出库单
     */
    @Transactional
    public OutboundRecord outbound(OutboundRecord record) {
        // 验证教材存在
        if (!bookRepository.existsById(record.getBookId())) {
            throw new RuntimeException("教材不存在");
        }

        // 检查库存是否充足
        Inventory inventory = inventoryRepository.findByBookId(record.getBookId())
                .orElseThrow(() -> new RuntimeException("库存记录不存在"));

        if (inventory.getQuantity() < record.getQuantity()) {
            throw new RuntimeException("库存不足！当前库存: " + inventory.getQuantity()
                    + ", 需要: " + record.getQuantity());
        }

        // 扣减库存
        inventory.setQuantity(inventory.getQuantity() - record.getQuantity());
        inventoryRepository.save(inventory);

        // 保存出库记录
        record.setOutboundDate(LocalDateTime.now());
        return outboundRecordRepository.save(record);
    }

    /**
     * 获取入库记录
     */
    public List<InboundRecord> getAllInboundRecords() {
        return inboundRecordRepository.findAllOrderByInboundDateDesc();
    }

    /**
     * 获取出库记录
     */
    public List<OutboundRecord> getAllOutboundRecords() {
        return outboundRecordRepository.findAllOrderByOutboundDateDesc();
    }
}
