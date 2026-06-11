package com.school.book;

import com.school.book.dto.OrderSubmitDTO;
import com.school.book.entity.*;
import com.school.book.repository.*;
import com.school.book.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 学校教材订购系统 - 单元测试
 */
@SpringBootTest
class SchoolTextbookOrderingSystemApplicationTests {

    @Autowired
    private UserService userService;

    @Autowired
    private BookService bookService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private PurchaseService purchaseService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    /**
     * 测试用户登录
     */
    @Test
    void testLogin() {
        User user = userService.login("teacher1", "123456");
        assertNotNull(user, "教师登录应该成功");
        assertEquals("TEACHER", user.getRole());

        User student = userService.login("student1", "123456");
        assertNotNull(student, "学生登录应该成功");
        assertEquals("STUDENT", student.getRole());

        User distributor = userService.login("distributor1", "123456");
        assertNotNull(distributor, "发行人员登录应该成功");
        assertEquals("DISTRIBUTOR", distributor.getRole());

        User purchaser = userService.login("purchaser1", "123456");
        assertNotNull(purchaser, "采购人员登录应该成功");
        assertEquals("PURCHASER", purchaser.getRole());
    }

    /**
     * 测试错误密码登录
     */
    @Test
    void testLoginWithWrongPassword() {
        User user = userService.login("teacher1", "wrong_password");
        assertNull(user, "错误密码登录应该返回null");
    }

    /**
     * 测试教材查询
     */
    @Test
    void testBookService() {
        List<Book> books = bookService.getAllBooks();
        assertNotNull(books);
        assertTrue(books.size() > 0, "应该存在教材数据");

        Book book = bookService.getBookByIsbn("978-7-04-053105-2");
        assertNotNull(book);
        assertEquals("高等数学（第七版）上册", book.getName());
    }

    /**
     * 测试库存查询
     */
    @Test
    void testInventory() {
        List<Inventory> allInventory = inventoryService.getAllInventory();
        assertNotNull(allInventory);

        List<Inventory> outOfStock = inventoryService.getOutOfStock();
        assertNotNull(outOfStock);
    }

    /**
     * 测试完整的购书流程
     */
    @Test
    @Transactional
    void testFullOrderFlow() {
        // 1. 学生提交购书单
        User student = userService.login("student1", "123456");
        assertNotNull(student);

        // 准备购书单数据
        OrderSubmitDTO dto = new OrderSubmitDTO();
        dto.setUserId(student.getId());
        dto.setRemark("测试购书单");

        OrderSubmitDTO.OrderItemDTO item = new OrderSubmitDTO.OrderItemDTO();
        item.setBookId(1); // 高等数学上册
        item.setQuantity(2);
        dto.setItems(List.of(item));

        Order order = orderService.submitOrder(dto);
        assertNotNull(order);
        assertEquals("PENDING", order.getStatus(), "新订单应为待审核状态");
        assertNotNull(order.getOrderNo(), "订单号不应为空");

        // 2. 教材发行人员审核
        User distributor = userService.login("distributor1", "123456");
        assertNotNull(distributor);

        Order approved = orderService.approveOrder(order.getId(), distributor.getId(), true, "审核通过");
        assertEquals("APPROVED", approved.getStatus(), "审核通过后状态应为 APPROVED");

        // 3. 开发票
        Invoice invoice = orderService.createInvoice(order.getId(), distributor.getId());
        assertNotNull(invoice);
        assertNotNull(invoice.getInvoiceNo());

        // 4. 领取教材
        orderService.fulfillOrder(order.getId(), distributor.getId());
        Order fulfilled = orderService.getOrderById(order.getId());
        assertEquals("FULFILLED", fulfilled.getStatus(), "领书后状态应为 FULFILLED");

        // 5. 验证库存已扣减
        Inventory inv = inventoryService.getInventoryByBookId(1);
        assertNotNull(inv);
        // 初始200 - 2 = 198
        assertEquals(198, inv.getQuantity(), "库存应该减少2本");
    }

    /**
     * 测试缺书登记和采购流程
     */
    @Test
    @Transactional
    void testPurchaseFlow() {
        // 1. 发行人员登记缺书
        User distributor = userService.login("distributor1", "123456");
        assertNotNull(distributor);

        // 教材ID 5（数据结构）初始库存为0
        OutOfStockRecord outOfStock = purchaseService.registerOutOfStock(
                5, 50, distributor.getId(), "数据结构教材缺书");
        assertNotNull(outOfStock);
        assertEquals("PENDING", outOfStock.getStatus());

        // 2. 采购人员创建采购单
        User purchaser = userService.login("purchaser1", "123456");
        assertNotNull(purchaser);

        PendingPurchase purchase = purchaseService.createPurchaseFromOutOfStock(
                outOfStock.getId(), purchaser.getId());
        assertNotNull(purchase);
        assertEquals("PENDING", purchase.getStatus());

        // 3. 采购到货入库
        InboundRecord inbound = purchaseService.receivePurchase(purchase.getId(), purchaser.getId());
        assertNotNull(inbound);
        assertEquals(50, inbound.getQuantity().intValue());

        // 4. 验证库存已更新
        Inventory inv = inventoryService.getInventoryByBookId(5);
        assertNotNull(inv);
        assertEquals(50, inv.getQuantity().intValue(), "库存应该增加50本");
    }
}
