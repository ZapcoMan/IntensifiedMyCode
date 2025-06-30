# 项目说明文档
# 强化的脚手架

## 一、项目概述

本项目是一个基于Spring Boot后端框架与Vue前端框架的现代化管理系统，采用前后端分离架构，适用于快速构建企业级应用。

- **后端**：Spring Boot 3.x + MyBatis Plus + MySQL
- **前端**：Vue3 + Vite + Element Plus
- **认证**：JWT Token认证 + Redis会话管理
- **日志**：通过AOP实现操作日志记录
- **其他**：集成Swagger API文档，支持跨域访问

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

## 四、技术栈详解

### 后端技术栈

- **Spring Boot 3.x**：基于最新Java特性，简化Spring应用的初始搭建以及开发
- **MyBatis Plus**：增强型持久层框架，提供丰富的CRUD操作和查询构造器
- **MySQL 8.x**：高性能关系型数据库，用于数据存储
- **JWT**：用于用户认证，提供安全的API访问
- **Redis**：缓存中间件，用于会话管理和热点数据缓存
- **AOP**：面向切面编程，用于实现操作日志记录和性能监控
- **Spring Security**：安全框架，提供认证授权功能
- **Swagger UI**：API文档自动生成工具
- **Lombok**：减少样板代码的编写
- **MapStruct**：对象映射工具，提高DO/DTO转换效率

### 前端技术栈

- **Vue3**：渐进式JavaScript框架，构建用户界面
- **Vite**：新型前端构建工具，提供快速的冷启动和即时模块热更新
- **Element Plus**：基于Vue 3的组件库，提供丰富的UI组件
- **Axios**：HTTP客户端，用于API请求管理
- **Pinia**：状态管理库，替代Vuex
- **Vue Router 4**：路由管理，支持懒加载和动态路由
- **SCSS**：CSS扩展语言，提供变量、嵌套等功能
- **Eslint**：代码规范工具，确保代码质量

## 五、快速开始

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