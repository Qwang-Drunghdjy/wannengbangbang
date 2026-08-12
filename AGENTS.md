# AGENTS.md - 万能帮帮

## 🎯 项目概述

"万能帮帮"是一个失物招领 Web 应用：用户注册/登录后发布 **拾物招领**（捡到的物品，供失主认领）与 **寻物启事**（丢失的物品），系统基于 HanLP 中文分词 + 加权 Jaccard 相似度为寻物启事推荐匹配的拾物。后端 Java 17 + Spring Boot 3.4.5 + JPA + MySQL 8；前端 Vue 3 + TypeScript + Tailwind（规范见 [docs/frontend-development.md](docs/frontend-development.md)）。

- **后端**: Java 17, Spring Boot 3.4.5, Spring Data JPA, MySQL 8, Maven
- **认证**: JWT（jjwt 0.12.6，7 天过期）+ BCrypt 密码哈希，轻量 HandlerInterceptor（非 Spring Security）
- **匹配**: HanLP portable-1.8.4 中文分词 + 加权 Jaccard 相似度
- **部署**: 微信云托管 / 前端按 v1.1 规范开发中

仓库结构：

```
backend/        # Spring Boot 后端
docs/           # 设计文档（数据模型、前端规范）
plans/          # 分步开发计划与记录
```

## 🛠️ 快速命令

| 场景 | 命令 |
| ------ | ------ |
| 编译 | `cd backend && mvn clean compile` |
| 测试 | `cd backend && mvn test` |
| 打包 | `cd backend && mvn clean package` |
| 本地启动 | `cd backend && mvn spring-boot:run` |

环境变量（由 application.yml 读取，均可省略用默认值）：

| 变量 | 默认值 | 说明 |
| ------ | ------ | ------ |
| `MYSQL_ADDRESS` | `localhost:3306` | MySQL 地址 |
| `MYSQL_USERNAME` | `root` | MySQL 用户 |
| `MYSQL_PASSWORD` | `root` | MySQL 密码 |
| `JWT_SECRET` | 开发默认（≥32 字符） | JWT 密钥，**生产必换** |

## 📁 后端架构

```
backend/src/main/java/com/uang/backend/
    config/         # JwtUtil + AuthInterceptor + WebMvcConfig（认证）
    controller/     # REST 控制器 + GlobalExceptionHandler
    dto/            # Result<T>、MatchResult、Login/RegisterRequest 等
    entity/         # User / LostItem / FindItem
    repository/     # Spring Data JPA 仓库
    service/        # UserService / LostItemService / FindItemService / MatchingService
src/main/resources/
    application.yml # 数据源 & JPA & JWT 配置
src/test/java/      # MockMvc / Mockito 单元测试
```

核心子系统：**认证**（JWT + BCrypt + 拦截器，见 🔐 认证方案）、**物品发布**（关联登录用户，contact 默认取手机号）、**智能匹配**（HanLP + Jaccard，见 🔍 智能匹配）。

### 前后端术语映射（重要 ⚠️）

> 后端实体命名与直觉相反：**`LostItem` = 拾获物品（拾物招领）**，**`FindItem` = 丢失物品（寻物启事）**。前端一律使用 `seek / claim`，禁止按名称猜测端点。

| 后端表 / 实体 | 含义 | 前端 category | 端点 |
| ------ | ------ | ------ | ------ |
| `lost_item` / `LostItem` | 拾获物品（拾物招领） | `'claim'` | `/api/v1/lost-items` |
| `find_item` / `FindItem` | 丢失物品（寻物启事） | `'seek'` | `/api/v1/find-items` |

## 🖥️ 前端架构（规划中）

- **技术栈**: Vue 3 + TypeScript + Tailwind CSS（或等效 UI 库）
- **路由**: 一级 tab（首页 / 消息 / 我的）+ 二级页（发布 / 详情 / 匹配结果）+ 登录 / 注册
- **API 层**: 统一请求封装 → 解包 `Result<T>`、自动带 `Authorization: Bearer <token>`、401 跳登录
- **状态**: Pinia / Context；token 存 localStorage
- 完整页面与交互规范见 [docs/frontend-development.md](docs/frontend-development.md)（v1.1）

## 🗄️ 核心数据模型

三张表：`user`、`lost_item`（拾物招领）、`find_item`（寻物启事），后两者通过 `user_id` 外键关联发布者。完整字段定义见 [docs/data-model.md](docs/data-model.md)。

- **User**: `phone` 唯一，`password` 存 BCrypt 哈希，`status` = `NORMAL`/`BANNED`
- **LostItem**: `title` / `image_url` 必填；**FindItem**: `title` 必填，`image_url` 可选
- **LostItem / FindItem**: `contact` 可选（默认取发布者手机号），`create_time` 由 Service 赋值

