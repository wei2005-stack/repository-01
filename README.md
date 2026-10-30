# 高校学籍管理系统（Java / Spring Boot 版）

> 数据库课程设计 | 课题编号：22
>
> 在原有 Python + FastAPI 版本基础上，使用 Java 技术栈重写后端，保留原有页面与业务功能。

## 项目简介

基于 **Spring Boot 3 + MyBatis-Plus + SQL Server** 的 B/S 架构高校学籍管理系统，实现院系/专业/班级/课程管理、学生信息管理、成绩管理、奖惩管理、统计查询、CSV 导入导出、数据备份恢复与操作日志审计。

项目保留原有 4 个数据库视图、5 个存储过程与触发器，通过 MyBatis XML 直接调用，展示关系型数据库在真实业务中的使用方式。

## 技术栈

| 层级 | 技术选型 |
|------|----------|
| 语言 / 运行时 | Java 17 |
| 后端框架 | Spring Boot 3.2.12（Spring MVC） |
| 持久层 | MyBatis-Plus 3.5.7 + MyBatis XML |
| 数据库 | MS SQL Server（mssql-jdbc 12.6.1） |
| 安全认证 | Spring Security 6 + JWT（JJWT 0.12.6） |
| 模板引擎 | Thymeleaf 3 |
| 前端 | Bootstrap 5.3 + Chart.js 4.4 |
| 构建工具 | Maven 3.9 |

## 核心实现

- **认证与授权**：Spring Security 6 无状态会话（STATELESS），登录成功后签发 JWT 并写入 HttpOnly Cookie；JWT 过滤器解析用户身份，按 `admin / academic / readonly` 三种角色控制菜单、接口与数据范围。
- **密码平滑迁移**：兼容原系统 SHA-256（固定盐）密码，用户首次登录成功后自动升级为 BCrypt，历史账号无需重置密码。
- **数据库对象复用**：通过 MyBatis XML 调用 4 个视图与 5 个存储过程，分页查询使用 SQL Server `OFFSET ... FETCH NEXT`，MyBatis-Plus 分页插件按 `SQL_SERVER` 方言工作。
- **批量导入导出**：基于 Apache Commons CSV 实现学生/成绩 CSV 导入导出，包含表头校验、学号/身份证去重、班级/课程存在性校验、错误行跳过策略，导出文件带 UTF-8 BOM 防止 Excel 乱码。
- **备份恢复**：通过 `sqlcmd` 进程执行完整/差异备份、还原与删除，带路径穿越校验与超时控制。
- **操作日志**：沿用数据库触发器自动记录增删改操作，管理端支持按表名、操作类型、关键字筛选。

## 功能列表

### 基础信息管理
- 院系、专业、班级、课程增删改查
- 学生信息多条件筛选（院系/专业/班级/年级）、关键字搜索、分页
- 学生详情页（基本信息 + 成绩 + 奖惩）

### 业务功能
- 成绩管理（按学号/姓名/课程/学期筛选）
- 奖惩记录管理
- 学生 / 成绩 CSV 导入导出（按筛选条件自动命名）

### 统计查询
- 各院系、各专业男女人数统计（存储过程）
- 学生信息视图、成绩综合视图、不及格学生视图、班级统计视图
- 成绩等级分布饼图、课程平均分柱状图
- 首页仪表盘（统计卡片 + 院系专业数 / 课程类型 / 院系学生人数图表）

### 系统管理
- 登录 / 登出（JWT + HttpOnly Cookie）
- 用户管理（新增、启用禁用、重置密码、删除）
- 数据库备份与恢复
- 操作日志审计

## 快速开始

### 1. 环境要求

- JDK 17
- Maven 3.9+
- MS SQL Server（需开启 TCP 1433，JDBC 无法通过共享内存连接）

### 2. 初始化数据库

在 SSMS 中按顺序执行 `SQL/` 下的脚本：

```
SQL/01_create_database.sql     # 建库建表
SQL/02_views.sql               # 4 个视图
SQL/03_stored_procedures.sql   # 5 个存储过程
SQL/04_triggers.sql            # 触发器（班级人数 + 操作日志）
SQL/05_indexes.sql             # 索引
SQL/06_security.sql            # 安全性
SQL/08_test_data.sql           # 测试数据
SQL/10_app_users.sql           # 应用用户表
SQL/11_operation_log.sql       # 操作日志表
```

### 3. 修改数据库配置

编辑 `java-backend/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:sqlserver://localhost:1433;databaseName=student_manage;encrypt=true;trustServerCertificate=true
    username: sa
    password: 123456
```

### 4. 启动

