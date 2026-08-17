# 「全部消息」列表页改造计划

## Context（背景）

- 首页「最新消息」区只展示最新 6 条（混合寻物 + 拾物），用户需要浏览**完整列表**并筛出**自己发布**的内容。
- 需求（已逐条确认）：
  1. 「最新消息」标题旁新增「**全部消息**」按钮 → 进入新页面
  2. 新页面顶端可切换「**寻物 / 拾物**」标签页（Q3：**寻物在前、默认寻物**）
  3. 「**仅查看我的**」开关，默认关闭；开启后仅显示当前用户发布的消息
  4. Q1：**后端 `mine=true` 参数**方案（精确分页，需后端改动 + 重新部署微信云托管）
  5. Q2：分页交互用**「加载更多」按钮**
  6. Q4：未登录时开关**置灰**，点击提示「请先登录」

## Approach（方案）

### 后端：列表接口支持 `mine=true`（仅查看我的）

- **Repository**（`LostItemRepository` / `FindItemRepository`）：新增
  - `Page<LostItem> findByUserId(Long userId, Pageable pageable)`
  - `Page<LostItem> findByUserIdAndTitleContaining(Long userId, String title, Pageable pageable)`
  - FindItem 同款两方法。
- **Service**（`LostItemService` / `FindItemService`）：`findAll` 签名扩展为 `findAll(String title, Long userId, Pageable pageable)`，四分支：
  - `userId != null && title 有值` → `findByUserIdAndTitleContaining`
  - `userId != null` → `findByUserId`
  - `title 有值` → `findByTitleContaining`
  - 否则 → `findAll(pageable)`
- **Controller**（`LostItemController` / `FindItemController`）`list`：新增 `@RequestParam(required = false) Boolean mine` + `HttpServletRequest request`：
  - `mine == true` 时解析 `Authorization: Bearer <token>` 拿 userId（复用 `AuthInterceptor` 现有解析逻辑，新增 **public static `extractUserId(HttpServletRequest, JwtUtil)`** 帮助方法，返回 null 表示未登录/无效）
  - 无有效 token → 抛 `UnauthorizedException`（新增异常类）→ `GlobalExceptionHandler` 注册为 **HTTP 401** + `Result.error(401, "未登录或登录已过期")`（与拦截器文案一致）
  - 未开启 mine 时行为完全不变（GET 仍公开）
- **测试**：更新 `LostItemServiceTest` / `FindItemServiceTest`（`findAll` 新签名加 `null` 参数）+ 新增 mine 分支用例；更新 `LostItemControllerTest` / `FindItemControllerTest`（mock 签名）+ 新增 `mine=true` 带 token / 不带 token（401）用例。

### 前端：全部消息页 + 首页入口

- **`types.ts`**：修正 `PageResult<T>` 为实际形状 `{ content: T[]; page: { size; number; totalElements; totalPages } }`（已部署后端实测为 Spring Boot 3.4 新序列化；全仓库仅 `items.ts` 使用，首页只用 `.content`，修正安全）。
- **`items.ts`**：`PageQuery` 增加 `mine?: boolean`。
- **新增 `components/ItemListItem.vue`**：抽出自首页的列表项渲染（🔍/🎒 图标 + 橙/绿「寻物」「拾物」标签 + 标题 + 地点 + 相对时间 + 按类型跳详情），首页与新页面共用（新页面必需，属任务内复用而非顺手重构）。
- **新增 `views/AllMessagesView.vue`**：
  - 状态：`tab: 'seek' | 'claim'`（默认 seek）、`onlyMine: boolean`（默认 false）、`items`、`page`、`hasMore`、`loading`、`loadingMore`、`error`
  - 取数：`fetchLostItems/fetchFindItems({ page, size: 10, sort: 'createTime,desc', mine: onlyMine || undefined })`，按当前 tab 调对应接口
  - 切 tab / 切开关 → 重置 `page=0` 重新拉取；「加载更多」→ `page+1` 追加（按 id 去重）
  - `hasMore = items.length < page.totalElements`（从 `page` 元数据取）
  - 「仅查看我的」开关：`!authStore.isLoggedIn` 时置灰（`opacity-50`），点击 `window.alert('请先登录')`（与现有 alert 用法一致）；登录后点击即切换并刷新
  - 空态按场景区分：未开 mine「暂无寻物消息 / 暂无拾物消息」；开 mine「您还没有发布寻物消息 / 您还没有发布拾物消息」
- **`HomeView.vue`**：标题行改 `flex justify-between`，「最新消息」+ `<router-link to="/all-messages" class="text-sm text-primary">全部消息 →</router-link>`；列表 `<li>` 内改用 `ItemListItem` 组件。
- **`router/index.ts`**：新增 `/all-messages` 二级页（SimpleLayout，`meta.title: '全部消息'`，**不设 requiresAuth**——页面公开可浏览，开关按 Q4 提示登录）。

### 文档同步