## 🔌 API 设计规范

- 统一返回 `Result<T>`: `{ "code": 200, "message": "success", "data": … }`；异常由 `GlobalExceptionHandler` 统一转 `Result.error(500, msg)`
- 分页使用 `Pageable`（`page`, `size`, `sort`）
- 权限规则：**GET 公开，POST 需登录（Bearer token），`/api/v1/auth` 公开**
- 认证失败：拦截器返回 **HTTP 401** + `Result.error(401, "未登录或登录已过期")`

| 方法 | 路径 | 认证 | 说明 |
| ------ | ------ | ------ | ------ |
| `POST` | `/api/v1/auth/register` | ❌ | 注册 `{ phone, password, nickname }` |
| `POST` | `/api/v1/auth/login` | ❌ | 登录 → `{ token, userId, nickname }` |
| `POST` | `/api/v1/lost-items` | ✅ | 发布拾物（招领，image_url 必填） |
| `GET` | `/api/v1/lost-items` | ❌ | 拾物列表 `?title=&page=&size=` |
| `GET` | `/api/v1/lost-items/{id}` | ❌ | 拾物详情 |
| `POST` | `/api/v1/find-items` | ✅ | 发布寻物启事 |
| `GET` | `/api/v1/find-items` | ❌ | 寻物列表 `?title=&page=&size=` |
| `GET` | `/api/v1/find-items/{id}` | ❌ | 寻物详情 |
| `GET` | `/api/v1/find-items/{id}/matches?limit=3` | ❌ | 智能匹配（寻物 → 拾物） |
| `GET` | `/health` | ❌ | 健康检查（微信云托管） |

## 🔐 认证方案

- **JWT**: jjwt 0.12.6，密钥 `${JWT_SECRET}`（HS256 需 ≥32 字符），7 天过期，payload 含 `sub`(userId) + `phone`
- **密码**: `BCryptPasswordEncoder`（spring-security-crypto，不引入全套 Security）
- **拦截**: `AuthInterceptor` 解析 JWT → 注入 `request.setAttribute("userId", …)`；`WebMvcConfig` 拦截 `/api/v1/**` 的 POST，放行 GET 与 `/api/v1/auth/**`

## 🔍 智能匹配

```
score = 0.6 × Jaccard(title) + 0.3 × Jaccard(description) + 0.1 × Jaccard(location)
```

- 分词：`HanLP.segment(text)` → `Set<String>`；空字段相似度记为 0
- 方法链：`tokenize` → `jaccard` → `fieldSimilarity` → `calculateScore` → `findMatches`
- 返回 `score` 为 0.0 ~ 1.0，前端展示 ×100 取整；实时计算无缓存，方法粒度已拆细便于后续加 `@Cacheable`

## ⚙️ 关键约定

1. Controller → Service → Repository 分层，禁止手写 SQL
2. Lombok（`@Data`/`@NoArgsConstructor`/`@AllArgsConstructor`），所有 public 方法写 Javadoc
3. 密码永远 BCrypt 存储；JWT 密钥从环境变量读取
4. 精确修改：只改任务相关代码，不顺手重构无关内容
5. 发布物品时从 JWT 提取 userId 关联发布者（[data-model.md](docs/data-model.md)）
6. 前端发布类型一律 `seek`/`claim`，严禁直连后端 `lost/find` 命名（[frontend-development.md](docs/frontend-development.md) 5.1 节）
7. 计划完成后分步执行，每步等用户确认

## 🧪 本地开发及验证流程

闭环：**改代码 → `mvn clean compile` → `mvn spring-boot:run` → curl 验证 → `mvn test`**

```bash
# 1. 注册（公开）
curl -X POST :8080/api/v1/auth/register -H "Content-Type: application/json" \
  -d '{"phone":"13800001111","password":"123456","nickname":"张三"}'

# 2. 登录（公开）→ 取 TOKEN；token 缺失/过期时发布接口返回 401
curl -X POST :8080/api/v1/auth/login -H "Content-Type: application/json" \
  -d '{"phone":"13800001111","password":"123456"}'

# 3. 发布拾物（需带 token）
curl -X POST :8080/api/v1/lost-items -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"title":"黑色钱包","description":"在图书馆捡到","location":"图书馆","image_url":"https://..."}'
```

日志：Spring Boot 默认输出到控制台（application.yml 未配置文件日志）。

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
| `docs/frontend-development.md` | 前端开发规范 v1.1（页面 / 交互 / 数据模型 / 组件） |
| `plans/lost-item.md` | LostItem + FindItem CRUD 开发记录 |
| `plans/find-item.md` | FindItem 审批计划 |
| `plans/match.md` | 智能匹配审批计划 |
| `plans/frontend.md` | 前端脚手架搭建计划 |
