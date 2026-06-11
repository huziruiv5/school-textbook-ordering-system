-- ============================================
-- 学校教材订购系统 - 数据库初始化脚本
-- 软件工程课程设计
-- ============================================

CREATE DATABASE IF NOT EXISTS school_textbook
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE school_textbook;

-- ============================================
-- 用户表
-- ============================================
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '登录用户名',
    `password` VARCHAR(100) NOT NULL COMMENT '登录密码',
    `real_name` VARCHAR(50) NOT NULL COMMENT '真实姓名',
    `role` VARCHAR(20) NOT NULL COMMENT '角色: TEACHER-教师, STUDENT-学生, DISTRIBUTOR-教材发行人员, PURCHASER-采购人员',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '电子邮箱',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_role` (`role`),
    INDEX `idx_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ============================================
-- 教材信息表
-- ============================================
DROP TABLE IF EXISTS `books`;
CREATE TABLE `books` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `isbn` VARCHAR(20) NOT NULL UNIQUE COMMENT 'ISBN编号',
    `name` VARCHAR(200) NOT NULL COMMENT '教材名称',
    `author` VARCHAR(100) DEFAULT NULL COMMENT '作者',
    `publisher` VARCHAR(100) DEFAULT NULL COMMENT '出版社',
    `price` DECIMAL(10,2) DEFAULT NULL COMMENT '定价',
    `description` TEXT DEFAULT NULL COMMENT '描述',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_isbn` (`isbn`),
    INDEX `idx_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教材信息表';

-- ============================================
-- 库存表
-- ============================================
DROP TABLE IF EXISTS `inventory`;
CREATE TABLE `inventory` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `book_id` INT NOT NULL UNIQUE COMMENT '教材ID',
    `quantity` INT NOT NULL DEFAULT 0 COMMENT '库存数量',
    `location` VARCHAR(100) DEFAULT NULL COMMENT '存放位置',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_book_id` (`book_id`),
    CONSTRAINT `fk_inventory_book` FOREIGN KEY (`book_id`) REFERENCES `books`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存表';

-- ============================================
-- 购书单表
-- ============================================
DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `order_no` VARCHAR(50) NOT NULL UNIQUE COMMENT '购书单号',
    `user_id` INT NOT NULL COMMENT '提交人(教师/学生)',
    `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态: PENDING-待审核, APPROVED-已通过, REJECTED-已驳回, FULFILLED-已完成',
    `total_amount` DECIMAL(12,2) DEFAULT NULL COMMENT '总金额',
    `submit_date` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '提交日期',
    `approve_date` DATETIME DEFAULT NULL COMMENT '审核日期',
    `approver_id` INT DEFAULT NULL COMMENT '审核人ID',
    `remark` TEXT DEFAULT NULL COMMENT '备注',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_order_no` (`order_no`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_status` (`status`),
    CONSTRAINT `fk_order_user` FOREIGN KEY (`user_id`) REFERENCES `users`(`id`),
    CONSTRAINT `fk_order_approver` FOREIGN KEY (`approver_id`) REFERENCES `users`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购书单表';

