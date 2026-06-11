# 数据库设计文档

## 1. 数据库概述

- **数据库名称**：school_textbook
- **字符集**：utf8mb4
- **排序规则**：utf8mb4_unicode_ci
- **数据库管理系统**：MySQL 8.0

## 2. E-R 图（文字描述）

### 实体关系

```
Users ──1:N──> Orders ──1:N──> OrderItems <──N:1── Books
                                                      │
                                                      │ 1:1
                                                      │
Users ──1:N──> OutOfStockRecords ──1:1──> PendingPurchases <──N:1── Books
                                                      │
                                                      │ 1:1
                                                      │
Users ──1:N──> InboundRecords <──N:1── Books
Users ──1:N──> OutboundRecords <──N:1── Books
Orders ──1:1──> Invoices
Books ──1:1──> Inventory
```

## 3. 表结构

### 3.1 用户表 (users)

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | INT | PK, AUTO_INCREMENT | 用户ID |
| username | VARCHAR(50) | NOT NULL, UNIQUE | 登录用户名 |
| password | VARCHAR(100) | NOT NULL | 登录密码 |
| real_name | VARCHAR(50) | NOT NULL | 真实姓名 |
| role | VARCHAR(20) | NOT NULL | 角色: TEACHER/STUDENT/DISTRIBUTOR/PURCHASER |
| phone | VARCHAR(20) | DEFAULT NULL | 联系电话 |
| email | VARCHAR(100) | DEFAULT NULL | 电子邮箱 |
| created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | DATETIME | DEFAULT CURRENT_TIMESTAMP ON UPDATE | 更新时间 |

索引：idx_role (role), idx_username (username)

### 3.2 教材信息表 (books)

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | INT | PK, AUTO_INCREMENT | 教材ID |
| isbn | VARCHAR(20) | NOT NULL, UNIQUE | ISBN编号 |
| name | VARCHAR(200) | NOT NULL | 教材名称 |
| author | VARCHAR(100) | DEFAULT NULL | 作者 |
| publisher | VARCHAR(100) | DEFAULT NULL | 出版社 |
| price | DECIMAL(10,2) | DEFAULT NULL | 定价 |
| description | TEXT | DEFAULT NULL | 描述 |
| created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | DATETIME | DEFAULT CURRENT_TIMESTAMP ON UPDATE | 更新时间 |

索引：idx_isbn (isbn), idx_name (name)

### 3.3 库存表 (inventory) — 数据存储D2

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | INT | PK, AUTO_INCREMENT | 库存ID |
| book_id | INT | NOT NULL, UNIQUE, FK→books(id) | 教材ID |
| quantity | INT | NOT NULL, DEFAULT 0 | 库存数量 |
| location | VARCHAR(100) | DEFAULT NULL | 存放位置 |
| updated_at | DATETIME | DEFAULT CURRENT_TIMESTAMP ON UPDATE | 更新时间 |

### 3.4 购书单表 (orders) — 数据存储D1

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | INT | PK, AUTO_INCREMENT | 购书单ID |
| order_no | VARCHAR(50) | NOT NULL, UNIQUE | 购书单号 |
| user_id | INT | NOT NULL, FK→users(id) | 提交人(教师/学生) |
| status | VARCHAR(20) | NOT NULL, DEFAULT 'PENDING' | PENDING/APPROVED/REJECTED/FULFILLED |
| total_amount | DECIMAL(12,2) | DEFAULT NULL | 总金额 |
| submit_date | DATETIME | DEFAULT CURRENT_TIMESTAMP | 提交日期 |
| approve_date | DATETIME | DEFAULT NULL | 审核日期 |
| approver_id | INT | DEFAULT NULL, FK→users(id) | 审核人ID |
| remark | TEXT | DEFAULT NULL | 备注 |
| created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | DATETIME | DEFAULT CURRENT_TIMESTAMP ON UPDATE | 更新时间 |

### 3.5 购书单明细表 (order_items)

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | INT | PK, AUTO_INCREMENT | 明细ID |
| order_id | INT | NOT NULL, FK→orders(id) ON DELETE CASCADE | 购书单ID |
| book_id | INT | NOT NULL, FK→books(id) | 教材ID |
| quantity | INT | NOT NULL | 数量 |
| price | DECIMAL(10,2) | DEFAULT NULL | 单价 |
| subtotal | DECIMAL(12,2) | DEFAULT NULL | 小计 |

### 3.6 缺书登记表 (out_of_stock_records) — 数据存储D3

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | INT | PK, AUTO_INCREMENT | 记录ID |
| book_id | INT | NOT NULL, FK→books(id) | 教材ID |
| quantity | INT | NOT NULL | 缺书数量 |
| register_date | DATETIME | DEFAULT CURRENT_TIMESTAMP | 登记日期 |
| register_id | INT | DEFAULT NULL, FK→users(id) | 登记人ID |
| status | VARCHAR(20) | NOT NULL, DEFAULT 'PENDING' | PENDING/PURCHASED |
| remark | TEXT | DEFAULT NULL | 备注 |
| created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP | 创建时间 |

