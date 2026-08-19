

# 万能帮帮 - 失物招领与智能匹配平台

## 📋 项目简介

万能帮帮是一个基于 **Spring Boot + Vue 3** 的失物招领与智能匹配平台，支持用户发布丢失物品和拾取物品，并通过 AI 技术自动生成物品描述，实现智能匹配与推荐功能。

### 核心功能

- **用户认证**：注册、登录、JWT 令牌鉴权
- **失物招领**：发布/浏览丢失物品和拾取物品信息
- **AI 描述生成**：基于智谱 AI (GLM) 自动识别物品图片并生成标题和描述
- **智能匹配**：基于物品标题、描述、地点等多维度信息进行相似度匹配
- **消息通知**：实时推送匹配结果
- **个人中心**：管理个人发布记录

---

## 🛠 技术栈

### 后端

| 技术 | 用途 |
|------|------|
| **Spring Boot 3.x** | 应用框架 |
| **Spring Data JPA** | 数据持久化 |
| **MySQL** | 主数据库 |
| **JWT** | 用户认证 |
| **GLM (智谱 AI)** | 图片描述生成 |

### 前端

| 技术 | 用途 |
|------|------|
| **Vue 3** | UI 框架 |
| **TypeScript** | 类型安全 |
| **Vite** | 构建工具 |
| **Pinia** | 状态管理 |
| **Vue Router** | 路由管理 |
| **Axios** | HTTP 客户端 |

---

## 📁 项目结构

```
├── backend/                 # 后端 Spring Boot 项目
│   ├── src/main/java/com/uang/backend/
│   │   ├── config/         # 配置类（JWT、拦截器等）
│   │   ├── controller/     # REST API 控制器
│   │   ├── dto/            # 数据传输对象
│   │   ├── entity/         # 实体类
│   │   ├── repository/     # 数据仓库
│   │   ├── service/        # 业务逻辑层
│   │   ├── client/         # AI 客户端
│   │   └── exception/      # 自定义异常
│   └── src/main/resources/
│       └── application.yml # 配置文件
│
├── frontend/               # 前端 Vue 3 项目
│   ├── src/
│   │   ├── api/           # API 接口定义
│   │   ├── components/    # 公共组件
│   │   ├── views/         # 页面视图
│   │   ├── stores/        # Pinia 状态管理
│   │   ├── router/        # 路由配置
│   │   └── utils/         # 工具函数
│   └── nginx.conf         # Nginx 配置
│
├── docs/                   # 文档目录
│   ├── development.md     # 开发手册
│   └── data-model.md      # 数据模型说明
│
└── plans/                  # 开发计划文档
```

---

## 🚀 快速开始

### 环境要求

- **后端**：JDK 17+, Maven 3.9+, MySQL 8.0+
- **前端**：Node.js 18+, npm/yarn/pnpm

### 后端配置

1. **创建数据库**：
   ```sql
   CREATE DATABASE wannengbangbang DEFAULT CHARACTER SET utf8mb4;
   ```

2. **配置 application.yml**：
   ```yaml
   spring:
     datasource:
       url: jdbc:mysql://localhost:3306/wannengbangbang
       username: your_username
       password: your_password
   
   jwt:
     secret: your-jwt-secret-key
     expire-days: 7
   
   glm:
     api-key: your-glm-api-key
     rate-limit-per-minute: 5
   ```

3. **启动后端**：
   ```bash
   cd backend
   ./mvnw spring-boot:run
   ```

   后端将在 `http://localhost:8080` 启动。

### 前端配置

1. **安装依赖**：
   ```bash
   cd frontend
   npm install
   ```

2. **配置环境变量**（可选）：
   ```bash
   # .env.development
   VITE_API_BASE_URL=http://localhost:8080/api/v1
   ```

3. **启动开发服务器**：
   ```bash
   npm run dev
   ```

   前端将在 `http://localhost:5173` 启动。

---

## 📡 API 文档

### 认证接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/v1/auth/register` | 用户注册 |
| POST | `/api/v1/auth/login` | 用户登录 |

### 物品接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/lost-items` | 获取丢失物品列表 |
| POST | `/api/v1/lost-items` | 发布丢失物品（需认证） |
| GET | `/api/v1/lost-items/{id}` | 获取物品详情 |
| GET | `/api/v1/find-items` | 获取拾取物品列表 |
| POST | `/api/v1/find-items` | 发布拾取物品（需认证） |
| GET | `/api/v1/find-items/{id}` | 获取物品详情 |

### AI 接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/v1/ai/describe` | 图片描述生成（需认证） |

### 匹配接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/find-items/{id}/matches` | 查找与拾取物品匹配的丢失物品 |
| GET | `/api/v1/lost-items/{id}/matches` | 查找与丢失物品匹配的拾取物品 |

---

## 🔐 JWT 认证

所有需要认证的接口需在请求头中携带 Token：

```
Authorization: Bearer <your-jwt-token>
```

Token 将在登录成功后返回，有效期默认为 7 天。

---

## 🐳 Docker 部署

### 构建后端镜像

```bash
cd backend
docker build -t wannengbangbang-backend .
```

### 构建前端镜像

```bash
cd frontend
docker build -t wannengbangbang-frontend .
```

### 使用 Docker Compose

```yaml
version: '3.8'
services:
  backend:
    build: ./backend
    ports:
      - "8080:8080"
    environment:
      - SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/wannengbangbang
    depends_on:
      - mysql
  
  frontend:
    build: ./frontend
    ports:
      - "80:80"
  
  mysql:
    image: mysql:8.0
    environment:
      - MYSQL_ROOT_PASSWORD=rootpassword
      - MYSQL_DATABASE=wannengbangbang
```

---

## 📝 开发指南

### 代码规范

- 后端遵循 Java 命名规范，使用 Lombok 简化代码
- 前端使用 TypeScript，组件采用 Composition API
- 提交代码前请运行测试：`./mvnw test`（后端）

### 前后端术语映射

| 前端 | 后端 | 说明 |
|------|------|------|
| `lostItems` | `LostItem` | 丢失物品 |
| `findItems` | `FindItem` | 拾取物品 |
| `imageUrl` | `imageUrl` | 图片地址 |
| `contact` | `contact` | 联系方式 |

详见 [docs/frontend-development.md](docs/frontend-development.md)

---

## 🧪 测试

### 后端单元测试

```bash
cd backend
./mvnw test
```

测试覆盖：
- Controller 层测试（MockMvc）
- Service 层测试
- 匹配算法测试

---

## 📄 许可证

本项目仅供学习与研究使用。

---

## 📞 联系我们

如有问题或建议，欢迎提交 Issue 或 Pull Request。