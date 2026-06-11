# 系统设计文档

## 1. 系统架构设计

### 1.1 总体架构

采用 **B/S 架构** + **Spring MVC 三层架构**：

```
┌─────────────────────────────────────────────────┐
│                  客户端 (Browser)                  │
│           Thymeleaf 渲染 + Bootstrap UI           │
└──────────────────────┬──────────────────────────┘
                       │ HTTP 请求
                       ▼
┌─────────────────────────────────────────────────┐
│              Controller 层 (控制层)                │
│        接收请求、参数校验、调用Service、返回视图       │
├─────────────────────────────────────────────────┤
│              Service 层 (业务逻辑层)                │
│        核心业务逻辑、事务管理、数据校验               │
├─────────────────────────────────────────────────┤
│            Repository 层 (数据访问层)               │
│          Spring Data JPA, 数据库CRUD操作            │
└──────────────────────┬──────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────┐
│                 MySQL 数据库                       │
│           8张核心表 + 2张辅助表                    │
└─────────────────────────────────────────────────┘
```

### 1.2 技术架构图

```
┌────────────────────────────────────────────────────────────┐
│                         前端层                              │
│  Thymeleaf 模板引擎 | Bootstrap 5 | Bootstrap Icons | CSS3 │
├────────────────────────────────────────────────────────────┤
│                        控制层                               │
│  LoginController | IndexController                         │
│  SalesController | PurchaseController                      │
│  InventoryController | BookController                       │
├────────────────────────────────────────────────────────────┤
│                        业务层                               │
│  UserService | BookService | OrderService                  │
│  InventoryService | PurchaseService                         │
├────────────────────────────────────────────────────────────┤
│                        数据层                               │
│  Spring Data JPA (Hibernate) | MySQL JDBC Driver            │
├────────────────────────────────────────────────────────────┤
│                      基础设施层                              │
│  Spring Boot 3.2 | Maven | Java 17                         │
└────────────────────────────────────────────────────────────┘
```

## 2. 模块设计

### 2.1 模块划分

```
学校教材订购系统
├── 销售管理模块 (Sales)
│   ├── 购书单管理
│   │   ├── 提交购书单
│   │   ├── 审核购书单
│   │   └── 查看购书单
│   ├── 发票管理
│   └── 领书管理
├── 采购管理模块 (Purchase)
│   ├── 缺书登记
│   ├── 采购单管理
│   └── 到货入库
├── 库存管理模块 (Inventory)
│   ├── 库存查询
│   ├── 入库记录
│   └── 出库记录
├── 教材管理模块 (Book)
│   ├── 教材查询
│   ├── 教材添加
│   └── 教材编辑
└── 系统管理
    ├── 用户登录
    └── 权限控制
```

### 2.2 模块交互

```
┌──────────┐     提交购书单      ┌──────────┐
│          │ ──────────────────> │          │
│ 教师/学生 │                     │ 销售模块  │
│          │ <────────────────── │          │
└──────────┘     返回领书通知     └────┬─────┘
                                      │
                                      │ 审核/开发票
                                      │
                                      ▼
                               ┌──────────────┐
                               │ 教材发行人员   │
                               └──────┬───────┘
                                      │
                           ┌──────────┴──────────┐
                           │                     │
                           ▼                     ▼
                     ┌──────────┐         ┌──────────┐
                     │ 库存模块  │         │ 采购模块  │
                     │          │         │          │
                     └──────────┘         └────┬─────┘
                                               │
                                               │ 采购
                                               ▼
                                        ┌──────────┐
                                        │ 采购人员  │
                                        └──────────┘
```

## 3. 类设计

### 3.1 核心类

