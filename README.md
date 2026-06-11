# 学校教材订购系统

![Java](https://img.shields.io/badge/Java-17-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-green)
![MySQL](https://img.shields.io/badge/MySQL-8.0-orange)
![License](https://img.shields.io/badge/License-MIT-lightgrey)

## 项目简介

**学校教材订购系统** 是一个基于 **Spring Boot + MySQL** 的 Web 应用，实现学校教材订购的数字化管理。本系统为**软件工程课程设计**项目，涵盖了软件工程的需求分析、系统设计、实现与测试全过程。

系统分为两个核心子系统：

- **销售系统**：教师/学生提交购书单 → 教材发行人员审核 → 开发票 → 书库领书
- **采购系统**：登记缺书 → 发缺书单给采购人员 → 新书入库 → 通知教材发行人员

## 功能模块

### 1. 销售管理
- 教师/学生在线提交购书单
- 教材发行人员审核购书单（通过/驳回）
- 审核通过后自动开具发票
- 书库凭领书单出库发书

### 2. 采购管理
- 教材发行人员登记缺书记录
- 自动/手动生成采购单
- 采购到货后入库操作
- 库存自动更新

### 3. 库存管理
- 实时库存查询
- 入库/出库记录追溯
- 库存预警（低库存/缺货标识）

### 4. 教材管理
- 教材信息增删改查
- ISBN 唯一标识

## 技术栈

| 层级 | 技术 |
|------|------|
| 前端 | Thymeleaf + Bootstrap 5 + Bootstrap Icons |
| 后端 | Spring Boot 3.2 (Spring MVC, Spring Data JPA) |
| 数据库 | MySQL 8.0 |
| 构建工具 | Maven |
| 语言 | Java 17 |
| 测试 | JUnit 5 + Spring Boot Test |

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.8+
- MySQL 8.0+

### 1. 创建数据库

```sql
CREATE DATABASE school_textbook DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

或者直接运行 SQL 脚本：

```bash
mysql -u root -p < sql/init.sql
```

### 2. 修改数据库配置

编辑 `src/main/resources/application.yml`，修改数据库连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/school_textbook?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root       # 修改为你的数据库用户名
    password: root       # 修改为你的数据库密码
```

### 3. 启动项目

```bash
# 使用 Maven 直接启动
mvn spring-boot:run

# 或者打包后运行
mvn package -DskipTests
java -jar target/school-textbook-ordering-system-1.0.0.jar
```

### 4. 访问系统

打开浏览器访问：**http://localhost:8080**

### 5. 测试账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| teacher1 | 123456 | 教师 |
| teacher2 | 123456 | 教师 |
| student1 | 123456 | 学生 |
| student2 | 123456 | 学生 |
| distributor1 | 123456 | 教材发行人员 |
| purchaser1 | 123456 | 采购人员 |

## 系统架构

### 三层架构

```
Controller (控制层) → Service (业务层) → Repository (数据访问层)
    ↓                      ↓                       ↓
  Thymeleaf模板          业务逻辑               JPA/MySQL
```

### 数据存储结构（6个核心表）

1. **orders** - 购书单表
2. **inventory** - 库存表
3. **out_of_stock_records** - 缺书登记表
4. **pending_purchases** - 待购教材表
5. **inbound_records** - 入库记录表
6. **outbound_records** - 出库记录表

辅助表：users（用户表）、books（教材信息表）、order_items（购书单明细）、invoices（发票表）

## 业务流程

### 销售系统流程

```
教师/学生                   教材发行人员                 书库
    |                          |                       |
    |-- 提交购书单 -->          |                       |
    |                          |-- 审核购书单 --        |
    |                          |   (通过/驳回)         |
    |                          |                       |
    |                          |-- 开发票 -->           |
    |<-- 领书通知 --- ----- ---|                       |
    |-- 领取教材 ----------------------------------->  |
    |                          |                       |-- 出库
    |                          |<-- 完成通知 --- -----|
```

### 采购系统流程

```
教材发行人员                   采购人员                  书库
    |                          |                       |
    |-- 登记缺书 -->           |                       |
    |                          |-- 创建采购单 --        |
    |                          |-- 采购到货 --------->  |
    |                          |                       |-- 入库
    |<-- 进书通知 --- --- ----|                       |
```

## 项目结构

```
school-textbook-ordering-system/
├── pom.xml                          # Maven 构建配置
├── sql/
│   └── init.sql                     # 数据库初始化脚本
├── docs/
│   ├── requirements.md              # 需求分析文档
│   ├── design.md                    # 系统设计文档
│   └── database-design.md           # 数据库设计文档
├── src/
│   ├── main/
│   │   ├── java/com/school/book/
│   │   │   ├── SchoolTextbookOrderingSystemApplication.java  # 启动类
│   │   │   ├── common/              # 通用工具类
│   │   │   ├── config/              # 配置类
│   │   │   ├── entity/              # 实体类 (JPA)
│   │   │   ├── repository/          # 数据访问层
│   │   │   ├── service/             # 业务逻辑层
│   │   │   ├── controller/          # 控制层
│   │   │   └── dto/                 # 数据传输对象
│   │   └── resources/
│   │       ├── application.yml      # 应用配置
│   │       ├── static/              # 静态资源 (CSS/JS)
│   │       └── templates/           # Thymeleaf 模板
│   │           ├── common/          # 公共布局
│   │           ├── sales/           # 销售系统页面
│   │           ├── purchase/        # 采购系统页面
│   │           ├── inventory/       # 库存管理页面
│   │           └── book/            # 教材管理页面
│   └── test/                        # 单元测试
└── README.md
```

## 运行测试

```bash
# 运行所有测试
mvn test

# 运行指定测试类
mvn test -Dtest=SchoolTextbookOrderingSystemApplicationTests
```

## 验证功能

### 销售系统验证
1. 用 student1 登录，提交购书单
2. 切换 distributor1 登录，审核购书单
3. 在购书单详情页点击"开发票"
4. 点击"领取教材"完成出库

### 采购系统验证
1. 用 distributor1 登录，登记缺书
2. 切换 purchaser1 登录，在缺书记录中点击"生成采购单"
3. 在采购单列表中点击"到货入库"
4. 查看库存已更新

## 软件工程文档

详细文档请参阅 `docs/` 目录：

- [需求分析文档](docs/requirements.md) - 系统需求、用例分析
- [系统设计文档](docs/design.md) - 架构设计、模块设计
- [数据库设计文档](docs/database-design.md) - ER图、表结构

## 外部实体

- **教师 (Teacher)** - 提交购书单、领取教材
- **学生 (Student)** - 提交购书单、领取教材
- **教材发行人员 (Distributor)** - 审核购书单、开发票、登记缺书
- **采购人员 (Purchaser)** - 采购教材、入库管理

## 开发环境

- **操作系统**: Windows/Linux/macOS
- **IDE**: IntelliJ IDEA / Eclipse / VS Code
- **数据库管理**: MySQL Workbench / Navicat
- **API测试**: Postman
- **版本管理**: Git

## 许可

本项目仅用于教育目的。
