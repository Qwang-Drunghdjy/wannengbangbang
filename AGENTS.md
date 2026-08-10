# AGENTS.md - 万能帮帮

## 🎯 项目概述
这是一个基于 **Spring Boot 3** 的失物招领 Web 应用后端。项目采用 **前后端分离** 架构，前端计划使用 Vue.js 构建移动端优先的响应式页面。初步功能是允许用户上传拾物照片和相关信息，并存储到 MySQL 数据库中。

## 🛠️ 技术栈
- **后端**: Java 17, Spring Boot 3, Spring Data JPA, MySQL 8, Maven
- **部署**: 微信云托管
- **前端**: Vue.js

## 📁 推荐的项目结构 (Monorepo)
```
wannengbangbang/   (Gitee 仓库根目录)
├── backend/          # Spring Boot 后端代码 (本项目的根目录)
│   ├── src/
│   ├── pom.xml
│   └── Dockerfile    # (可选) 微信云托管部署用
├── frontend/         # (未来) 前端代码 (Vue/React)
└── AGENTS.md         # 本文件
```

## 🗄️ 核心数据模型 (初步)
- **失物表 (`lost_item`)**: 核心数据表。
    - `id`: 主键
    - `title`: 物品名称 (String)
    - `description`: 物品描述 (String)
    - `location`: 拾获地点 (String)
    - `contact`: 联系方式 (String)
    - `image_url`: 图片存储的 URL (String)
    - `create_time`: 发布时间 (Timestamp)

## 🔌 API 设计规范
- 采用 **RESTful** 风格设计接口。
- 基础路径: `/api/v1/lost-items`
- 核心接口示例:
    - `POST /api/v1/lost-items`: 发布新的失物信息。
    - `GET /api/v1/lost-items`: 获取失物列表（支持分页和筛选）。
    - `GET /api/v1/lost-items/{id}`: 获取特定失物的详细信息。

## 🧪 构建与测试命令
- **构建项目**: `mvn clean package`
- **本地运行**: `mvn spring-boot:run`

## ⚙️ 编码与协作规范
- 遵循标准的 Spring Boot 项目结构 (Controller, Service, Repository)。
- 使用 **Lombok** 简化 POJO 类。
- 所有 API 接口需编写清晰的文档注释。

## 🌐 部署与环境 (微信云托管)
- 平台要求通过环境变量配置数据库连接。
- 关键环境变量: `MYSQL_ADDRESS`, `MYSQL_USERNAME`, `MYSQL_PASSWORD`。
- 部署时可参考微信云托管官方 Spring Boot 模板。

## 🧩 给 AI 的特别指引
- 所有生成的代码需符合上述技术栈和规范。
- 数据库交互请使用 Spring Data JPA。
- API 设计需考虑移动端访问，返回数据格式建议使用 JSON。
- 图片上传功能需考虑与微信云托管对象存储服务集成。