```
┌─────────────────────────────────────┐
│  OrderService (销售系统核心)           │
├─────────────────────────────────────┤
│ + submitOrder(dto): Order           │
│ + approveOrder(id, approver, ...)   │
│ + createInvoice(orderId, issuer)    │
│ + fulfillOrder(orderId, operator)   │
│ + getOrderById(id): Order           │
│ + getOrdersByUserId(id): List<Order>│
│ + getAllOrders(): List<Order>       │
└─────────────────────────────────────┘

┌─────────────────────────────────────┐
│  PurchaseService (采购系统核心)        │
├─────────────────────────────────────┤
│ + registerOutOfStock(...)           │
│ + createPurchaseFromOutOfStock(...) │
│ + createPurchaseDirectly(...)       │
│ + receivePurchase(purchaseId, ...)  │
│ + getAllOutOfStockRecords()         │
│ + getAllPendingPurchases()          │
└─────────────────────────────────────┘

┌─────────────────────────────────────┐
│  InventoryService (库存管理)          │
├─────────────────────────────────────┤
│ + getAllInventory(): List<Inventory> │
│ + getInStock(): List<Inventory>     │
│ + getOutOfStock(): List<Inventory>  │
│ + inbound(record): InboundRecord    │
│ + outbound(record): OutboundRecord  │
└─────────────────────────────────────┘
```

### 3.2 实体关系

实体类之间的关系：

```
User 1 ──── * Order       (一个用户可以有多个购书单)
Order 1 ──── * OrderItem  (一个购书单包含多个明细)
Book 1 ──── 1 Inventory   (一本教材对应一条库存记录)
Book 1 ──── * OrderItem   (一本教材可以在多个购书单明细中)
Book 1 ──── * OutOfStock  (一本教材可以有多次缺书记录)
Book 1 ──── * PendingPurchase (一本教材可以有多个采购单)
Book 1 ──── * InboundRecord (一本教材可以有多次入库记录)
Book 1 ──── * OutboundRecord (一本教材可以有多次出库记录)
Order 1 ──── 1 Invoice    (一个购书单对应一张发票)
OutOfStock 1 ──── 1 PendingPurchase (一个缺书记录对应一个采购单)
PendingPurchase 1 ──── 1 InboundRecord (一个采购单对应一个入库记录)
```

## 4. 界面设计

### 4.1 页面流转

```
/login ──> /dashboard
              │
              ├── /sales/orders ──> /sales/orders/submit
              │        │              /sales/orders/{id}
              │        │              /sales/orders/{id}/approve
              │        └── /sales/invoices
              │
              ├── /purchase/out-of-stock ──> /purchase/out-of-stock/register
              │        │
              │        └── /purchase/purchases ──> /purchase/purchases/create
              │
              ├── /inventory ──> /inventory/inbound
              │        └── /inventory/outbound
              │
              └── /books ──> /books/add
                       └── /books/edit/{id}
```

### 4.2 界面设计原则

- **响应式布局**：基于 Bootstrap 5 响应式网格系统
- **统一布局**：所有页面共享导航栏和布局模板
- **状态可视化**：订单/采购单状态使用彩色标签区分
- **操作反馈**：操作成功/失败通过提示消息反馈
- **角色适配**：根据用户角色显示不同的操作入口

## 5. 安全设计

### 5.1 登录认证
- 基于 Session 的用户登录状态管理
- 未登录用户自动重定向到登录页面

### 5.2 权限控制
- 基于角色（TEACHER, STUDENT, DISTRIBUTOR, PURCHASER）的访问控制
- 教材发行人员：购书单审核、开发票、缺书登记
- 采购人员：采购单创建、到货入库
- 教师/学生：提交购书单、领书
- 控制器层进行角色校验

### 5.3 数据校验
- 前端：HTML5 表单验证
- 后端：Jakarta Validation (@NotNull, @NotEmpty, @Positive)
- Service 层业务规则校验（库存不足检查、状态流转检查等）

## 6. 事务设计

关键事务场景：

- **提交购书单**：创建订单 + 订单明细（同一个事务）
- **出库操作**：创建出库记录 + 扣减库存（同一个事务）
- **入库操作**：创建入库记录 + 增加库存（同一个事务）
- **采购到货**：入库 + 更新采购单状态（同一个事务）

使用 Spring `@Transactional` 注解保证事务一致性。
