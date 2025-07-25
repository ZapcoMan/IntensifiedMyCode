# 项目说明文档

## 强化的脚手架

## 目录

- [一、项目概述](#一项目概述)
- [二、目录结构](#二目录结构)
- [三、系统功能简介](#三系统功能简介)
- [四、后端模块说明](#四后端模块说明)
- [五、前端模块说明](#五前端模块说明)
- [六、数据库设计](#六数据库设计)
- [七、快速开始](#七快速开始)

## 一、项目概述

> 本项目是一个基于Spring Boot后端框架与Vue前端框架的现代化管理系统，采用前后端分离架构，适用于快速构建企业级应用。

- **后端**：Spring Boot 3.x + MyBatis Plus + MySQL
- **前端**：Vue3 + Vite + Element Plus
- **认证**：JWT Token认证 + Redis会话管理
- **日志**：通过AOP实现操作日志记录
- **其他**：集成Swagger API文档，支持跨域访问

### 技术栈

| 类型   | 技术/框架                          |
|------|--------------------------------|
| 后端   | Spring Boot 3.x / MyBatis Plus |
| 数据库  | MySQL / Redis                  |
| 安全认证 | JWT / Spring Security          |
| 前端   | Vue 3 / Vite / Element Plus    |
| 工具   | Swagger（API文档）、AOP（日志记录）       |

---

## 二、目录结构

```bash
.
├── springboot        # Spring Boot 后端项目
│   ├── src           # 源码目录
│   │   └── main      # 主程序目录
│   │       ├── java  # Java源代码
│   │       │   └── com
│   │       │       └── example
│   │       │           ├── annotation    # 自定义注解
│   │       │           ├── aspect        # AOP切面
│   │       │           ├── common        # 公共类
│   │       │           ├── config        # 配置类
│   │       │           ├── controller    # 控制器
│   │       │           ├── entity        # 实体类
│   │       │           ├── exception     # 异常处理
│   │       │           ├── mapper        # 数据访问层
│   │       │           ├── security      # 安全相关
│   │       │           ├── service       # 服务接口及实现
│   │       │           ├── utils         # 工具类
│   │       │           └── SpringbootApplication.java # 应用启动类
│   │       └── resources # 配置和资源文件
│   │           ├── mapper    # MyBatis映射文件
│   │           └── application.yml # 配置文件
│   └── pom.xml       # Maven项目配置文件
├── vue               # Vue 前端项目
│   ├── src           # 源码目录
│   │   ├── api       # 接口调用
│   │   ├── assets    # 静态资源
│   │   ├── router    # 路由配置
│   │   ├── utils     # 工具类
│   │   ├── views     # 页面组件
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

## 三、系统功能简介

### 用户管理

- 用户注册与登录（支持邮箱/手机号）
- 用户信息维护（头像、昵称、联系方式等）
- 密码修改与找回
- 角色权限分配

### 管理员功能

- 用户管理（增删改查、状态管理）
- 审计日志管理（操作记录追踪）
- 通知公告管理（发布、编辑、删除）
- 系统参数配置

### 系统功能

- 文件上传与下载（支持本地存储和OSS对象存储）
- JWT Token认证机制（带自动刷新令牌功能）
- AOP实现的操作日志记录
- RESTful API设计规范
- 多环境配置管理（开发、测试、生产）

## 三、核心功能模块

### 用户管理

- 注册、登录、修改密码
- 权限控制（用户/管理员）
- 账户状态管理（启用/禁用）

### 管理员功能

- 用户管理
- 日志审计查看
- 通知公告发布与维护
- 系统参数设置

### 系统功能

- 文件上传下载（本地/OSS）
- JWT Token自动刷新机制
- AOP实现操作日志记录
- RESTful API规范设计
- 多环境配置支持（dev/test/prod）

---

## 四、后端模块说明

| 包名                                                                                                                                     | 功能描述             |
|----------------------------------------------------------------------------------------------------------------------------------------|------------------|
| `controller`                                                                                                                           | 接收 HTTP 请求，处理路由  |
| `service`                                                                                                                              | 核心业务逻辑           |
| `mapper`                                                                                                                               | 数据库访问接口（MyBatis） |
| `entity`                                                                                                                               | 数据库实体类           |
| `dto`                                                                                                                                  | 数据传输对象           |
| `aspect`                                                                                                                               | 切面编程，用于日志记录      |
| [config](file://C:\Users\Administrator\Desktop\IntensifiedMyCode\vue\node_modules\unplugin-vue-components\dist\resolvers.js#L636-L636) | 系统配置类            |
| `security`                                                                                                                             | 安全相关（JWT、权限验证等）  |
| `exception`                                                                                                                            | 统一异常处理           |
| `utils`                                                                                                                                | 工具类（TokenUtils等） |

---

## 五、前端模块说明

| 目录                                                                                               | 功能描述                        |
|--------------------------------------------------------------------------------------------------|-----------------------------|
| `api`                                                                                            | 封装所有后端接口请求                  |
| `views`                                                                                          | 页面组件（如 Login.vue, Home.vue） |
| [router](file://C:\Users\Administrator\Desktop\IntensifiedMyCode\vue\src\router\index.js#L2-L20) | Vue Router 路由配置             |
| `assets`                                                                                         | 静态资源（CSS、图标等）               |
| `utils`                                                                                          | 工具函数（如 request.js）          |
| `components`                                                                                     | 可复用的 UI 组件                  |

---

## 六、数据库设计

主要表包括：

- `user`：用户信息
- `admin`：管理员信息
- `audit_log`：操作日志
- `notification`：系统通知
- [file](file://cn\hutool\core\io\FileUtil.java#L55-L55)：上传文件记录

> SQL脚本位于：`sql/intensifiedmycode.sql`

---



## 七、快速开始

### 后端启动步骤

1. 安装JDK 17+
2. 使用IDE导入springboot目录作为Maven项目
3. 执行SQL脚本（位于sql目录）创建数据库
4. 修改application.yml配置数据库连接信息、Redis配置等
5. 运行SpringbootApplication.java启动应用
6. 访问http://localhost:8080/swagger-ui.html 查看API文档

### 前端启动步骤

1. 安装Node.js 18+
2. 进入vue目录，执行`npm install`安装依赖
3. 执行`npm run dev`启动开发服务器
4. 浏览器访问`http://localhost:3000`查看应用
5. 默认登录账号：admin/123456