# 项目说明文档
# 强化的脚手架
## 一、项目概述

本项目是一个基于Spring Boot后端框架与Vue前端框架的管理系统。

- 后端：Spring Boot + MyBatis + MySQL
- 前端：Vue3 + Vite + Element Plus
- 认证：JWT Token认证
- 日志：通过AOP实现操作日志记录

## 二、目录结构

```bash
.
├── springboot        # Spring Boot 后端项目
│   ├── src           # 源码目录
│   │   └── main      # 主程序目录
│   │       ├── java  # Java源代码
│   │       └── resources # 配置和资源文件
│   └── pom.xml       # Maven项目配置文件
├── vue               # Vue 前端项目
│   ├── src           # 源码目录
│   ├── index.html    # 入口HTML文件
│   └── package.json  # npm包配置文件
├── sql               # SQL脚本目录
└── README.md         # 项目说明文档
```

## 三、系统功能简介

### 用户管理

- 用户注册与登录
- 用户信息维护
- 密码修改

### 管理员功能

- 用户管理
- 审计日志管理
- 通知公告管理

### 系统功能

- 文件上传与下载
- JWT Token认证机制
- AOP实现的操作日志记录

## 四、技术栈详解

### 后端技术栈

- **Spring Boot**：简化Spring应用的初始搭建以及开发
- **MyBatis**：优秀的持久层框架，支持定制化SQL、存储过程以及高级映射
- **MySQL**：轻量级数据库，用于数据存储
- **JWT**：用于用户认证，提供安全的API访问
- **AOP**：面向切面编程，用于实现操作日志记录

### 前端技术栈

- **Vue3**：渐进式JavaScript框架，构建用户界面
- **Vite**：新型前端构建工具，提供快速的冷启动和即时模块热更新
- **Element Plus**：基于Vue 3的组件库，提供丰富的UI组件

## 五、快速开始

### 后端启动步骤

1. 安装JDK 17+
2. 使用IDE导入springboot目录作为Maven项目
3. 执行SQL脚本（位于sql目录）创建数据库
4. 修改application.yml配置数据库连接信息
5. 运行SpringbootApplication.java启动应用

### 前端启动步骤

1. 安装Node.js 16+
2. 进入vue目录，执行`npm install`安装依赖
3. 执行`npm run dev`启动开发服务器
4. 浏览器访问`http://localhost:3000`查看应用