```bash
cd java-backend
mvn spring-boot:run
```

或先打包再运行：

```bash
mvn -DskipTests package
java -jar target/student-manage-1.0.0.jar
```

访问 <http://localhost:8080>

### 5. 运行测试

```bash
cd java-backend
mvn test
```

单元测试位于 `java-backend/src/test/java`，覆盖 JWT 签发与解析、密码编码器（历史 SHA-256 兼容 + BCrypt）、角色判断与分页查询串构造，无需连接数据库。

### 6. 默认账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | admin123 | 管理员（全部权限） |
| teacher | teacher123 | 教务老师（读写权限） |
| student | student123 | 学生（仅查看本人信息） |

## 项目结构

```
数据库课程设计-修改版/
├── java-backend/                        # Java 后端（当前主实现）
│   ├── pom.xml
│   ├── src/main/java/com/tqh/studentmanage/
│   │   ├── config/                      # Security / MyBatis-Plus / Web 配置
│   │   ├── controller/                  # 各业务 Controller
│   │   ├── dto/                         # 导入结果等 DTO
│   │   ├── entity/                      # 数据库实体
│   │   ├── mapper/                      # MyBatis-Plus Mapper 接口
│   │   ├── security/                    # JWT、UserDetails、密码编码器
│   │   ├── service/                     # 学生/成绩导入导出、备份
│   │   └── util/                        # URL 参数工具
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   ├── mapper/                      # MyBatis XML（视图 / 存储过程 / 分页）
│   │   ├── static/                      # CSS、Logo、图片、CSV 模板
│   │   └── templates/                   # 25 个 Thymeleaf 页面
│   ├── src/test/java/                   # JWT / 密码编码器 / 工具类单元测试
│   └── logs/                            # 运行日志
├── SQL/                                 # 数据库脚本
├── backup/                              # 备份文件目录
├── .gitignore
└── README.md
```

## 数据库设计

### 数据表

| 表名 | 说明 |
|------|------|
| Department | 院系表 |
| Major | 专业表 |
| Class | 班级表 |
| Student | 学生表 |
| Course | 课程表 |
| Score | 成绩表（复合主键：学号 + 课程 + 学期 + 考试类型） |
| RewardPunishment | 奖惩记录表 |
| AppUser | 应用用户表 |
| OperationLog | 操作日志表 |

### 数据库对象

| 类型 | 数量 | 说明 |
|------|------|------|
| 视图 | 4 | v_StudentInfo、v_StudentScore、v_ClassStatistics、v_FailedStudents |
| 存储过程 | 5 | 性别统计、班级成绩统计、成绩分布、新增学生 |
| 触发器 | 6 | 维护班级人数 + 记录操作日志 |
| 索引 | 14 | 覆盖高频查询 |

## 权限说明

| 功能 | 管理员 | 教务老师 | 学生 |
|------|:------:|:--------:|:----:|
| 首页仪表盘 | ✅ | ✅ | ✅ |
| 基础信息管理 | 增删改查 | 增删改查 | ❌ |
| 学生管理 | 增删改查 | 增删改（不可删除） | ❌ |
| 成绩管理 | 增删改查 | 增删改（不可删除） | ❌ |
| 奖惩管理 | 增删改查 | 增删改（不可删除） | ❌ |
| 统计查询 | ✅ | ✅ | 仅本人 |
| 导入导出 CSV | ✅ | ✅ | ❌ |
| 用户管理 | ✅ | ❌ | ❌ |
| 备份恢复 | ✅ | ❌ | ❌ |
| 操作日志 | ✅ | ❌ | ❌ |

## 常见问题

**Q：JDBC 连接失败，提示 TCP/IP 连接失败？**
A：在 SQL Server 配置管理器中启用 `MSSQLSERVER` 的 TCP/IP 协议，端口设为 1433 并重启服务。`sqlcmd` 默认走共享内存，能连接不代表 JDBC 能连接。

**Q：原有账号密码无法登录？**
A：系统兼容原 SHA-256 密码，首次登录成功后会自动升级为 BCrypt。确认数据库 `AppUser` 表数据来自 `SQL/10_app_users.sql`。

**Q：CSV 导出用 Excel 打开中文乱码？**
A：导出已写入 UTF-8 BOM；若仍乱码，请使用 Excel 的“数据 → 从文本/CSV”并选择 UTF-8。

**Q：备份失败？**
A：备份依赖 `sqlcmd`，请确认已安装并加入 PATH，同时确认 `application.yml` 中 `app.backup-dir` 与 `app.sqlcmd.*` 配置正确。

## 说明

- 本项目为课程设计作品，仅供学习使用。
