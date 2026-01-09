# 增强型脚手架

## 项目概述

> 本项目是一个基于Spring Boot后端框架与Vue前端框架的现代化管理系统，采用前后端分离架构，适用于毕业设计管理等企业级应用场景。

- **后端**：Spring Boot 3.4.7 + MyBatis + MySQL + JWT
- **前端**：Vue 3 + Vite + Element Plus
- **认证**：JWT Token认证机制
- **日志**：通过AOP实现操作日志记录
- **缓存**：集成Redis缓存优化系统性能
- **其他**：集成Swagger API文档，支持跨域访问

### 技术栈

| 类型 | 技术/框架 |
|------|-----------|
| 后端 | Spring Boot 3.4.7 / MyBatis / Java 17 |
| 数据库 | MySQL / Redis |
| 安全认证 | JWT / Spring Security |
| 前端 | Vue 3 / Vite / Element Plus / Pinia |
| 工具 | Swagger（API文档）、AOP（日志记录）、Lombok |

---

## 目录结构

```bash
.
├── springboot        # Spring Boot 后端项目
│   ├── src           # 源码目录
│   │   └── main      # 主程序目录
│   │       ├── java  # Java源代码
│   │       │   └── com
│   │       │       └── example
│   │       │           ├── annotation    # 自定义注解（审计日志）
│   │       │           ├── aspect        # AOP切面（审计日志处理）
│   │       │           ├── common        # 公共类（响应结果封装、枚举）
│   │       │           ├── config        # 配置类（跨域、JSON配置等）
│   │       │           ├── controller    # 控制器（用户、管理员、菜单等）
│   │       │           ├── entity        # 实体类（用户、管理员、菜单等）
│   │       │           ├── enums         # 枚举类（角色枚举）
│   │       │           ├── exception     # 异常处理（全局异常处理器）
│   │       │           ├── mapper        # 数据访问层（MyBatis接口）
│   │       │           ├── security      # 安全相关（JWT认证过滤器）
│   │       │           ├── service       # 服务接口及实现（用户、管理员等）
│   │       │           ├── strategy    # 策略模式（角色策略）
│   │       │           ├── utils         # 工具类（Token工具类、Redis工具类）
│   │       │           └── SpringbootApplication.java # 应用启动类
│   │       └── resources # 配置和资源文件
│   │           ├── mapper    # MyBatis映射文件
│   │           └── application.yml # 配置文件
│   └── pom.xml       # Maven项目配置文件
├── vue               # Vue 前端项目
│   ├── src           # 源码目录
│   │   ├── api       # 接口调用（用户、菜单、认证等API封装）
│   │   ├── assets    # 静态资源（CSS、图片等）
│   │   ├── router    # 路由配置（登录、注册、管理页面等）
│   │   ├── utils     # 工具类（Axios请求封装）
│   │   ├── views     # 页面组件（登录、注册、管理等页面）
│   │   ├── App.vue   # 根组件
│   │   └── main.js   # 应用入口
│   ├── index.html    # 入口HTML文件
│   ├── jsconfig.json # JavaScript配置
│   ├── package-lock.json # 包版本锁定
│   ├── package.json  # npm包配置文件
│   └── vite.config.js # Vite配置文件
├── sql               # SQL脚本目录
└── README.md         # 项目说明文档
```

## 核心功能模块

### 1. 用户管理模块
- **用户注册与登录**：支持普通用户注册和登录功能
- **用户信息维护**：用户可以查看和修改自己的信息
- **密码修改**：支持用户修改登录密码
- **分页查询**：支持用户信息的分页展示

### 2. 超级管理员管理模块
- **超级管理员登录**：支持超级管理员登录功能
- **超级管理员信息维护**：超级管理员可以管理自己的信息
- **密码修改**：支持超级管理员修改登录密码
- **用户管理**：超级管理员可以管理所有用户信息

### 3. 菜单管理模块
- **角色菜单**：根据用户角色动态加载相应菜单
- **菜单配置**：支持菜单的增删改查操作
- **权限控制**：基于角色的菜单访问控制

### 4. 文件管理模块
- **文件上传**：支持文件上传功能
- **文件下载**：支持文件下载功能
- **富文本编辑器**：集成wangEditor，支持图片上传

