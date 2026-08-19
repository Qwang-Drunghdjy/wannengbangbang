# AGENTS.md - 万能帮帮

## 🎯 项目概述

"万能帮帮"是一个失物招领 Web 应用：用户注册/登录后发布 **拾物招领**（捡到的物品）与 **寻物启事**（丢失的物品），系统基于 HanLP 中文分词 + 加权 Jaccard 相似度推荐匹配，并用 GLM-4V-Flash（免费视觉模型）自动生成物品关键词描述加速发布。后端 Java 17 + Spring Boot 3.4.5 + JPA + MySQL 8；前端 Vue 3 + TypeScript + Tailwind。

```text
backend/        # Spring Boot 后端
docs/           # 设计文档（数据模型 / 前端规范 / 开发手册）
plans/          # 分步开发计划与记录
```

## 🛠️ 快速命令

| 场景 | 命令（Windows） |
| ------ | ------ |
| 后端编译 | `cd backend && .\mvnw.cmd clean compile` |
| 后端测试 | `cd backend && .\mvnw.cmd test` |
| 本地启动 | `cd backend && .\mvnw.cmd spring-boot:run` |
| 前端构建 / Lint | `cd frontend && npm run build` / `npm run lint` |
| 前端启动 | `cd frontend && npm run dev`（:5173，/api 代理到 :8080） |

> ⚠️ 本机未全局安装 Maven，一律用自带 `mvnw.cmd`；工具链、踩坑记录、curl 冒烟模板见 [docs/development.md](docs/development.md)。

环境变量（application.yml 读取，均可省略用默认值）：

| 变量 | 默认值 | 说明 |
| ------ | ------ | ------ |
| `MYSQL_ADDRESS` / `MYSQL_USERNAME` / `MYSQL_PASSWORD` | `localhost:3306` / `root` / `root` | MySQL 连接 |
| `JWT_SECRET` | 开发默认（≥32 字符） | JWT 密钥，**生产必换** |
| `GLM_API_KEY` | 空 | 智谱 GLM-4V-Flash Key（免费：open.bigmodel.cn）；为空时 AI 接口返回友好错误；**生产必填** |

## 📁 后端架构

```text
backend/src/main/java/com/uang/backend/
    config/     # JwtUtil + AuthInterceptor + WebMvcConfig（认证）
    controller/ # REST 控制器 + GlobalExceptionHandler（400/401/429/500）
    dto/        # Result<T>、MatchResult、Login/RegisterRequest、DescribeImage* 等
    entity/     # User / LostItem / FindItem
    exception/  # UnauthorizedException / RateLimitException
    repository/ # Spring Data JPA 仓库
    service/    # User / LostItem / FindItem / Matching / Ai / RateLimit Service
    client/     # GlmClient（GLM-4V-Flash 薄封装，换模型只改此处）
src/main/resources/application.yml  # 数据源 & JPA & JWT & glm 配置
```

核心子系统（实现细节见对应 plans/ 文档）：

- **认证**：JWT（jjwt 0.12.6，7 天）+ BCrypt + HandlerInterceptor；拦截 `/api/v1/**` 的 POST（需 token），GET 与 `/api/v1/auth/**` 公开，失败 HTTP 401
- **匹配**：`0.6×Jaccard(title) + 0.3×Jaccard(description) + 0.1×Jaccard(location)`（HanLP 分词，实时无缓存）
- **AI 描述生成**：前端压缩图 → `POST /api/v1/ai/describe`（需登录，每用户 5 次/分钟，超限 429）→ GLM-4V-Flash 生成 `{ title, description }` 回填表单；seek/claim 差异化 prompt

### 前后端术语映射（重要 ⚠️）

> 后端实体命名与直觉相反：**`LostItem` = 拾获物品（拾物招领）**，**`FindItem` = 丢失物品（寻物启事）**。前端一律用 `seek / claim`，禁止按名称猜测端点。

| 后端表 / 实体 | 含义 | 前端 category | 端点 |
| ------ | ------ | ------ | ------ |
| `lost_item` / `LostItem` | 拾获物品（拾物招领） | `'claim'` | `/api/v1/lost-items` |
| `find_item` / `FindItem` | 丢失物品（寻物启事） | `'seek'` | `/api/v1/find-items` |

## 🖥️ 前端架构

Vue 3 + TS + Tailwind + Pinia + vue-router；`api/` 按域分文件（auth/items/ai），统一请求封装解包 `Result<T>`、自动带 token、401 跳登录（`request.ts`）。**图标统一用 Lucide（`lucide-vue-next`），禁止 Emoji 充当图标**（全站映射见 [docs/frontend-development.md](docs/frontend-development.md) §1.4）。发布页含 `UploadArea`（canvas 压缩 → 纯 base64）+「自动生成描述」按钮。完整规范见 [docs/frontend-development.md](docs/frontend-development.md)（v1.2）。