### 3.7 待购教材表 (pending_purchases) — 数据存储D4

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | INT | PK, AUTO_INCREMENT | 采购单ID |
| book_id | INT | NOT NULL, FK→books(id) | 教材ID |
| quantity | INT | NOT NULL | 采购数量 |
| out_of_stock_id | INT | DEFAULT NULL, FK→out_of_stock_records(id) | 关联缺书登记ID |
| purchase_date | DATETIME | DEFAULT CURRENT_TIMESTAMP | 采购日期 |
| purchaser_id | INT | DEFAULT NULL, FK→users(id) | 采购人ID |
| status | VARCHAR(20) | NOT NULL, DEFAULT 'PENDING' | PENDING/ORDERED/RECEIVED |
| remark | TEXT | DEFAULT NULL | 备注 |
| created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | DATETIME | DEFAULT CURRENT_TIMESTAMP ON UPDATE | 更新时间 |

### 3.8 入库记录表 (inbound_records) — 数据存储D5

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | INT | PK, AUTO_INCREMENT | 入库ID |
| book_id | INT | NOT NULL, FK→books(id) | 教材ID |
| quantity | INT | NOT NULL | 入库数量 |
| inbound_date | DATETIME | DEFAULT CURRENT_TIMESTAMP | 入库日期 |
| operator_id | INT | DEFAULT NULL, FK→users(id) | 操作人ID |
| source | VARCHAR(20) | DEFAULT 'PURCHASE' | PURCHASE/RETURN |
| purchase_id | INT | DEFAULT NULL | 关联采购单ID |
| remark | TEXT | DEFAULT NULL | 备注 |
| created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP | 创建时间 |

### 3.9 出库记录表 (outbound_records) — 数据存储D5

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | INT | PK, AUTO_INCREMENT | 出库ID |
| book_id | INT | NOT NULL, FK→books(id) | 教材ID |
| quantity | INT | NOT NULL | 出库数量 |
| outbound_date | DATETIME | DEFAULT CURRENT_TIMESTAMP | 出库日期 |
| operator_id | INT | DEFAULT NULL, FK→users(id) | 操作人ID |
| order_item_id | INT | DEFAULT NULL | 关联购书单明细ID |
| order_no | VARCHAR(50) | DEFAULT NULL | 关联购书单号 |
| remark | TEXT | DEFAULT NULL | 备注 |
| created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP | 创建时间 |

### 3.10 发票表 (invoices)

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | INT | PK, AUTO_INCREMENT | 发票ID |
| invoice_no | VARCHAR(50) | NOT NULL, UNIQUE | 发票号 |
| order_id | INT | NOT NULL, FK→orders(id) | 关联购书单ID |
| amount | DECIMAL(12,2) | DEFAULT NULL | 发票金额 |
| issue_date | DATETIME | DEFAULT CURRENT_TIMESTAMP | 开票日期 |
| issuer_id | INT | DEFAULT NULL, FK→users(id) | 开票人ID |
| remark | TEXT | DEFAULT NULL | 备注 |
| created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP | 创建时间 |

## 4. 外键约束汇总

| 外键 | 来源表 | 目标表 | 说明 |
|------|--------|--------|------|
| fk_inventory_book | inventory | books | 教材库存关联 |
| fk_order_user | orders | users | 购书单提交人 |
| fk_order_approver | orders | users | 购书单审核人 |
| fk_order_item_order | order_items | orders | 购书单明细关联到购书单 |
| fk_order_item_book | order_items | books | 购书单明细关联到教材 |
| fk_out_of_stock_book | out_of_stock_records | books | 缺书记录关联教材 |
| fk_out_of_stock_register | out_of_stock_records | users | 缺书记录登记人 |
| fk_pending_purchase_book | pending_purchases | books | 采购单关联教材 |
| fk_pending_purchase_user | pending_purchases | users | 采购人 |
| fk_pending_purchase_out_of_stock | pending_purchases | out_of_stock_records | 采购单关联缺书记录 |
| fk_inbound_book | inbound_records | books | 入库记录关联教材 |
| fk_inbound_user | inbound_records | users | 入库操作人 |
| fk_outbound_book | outbound_records | books | 出库记录关联教材 |
| fk_outbound_user | outbound_records | users | 出库操作人 |
| fk_invoice_order | invoices | orders | 发票关联购书单 |
| fk_invoice_user | invoices | users | 开票人 |

## 5. 数据流

### 5.1 销售流程数据流

```
提交购书单: 教师/学生 → orders + order_items (INSERT)
审核购书单: orders.status → APPROVED/REJECTED (UPDATE)
开发票:     invoices (INSERT)
领书出库:   outbound_records (INSERT) + inventory.quantity (UPDATE)
                      + orders.status → FULFILLED (UPDATE)
```

### 5.2 采购流程数据流

```
登记缺书:   out_of_stock_records (INSERT)
创建采购单: pending_purchases (INSERT) + out_of_stock_records.status → PURCHASED (UPDATE)
到货入库:   inbound_records (INSERT) + inventory.quantity (UPDATE)
                      + pending_purchases.status → RECEIVED (UPDATE)
```

## 6. 事务一致性保证

关键事务：

```sql
-- 出库操作事务
START TRANSACTION;
  INSERT INTO outbound_records (book_id, quantity, ...) VALUES (?, ?, ...);
  UPDATE inventory SET quantity = quantity - ? WHERE book_id = ?;
  UPDATE orders SET status = 'FULFILLED' WHERE id = ?;
COMMIT;

-- 入库操作事务
START TRANSACTION;
  INSERT INTO inbound_records (book_id, quantity, ...) VALUES (?, ?, ...);
  UPDATE inventory SET quantity = quantity + ? WHERE book_id = ?;
  UPDATE pending_purchases SET status = 'RECEIVED' WHERE id = ?;
COMMIT;
```