- `docs/frontend-development.md`：§3.1.3 补充「全部消息」入口；新增 §3.7「全部消息页」规格（标签页 / 仅查看我的 / 加载更多 / 空态）；§5 API 速查表与 §9 示例注释补充 `mine=true`。
- `AGENTS.md`：API 表 `GET /lost-items`、`GET /find-items` 行备注 `?mine=true（需登录）`。

## Files to modify（改动文件）

| 文件 | 改动 |
| ------ | ------ |
| `backend/.../repository/LostItemRepository.java` / `FindItemRepository.java` | 新增 `findByUserId`、`findByUserIdAndTitleContaining` |
| `backend/.../service/LostItemService.java` / `FindItemService.java` | `findAll` 加 `userId` 分支 |
| `backend/.../controller/LostItemController.java` / `FindItemController.java` | `list` 支持 `mine` + token 解析 |
| `backend/.../config/AuthInterceptor.java` | 新增 public static `extractUserId` |
| `backend/.../exception/UnauthorizedException.java`（新增） | 401 异常 |
| `backend/.../controller/GlobalExceptionHandler.java` | 注册 401 handler |
| `backend/src/test/...`（4 个测试类） | 适配 + 新增 mine 用例 |
| `frontend/src/api/types.ts` | 修正 `PageResult` 形状 |
| `frontend/src/api/items.ts` | `PageQuery.mine` |
| `frontend/src/components/ItemListItem.vue`（新增） | 列表项复用组件 |
| `frontend/src/views/AllMessagesView.vue`（新增） | 全部消息页 |
| `frontend/src/views/HomeView.vue` | 「全部消息」按钮 + 换用 ItemListItem |
| `frontend/src/router/index.ts` | `/all-messages` 路由 |
| `docs/frontend-development.md` / `AGENTS.md` | 文档同步 |

## Reuse（复用）

- `fetchLostItems` / `fetchFindItems` / `PageQuery`（`frontend/src/api/items.ts`）
- `relativeTime`（`frontend/src/utils/time.ts`）
- `PublishItem.category` / `PublishItem.user` / `PublishItem.createTime`（`frontend/src/api/types.ts`）
- `authStore.user.id` + `authStore.isLoggedIn`（`frontend/src/stores/auth.ts`）
- `SimpleLayout` + `TopBar`（`frontend/src/layouts/SimpleLayout.vue`，标题走 `route.meta.title`）
- 后端：`JwtUtil.parseToken` + `AuthInterceptor` 的 Bearer 解析逻辑（抽成 static 帮助方法）
- 现有 `window.alert` 提示样式（`PublishView` / `ItemDetailView` 同款）

## Steps（实施清单）

- [ ] **后端** 1. Repository 两接口新增 4 个查询方法
- [ ] **后端** 2. `AuthInterceptor.extractUserId` static 帮助方法 + `UnauthorizedException` + `GlobalExceptionHandler` 401 handler
- [ ] **后端** 3. 两个 Service 的 `findAll` 四分支
- [ ] **后端** 4. 两个 Controller 的 `list` 支持 `mine`
- [ ] **后端** 5. 更新 4 个测试类（签名适配 + mine 用例），`cd backend && .\mvnw.cmd clean compile` + `.\mvnw.cmd test` 全绿
- [ ] **前端** 6. `types.ts` 修正 `PageResult`；`items.ts` 加 `mine`
- [ ] **前端** 7. 新增 `ItemListItem.vue`；`HomeView.vue` 加「全部消息」按钮 + 换组件
- [ ] **前端** 8. 新增 `AllMessagesView.vue`；注册 `/all-messages` 路由
- [ ] **前端** 9. `npm run build` + `npm run lint` 通过
- [ ] **文档** 10. `docs/frontend-development.md` + `AGENTS.md` 同步
- [ ] **联调** 11. 本地起后端：curl 验证 `mine=true`（带/不带 token）；前端 dev 联调「全部消息」页全流程

## Verification（验证）

1. 后端：`cd backend && .\mvnw.cmd clean compile` BUILD SUCCESS；`.\mvnw.cmd test` 0 failures / 0 errors。
2. 前端：`cd frontend && npm run build`（vue-tsc + vite）通过；`npm run lint` 无报错。
3. 接口（本地后端）：`GET /api/v1/lost-items?mine=true` 无 token → **HTTP 401** + `Result.error(401,…)`；带 token → 仅返回该用户发布的内容；`mine=false/缺省` 行为不变。
4. 页面：首页「最新消息」右侧可见「全部消息 →」；新页面默认「寻物」tab；切换「拾物」tab 正确换数据；「加载更多」追加下一页且按钮在有更多时出现；未登录点「仅查看我的」→ alert「请先登录」且不切换；登录后开启 → 仅显示自己的消息。
5. 空态：某 tab 无数据/我的无数据时显示对应文案。
6. ⚠️ 部署提示（用户操作）：`mine=true` 依赖**后端重新部署微信云托管**；前端构建后可部署 EdgeOne Pages。部署前「仅查看我的」会对老后端返回 500（可接受，演示阶段先行验证其余功能）。