## 🗄️ 核心数据模型

三张表：`user`、`lost_item`（拾物招领）、`find_item`（寻物启事），后两者 `user_id` 外键关联发布者、`contact` 默认取发布者手机号。完整字段见 [docs/data-model.md](docs/data-model.md)。

## 🔌 API 设计规范

- 统一 `Result<T>`：`{ code, message, data }`；`GlobalExceptionHandler` 统一转 `Result.error(code, msg)`（500 业务 / 400 校验 / 401 未登录 / 429 限流）
- 权限：**GET 公开，POST 需登录**（Bearer token），`/api/v1/auth` 公开；分页用 `Pageable`

| 方法 | 路径 | 认证 | 说明 |
| ------ | ------ | ------ | ------ |
| `POST` | `/api/v1/auth/register` | ❌ | 注册 `{ phone, password, nickname }` |
| `POST` | `/api/v1/auth/login` | ❌ | 登录 → `{ token, userId, nickname }` |
| `POST` | `/api/v1/lost-items` | ✅ | 发布拾物（image_url 必填） |
| `GET` | `/api/v1/lost-items` | ❌（`mine=true` 需登录） | 列表 `?title=&page=&size=&sort=&mine=` |
| `GET` | `/api/v1/lost-items/{id}` | ❌ | 详情 |
| `POST` | `/api/v1/find-items` | ✅ | 发布寻物 |
| `GET` | `/api/v1/find-items` | ❌（`mine=true` 需登录） | 列表 |
| `GET` | `/api/v1/find-items/{id}` | ❌ | 详情 |
| `GET` | `/api/v1/find-items/{id}/matches?limit=3` | ❌ | 智能匹配（寻物 → 拾物） |
| `GET` | `/api/v1/lost-items/{id}/matches?limit=3` | ❌ | 智能匹配（拾物 → 寻物） |
| `POST` | `/api/v1/ai/describe` | ✅ | AI 生成描述 `{ imageBase64, category? }` → `{ title, description }`（5 次/分钟） |
| `GET` | `/health` | ❌ | 健康检查（微信云托管） |

## ⚙️ 关键约定

1. Controller → Service → Repository 分层，禁止手写 SQL；public 方法写 Javadoc
2. Lombok（`@Data`/`@NoArgsConstructor`/`@AllArgsConstructor`）
3. 密码永远 BCrypt 存储；JWT 密钥从环境变量读取
4. 精确修改：只改任务相关代码，不顺手重构无关内容
5. 发布物品时从 JWT 提取 userId 关联发布者
6. 前端发布类型一律 `seek`/`claim`，严禁直连后端 `lost/find` 命名
7. AI 调用只走服务端（`GlmClient` 薄封装），Key 存 `GLM_API_KEY` **绝不下发前端**；换模型只改 `GlmClient`
8. AI 接口必须限流（每用户 5 次/分钟，`glm.rate-limit-per-minute` 可配）
9. 计划完成后分步执行，每步等用户确认
10. 前端图标统一用 Lucide（`lucide-vue-next`）SVG 组件，**禁止 Emoji 充当图标**；SVG 尺寸用 Tailwind `size-*`（不吃 `text-*` 字号），装饰性图标加 `aria-hidden`（规范见 docs/frontend-development.md §1.4）

## 📋 质量检查

| 检查项 | 命令 | 标准 |
| ------ | ------ | ------ |
| 后端编译 / 测试 | `cd backend && .\mvnw.cmd clean compile` / `test` | BUILD SUCCESS / 0 failures, 0 errors |
| 前端构建 / Lint | `cd frontend && npm run build` / `npm run lint` | 无类型 / 无 ESLint 错误 |

## 📖 文档导航

| 文档 | 说明 |
| ------ | ------ |
| `AGENTS.md` | 项目心智模型 + 全局约定（本文档） |
| `docs/development.md` | 本机工具链、部署地址、踩坑记录、curl 冒烟模板 |
| `docs/data-model.md` | 数据库表字段定义 |
| `docs/frontend-development.md` | 前端开发规范 v1.1（页面 / 交互 / 数据模型 / 组件） |
| `plans/ai-describe.md` | 后端 AI 自动生成描述计划（已实现） |
| `plans/ai-describe-frontend.md` | 前端「自动生成描述」计划（已实现） |
| `plans/lost-item.md` / `find-item.md` / `match.md` / `frontend.md` | 历史开发计划与记录 |