### 5. 安全认证模块
- **JWT Token**：基于JWT的无状态认证机制
- **Token验证**：自动验证Token的有效性
- **角色权限**：基于角色的权限控制
- **策略模式**：使用策略模式处理不同角色的登录逻辑

### 6. 审计日志模块
- **操作记录**：使用AOP记录关键操作日志
- **日志查询**：支持查询最近的操作日志
- **自动记录**：通过注解自动记录操作信息

### 7. 通知管理模块
- **发送通知**：支持向用户发送通知
- **获取通知**：用户可以获取自己的通知
- **标记已读**：支持将通知标记为已读
- **删除通知**：支持删除通知

### 8. 缓存优化模块
- **Redis集成**：集成Redis缓存优化系统性能
- **数据缓存**：对用户信息、菜单数据等高频访问数据进行缓存
- **缓存更新**：数据更新时自动清除相关缓存
- **缓存策略**：实现合理的缓存过期策略

---

## 设计模式与架构特色

### 1. 策略模式
项目使用策略模式处理不同角色的登录和密码更新逻辑：
- **RoleStrategy**：定义角色策略接口
- **AdminStrategy**：超级管理员角色策略实现
- **UserStrategy**：普通用户角色策略实现
- **RoleStrategyContext**：策略上下文，根据角色动态选择策略

### 2. AOP面向切面编程
- **审计日志**：使用AOP自动记录操作日志
- **@AuditLogRecord**：自定义注解标记需要记录日志的方法
- **AuditLogAspect**：切面类处理日志记录逻辑

### 3. 分层架构
- **Controller层**：处理HTTP请求，参数验证
- **Service层**：核心业务逻辑处理
- **Mapper层**：数据访问层，与数据库交互
- **Entity层**：数据模型定义

### 4. 缓存架构
- **Redis配置**：集成Redis作为分布式缓存
- **缓存工具类**：提供统一的缓存操作接口
- **缓存策略**：在业务层实现数据缓存和更新

---

## 数据库设计

主要表包括：

- `user`：用户信息表（用户名、密码、角色、姓名、头像等）
- `admin`：超级管理员信息表（用户名、密码、角色、姓名、头像等）
- `audit_log`：操作日志表（用户名、操作、资源、IP地址、详情等）
- `notification`：通知表（通知内容、接收者、状态等）
- `menu`：菜单表（菜单名称、路径、父菜单、权限等）

> SQL脚本位于：`sql/intensifiedmycode.sql`

---

## 快速开始

### 环境准备

1. **Java 17+**：确保已安装Java 17或更高版本
2. **Node.js 18+**：确保已安装Node.js 18或更高版本
3. **MySQL 5.7+**：确保已安装并启动MySQL数据库
4. **Redis**：确保已安装并启动Redis服务

### 后端启动步骤