-- ============================================
-- 购书单明细表
-- ============================================
DROP TABLE IF EXISTS `order_items`;
CREATE TABLE `order_items` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `order_id` INT NOT NULL COMMENT '购书单ID',
    `book_id` INT NOT NULL COMMENT '教材ID',
    `quantity` INT NOT NULL COMMENT '数量',
    `price` DECIMAL(10,2) DEFAULT NULL COMMENT '单价',
    `subtotal` DECIMAL(12,2) DEFAULT NULL COMMENT '小计',
    INDEX `idx_order_id` (`order_id`),
    INDEX `idx_book_id` (`book_id`),
    CONSTRAINT `fk_order_item_order` FOREIGN KEY (`order_id`) REFERENCES `orders`(`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_order_item_book` FOREIGN KEY (`book_id`) REFERENCES `books`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购书单明细表';

-- ============================================
-- 缺书登记表
-- ============================================
DROP TABLE IF EXISTS `out_of_stock_records`;
CREATE TABLE `out_of_stock_records` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `book_id` INT NOT NULL COMMENT '教材ID',
    `quantity` INT NOT NULL COMMENT '缺书数量',
    `register_date` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '登记日期',
    `register_id` INT DEFAULT NULL COMMENT '登记人ID',
    `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态: PENDING-待处理, PURCHASED-已采购',
    `remark` TEXT DEFAULT NULL COMMENT '备注',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_book_id` (`book_id`),
    INDEX `idx_status` (`status`),
    CONSTRAINT `fk_out_of_stock_book` FOREIGN KEY (`book_id`) REFERENCES `books`(`id`),
    CONSTRAINT `fk_out_of_stock_register` FOREIGN KEY (`register_id`) REFERENCES `users`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='缺书登记表';

-- ============================================
-- 待购教材表
-- ============================================
DROP TABLE IF EXISTS `pending_purchases`;
CREATE TABLE `pending_purchases` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `book_id` INT NOT NULL COMMENT '教材ID',
    `quantity` INT NOT NULL COMMENT '采购数量',
    `out_of_stock_id` INT DEFAULT NULL COMMENT '关联缺书登记ID',
    `purchase_date` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '采购日期',
    `purchaser_id` INT DEFAULT NULL COMMENT '采购人ID',
    `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态: PENDING-待订购, ORDERED-已订购, RECEIVED-已到货',
    `remark` TEXT DEFAULT NULL COMMENT '备注',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_book_id` (`book_id`),
    INDEX `idx_status` (`status`),
    CONSTRAINT `fk_pending_purchase_book` FOREIGN KEY (`book_id`) REFERENCES `books`(`id`),
    CONSTRAINT `fk_pending_purchase_user` FOREIGN KEY (`purchaser_id`) REFERENCES `users`(`id`),
    CONSTRAINT `fk_pending_purchase_out_of_stock` FOREIGN KEY (`out_of_stock_id`) REFERENCES `out_of_stock_records`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='待购教材表';

-- ============================================
-- 入库记录表
-- ============================================
DROP TABLE IF EXISTS `inbound_records`;
CREATE TABLE `inbound_records` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `book_id` INT NOT NULL COMMENT '教材ID',
    `quantity` INT NOT NULL COMMENT '入库数量',
    `inbound_date` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '入库日期',
    `operator_id` INT DEFAULT NULL COMMENT '操作人ID',
    `source` VARCHAR(20) DEFAULT 'PURCHASE' COMMENT '来源: PURCHASE-采购入库, RETURN-退回入库',
    `purchase_id` INT DEFAULT NULL COMMENT '关联采购单ID',
    `remark` TEXT DEFAULT NULL COMMENT '备注',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_book_id` (`book_id`),
    INDEX `idx_inbound_date` (`inbound_date`),
    CONSTRAINT `fk_inbound_book` FOREIGN KEY (`book_id`) REFERENCES `books`(`id`),
    CONSTRAINT `fk_inbound_user` FOREIGN KEY (`operator_id`) REFERENCES `users`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='入库记录表';

-- ============================================
-- 出库记录表
-- ============================================
DROP TABLE IF EXISTS `outbound_records`;
CREATE TABLE `outbound_records` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `book_id` INT NOT NULL COMMENT '教材ID',
    `quantity` INT NOT NULL COMMENT '出库数量',
    `outbound_date` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '出库日期',
    `operator_id` INT DEFAULT NULL COMMENT '操作人ID',
    `order_item_id` INT DEFAULT NULL COMMENT '关联购书单明细ID',
    `order_no` VARCHAR(50) DEFAULT NULL COMMENT '关联购书单号',
    `remark` TEXT DEFAULT NULL COMMENT '备注',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_book_id` (`book_id`),
    INDEX `idx_outbound_date` (`outbound_date`),
    CONSTRAINT `fk_outbound_book` FOREIGN KEY (`book_id`) REFERENCES `books`(`id`),
    CONSTRAINT `fk_outbound_user` FOREIGN KEY (`operator_id`) REFERENCES `users`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='出库记录表';

-- ============================================
-- 发票表
-- ============================================
DROP TABLE IF EXISTS `invoices`;
CREATE TABLE `invoices` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `invoice_no` VARCHAR(50) NOT NULL UNIQUE COMMENT '发票号',
    `order_id` INT NOT NULL COMMENT '关联购书单ID',
    `amount` DECIMAL(12,2) DEFAULT NULL COMMENT '发票金额',
    `issue_date` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '开票日期',
    `issuer_id` INT DEFAULT NULL COMMENT '开票人ID',
    `remark` TEXT DEFAULT NULL COMMENT '备注',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_invoice_no` (`invoice_no`),
    INDEX `idx_order_id` (`order_id`),
    CONSTRAINT `fk_invoice_order` FOREIGN KEY (`order_id`) REFERENCES `orders`(`id`),
    CONSTRAINT `fk_invoice_user` FOREIGN KEY (`issuer_id`) REFERENCES `users`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='发票表';

-- ============================================
-- 插入初始数据
-- ============================================

-- 默认用户（密码为 123456 的 BCrypt 加密值）
INSERT INTO `users` (`username`, `password`, `real_name`, `role`, `phone`, `email`) VALUES
('teacher1', '123456', '张老师', 'TEACHER', '13800138001', 'teacher1@school.edu.cn'),
('teacher2', '123456', '李老师', 'TEACHER', '13800138002', 'teacher2@school.edu.cn'),
('student1', '123456', '王同学', 'STUDENT', '13900139001', 'student1@school.edu.cn'),
('student2', '123456', '赵同学', 'STUDENT', '13900139002', 'student2@school.edu.cn'),
('distributor1', '123456', '陈发行员', 'DISTRIBUTOR', '13700137001', 'distributor1@school.edu.cn'),
('purchaser1', '123456', '刘采购员', 'PURCHASER', '13600136001', 'purchaser1@school.edu.cn');

-- 默认教材
INSERT INTO `books` (`isbn`, `name`, `author`, `publisher`, `price`) VALUES
('978-7-04-053105-2', '高等数学（第七版）上册', '同济大学数学系', '高等教育出版社', 49.80),
('978-7-04-053106-9', '高等数学（第七版）下册', '同济大学数学系', '高等教育出版社', 45.60),
('978-7-04-052614-0', '线性代数（第六版）', '同济大学数学系', '高等教育出版社', 36.80),
('978-7-04-050272-4', '概率论与数理统计（第四版）', '盛骤、谢式千、潘承毅', '高等教育出版社', 42.50),
('978-7-302-59332-5', '数据结构（C语言版）', '严蔚敏、吴伟民', '清华大学出版社', 39.00),
('978-7-111-68504-3', '计算机组成原理', '唐朔飞', '机械工业出版社', 55.00),
('978-7-302-59333-2', '操作系统概论', '汤小丹', '清华大学出版社', 48.00),
('978-7-121-40035-1', '计算机网络（第8版）', '谢希仁', '电子工业出版社', 52.00),
('978-7-04-053107-6', '大学物理（上）', '张三慧', '高等教育出版社', 55.00),
('978-7-04-053108-3', '大学物理（下）', '张三慧', '高等教育出版社', 55.00);

-- 默认库存
INSERT INTO `inventory` (`book_id`, `quantity`, `location`) VALUES
(1, 200, 'A区-1架'),
(2, 180, 'A区-1架'),
(3, 150, 'A区-2架'),
(4, 160, 'A区-2架'),
(5, 0, 'B区-1架'),
(6, 0, 'B区-1架'),
(7, 120, 'B区-2架'),
(8, 100, 'B区-2架'),
(9, 90, 'C区-1架'),
(10, 85, 'C区-1架');
