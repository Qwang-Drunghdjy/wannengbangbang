# AGENTS.md - 万能帮帮

## 🎯 项目概述

"万能帮帮"是一个基于 **Spring Boot 3** 的失物招领 Web 应用。用户可以注册/登录，发布 **捡到的物品**（供失主认领）和 **丢失的物品**（寻物启事），系统提供基于 HanLP 中文分词 + Jaccard 相似度的 **智能匹配**。

- **后端**: Java 17, Spring Boot 3.4.5, Spring Data JPA, MySQL 8, Maven
- **认证**: JWT（jjwt 0.12.x, 7天过期）+ BCrypt 密码哈希, 轻量拦截器（非 Spring Security）
- **分词**: HanLP portable-1.8.4
- **部署**: 微信云托管 / 前端 Vue.js 移动端（待开发）

## 🛠️ 快速命令

| 场景 | 命令 |
| ------ | ------ |
| 编译 | `cd backend && mvn clean compile` |
| 测试 | `cd backend && mvn test` |
| 打包 | `cd backend && mvn clean package` |
| 本地启动 | `cd backend && mvn spring-boot:run` |

关键环境变量：`MYSQL_ADDRESS`（默认 `localhost:3306`）、`MYSQL_USERNAME`、`MYSQL_PASSWORD`、`JWT_SECRET`（生产必换）。

## 📁 后端架构

```
backend/src/main/java/com/uang/backend/
    config/         # JWT 工具类 + 认证拦截器
    controller/     # REST 控制器 + GlobalExceptionHandler
    dto/            # Result<T>、MatchResult、LoginRequest 等
    entity/         # User / LostItem / FindItem
    repository/     # Spring Data JPA 仓库
    service/        # 业务逻辑（含 MatchingService）
src/main/resources/
    application.yml # 数据源 & JPA & JWT 配置
src/test/java/      # MockMvc / Mockito 单元测试
```

## 🗄️ 核心数据模型

三张表：`user`（用户）、`lost_item`（捡到的物品）、`find_item`（丢失的物品）。`lost_item` 与 `find_item` 字段一一对应，均通过 `user_id` 外键关联发布者。完整字段定义见 [`docs/data-model.md`](docs/data-model.md)。

关键约束：

- **User**: `phone` 唯一，`password` 存 BCrypt 哈希，`status` 为 `NORMAL`/`BANNED`
- **LostItem** / **FindItem**: `title`/`image_url`(仅 LostItem) 必填，`contact` 可选（默认取用户手机号），`create_time` 由 Service 赋值

## 🔌 API 设计规范

- 统一返回 `Result<T>`: `{ "code": 200, "message": "success", "data": … }`
- 异常由 `GlobalExceptionHandler` 统一转为 `Result.error(500, msg)`
- 分页使用 `Pageable`（`page`, `size`, `sort`）
- 需认证接口携带 `Authorization: Bearer <token>`
- 权限规则：**GET 公开，POST 需登录，auth 公开**

| 方法 | 路径 | 认证 | 说明 |
| ------ | ------ | ------ | ------ |
| `POST` | `/api/v1/auth/register` | ❌ | 注册 `{ phone, password, nickname }` |
| `POST` | `/api/v1/auth/login` | ❌ | 登录 → `{ token, userId, nickname }` |
| `POST` | `/api/v1/lost-items` | ✅ | 发布捡到物品 |
| `GET` | `/api/v1/lost-items` | ❌ | 分页列表 `?title=&page=&size=` |
| `GET` | `/api/v1/lost-items/{id}` | ❌ | 失物详情 |
| `POST` | `/api/v1/find-items` | ✅ | 发布寻物启事 |
| `GET` | `/api/v1/find-items` | ❌ | 分页列表 `?title=&page=&size=` |
| `GET` | `/api/v1/find-items/{id}` | ❌ | 寻物详情 |
| `GET` | `/api/v1/find-items/{id}/matches?limit=3` | ❌ | 智能匹配 |

## 🔐 认证方案

- **JWT**: `io.jsonwebtoken:jjwt-api:0.12.6`，密钥来自 `${JWT_SECRET}`，7 天过期，payload 含 `sub`(userId) + `phone`
- **密码**: Spring Security Crypto 的 `BCryptPasswordEncoder`（仅 crypto 模块，不引入全套 Security）
- **拦截**: `HandlerInterceptor` → 解析 JWT → 注入 userId 到 request attribute

## 🔍 智能匹配

```
score = 0.6 × Jaccard(title) + 0.3 × Jaccard(description) + 0.1 × Jaccard(location)
```

- 分词：`HanLP.segment(text)` → `Set<String>`
- 空字段：任一方为空 → 该字段相似度记为 0
- 方法：`tokenize` → `jaccard` → `fieldSimilarity` → `calculateScore` → `findMatches`（入口）
- 当前实时计算、无缓存，方法粒度已拆细便于后续加 `@Cacheable`

## ⚙️ 编码规范

- Controller → Service → Repository 分层，禁止手写 SQL
- Lombok（`@Data`/`@NoArgsConstructor`/`@AllArgsConstructor`），所有 public 方法写 Javadoc
- 密码永远 BCrypt 存储，JWT 密钥从环境变量读取
- 精确修改：只改任务相关代码，不顺手重构无关内容
- 发布物品时关联当前登录用户（从 JWT 提取 userId）
- 计划完成后分步执行，每步等用户确认

## 🧪 本地验证

```bash
# 注册
curl -X POST :8080/api/v1/auth/register -H "Content-Type: application/json" \
  -d '{"phone":"13800001111","password":"123456","nickname":"张三"}'

# 登录 → 获取 token
curl -X POST :8080/api/v1/auth/login -H "Content-Type: application/json" \
  -d '{"phone":"13800001111","password":"123456"}'

# 发布失物（替换 TOKEN）
curl -X POST :8080/api/v1/lost-items -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"title":"黑色钱包","description":"在图书馆捡到","location":"图书馆","image_url":"https://..."}'
```

## 📋 质量检查

| 检查项 | 命令 | 标准 |
| ------ | ------ | ------ |
| 编译 | `mvn clean compile` | BUILD SUCCESS |
| 测试 | `mvn test` | 0 failures, 0 errors |

## 📖 文档导航

| 文档 | 说明 |
| ------ | ------ |
| `AGENTS.md` | 项目心智模型 + 全局约定（本文档） |
| `docs/data-model.md` | 完整的数据库表字段定义 |
| `plans/lost-item.md` | LostItem + FindItem CRUD 开发记录 |
| `plans/find-item.md` | FindItem 审批计划 |
| `plans/match.md` | 智能匹配审批计划 |