1. 进入[springboot](file://E:\java\maven_project\IntensifiedMyCode\springboot\src\main\java\com\example\SpringbootApplication.java#L9-L16)目录
2. 修改[application.yml](file://E:\java\maven_project\IntensifiedMyCode\springboot\src\main\resources\application.yml#L1-L28)中的数据库连接信息：
   ```yaml
   spring:
     datasource:
       driver-class-name: com.mysql.cj.jdbc.Driver
       username: root          # 修改为您的数据库用户名
       password: admin         # 修改为您的数据库密码
       url: jdbc:mysql://localhost:3307/intensifiedmycode?useUnicode=true&characterEncoding=utf-8&allowMultiQueries=true&useSSL=false&serverTimezone=GMT%2b8&allowPublicKeyRetrieval=true
   ```
3. 配置Redis连接信息（默认配置）：
   ```yaml
   spring:
     data:
       redis:
         host: localhost
         port: 6379
         password: 
         database: 0
   ```
4. 执行SQL脚本创建数据库表结构（位于sql目录）
5. 使用Maven编译项目：`mvn clean install`
6. 运行[SpringbootApplication.java](file://E:\java\maven_project\IntensifiedMyCode\springboot\src\main\java\com\example\SpringbootApplication.java#L9-L16)启动应用
7. 后端服务将在 http://localhost:9991 启动

### 前端启动步骤

1. 进入[vue](file://E:\java\maven_project\IntensifiedMyCode\vue\src\main.js#L1-L29)目录
2. 安装依赖：`npm install`
3. 修改[src/utils/request.js](file://E:\java\maven_project\IntensifiedMyCode\vue\src\utils\request.js#L1-L49)中的后端API地址：
   ```javascript
   let baseURL = 'http://127.0.0.1:9991';  // 确保与后端端口一致
   ```
4. 启动开发服务器：`npm run serve` 或 `npm run dev`
5. 前端应用将在 http://localhost:5173 启动（Vite默认端口）

### 默认账户

- **超级管理员账号**：admin / 123456
- **普通用户账号**：user / 123456

---

## 项目特性

### 1. 安全性
- JWT Token认证，保障接口安全
- 密码MD5加密存储（管理员）
- 自定义认证过滤器，验证请求合法性
- 角色权限控制，防止越权访问

### 2. 性能优化
- Redis缓存集成，优化高频数据访问性能
- 缓存策略设计，提升系统响应速度
- 数据库查询优化，减少不必要的数据库操作

### 3. 可扩展性
- 策略模式设计，易于扩展新角色
- 分层架构，各层职责明确
- 配置化管理，便于环境切换

### 4. 易用性
- 响应式设计，适配不同屏幕尺寸
- 主题切换，支持日间/夜间模式
- 完善的表单验证和错误提示
- 直观的用户界面设计

### 5. 可维护性
- 统一的代码风格和命名规范
- 完整的注释和文档
- 模块化设计，降低耦合度
- 标准化的API接口设计

---

## API接口说明

项目集成了Swagger文档，启动后端服务后可访问 http://localhost:9991/swagger-ui.html 查看完整的API文档。

### 主要接口分类：
- `/user/**`：用户相关接口
- `/admin/**`：管理员相关接口
- `/menu/**`：菜单相关接口
- `/files/**`：文件相关接口
- `/notification/**`：通知相关接口
- `/api/audit/**`：审计日志相关接口
- `/login`、`/register`、`/updatePassword`：认证相关接口

---

## 部署说明

### 生产环境部署

1. **后端部署**：
    - 打包：`mvn clean package`
    - 运行：`java -jar target/springboot-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod`

2. **前端部署**：
    - 构建：`npm run build`
    - 将dist目录下的文件部署到Web服务器（如Nginx）

### 多环境配置
项目支持开发环境(dev)、测试环境(test)和生产环境(prod)三种环境配置：

- **开发环境** (默认): 使用本地数据库和Redis，日志级别为debug，便于调试
- **测试环境**: 使用测试服务器的数据库和Redis，日志级别为info
- **生产环境**: 使用生产服务器的数据库和Redis，日志级别为warn，输出到文件，安全性更高

#### 激活不同环境的方法：

1. **开发环境** (默认):
   ```bash
   mvn spring-boot:run
   # 或者
   mvn clean package -Pdev
   java -jar target/springboot-0.0.1-SNAPSHOT.jar
   ```

2. **测试环境**:
   ```bash
   mvn clean package -Ptest
   java -jar target/springboot-0.0.1-SNAPSHOT.jar
   # 或者直接运行
   java -jar target/springboot-0.0.1-SNAPSHOT.jar --spring.profiles.active=test
   ```

3. **生产环境**:
   ```bash
   mvn clean package -Pprod
   java -jar target/springboot-0.0.1-SNAPSHOT.jar
   # 或者直接运行
   java -jar target/springboot-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
   ```

生产环境支持通过环境变量设置密码:
- `PROD_DB_PASSWORD`: 生产环境数据库密码
- `PROD_REDIS_PASSWORD`: 生产环境Redis密码

---

## 贡献指南

欢迎提交Issue和Pull Request来帮助改进项目。

### 代码规范
- Java代码遵循阿里巴巴Java开发手册规范
- 前端代码使用ESLint和Prettier进行代码格式化
- 提交信息使用约定式提交规范

### 开发流程
1. Fork项目
2. 创建功能分支
3. 提交更改
4. 发起Pull Request

---

## 许可证

本项目采用 MIT 许可证，详情请参阅 [LICENSE](LICENSE) 文件。

---

## 致谢

感谢以下开源项目的支持：
- Spring Boot
- Vue.js
- Element Plus
- MyBatis
- JWT
- AOP
- Lombok
- Hutool
- PageHelper
- Redis