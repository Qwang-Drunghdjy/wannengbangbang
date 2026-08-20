# 规划：物品详情页「已认领」开关

## Context（背景）

在物品详情页 `ItemDetailView` 增加一个「已认领」开关（Toggle）。仅当该消息是**当前登录用户本人发布**时才显示并可操作。标记为已认领的物品**不参与智能匹配**（不作为他人的匹配候选）。

区分两种物品：

- **拾物招领**（`LostItem`，前端 `claim`）：发布者=捡到者，标记「已认领」表示失主已取回。
- **寻物启事**（`FindItem`，前端 `seek`）：发布者=失主，标记「已认领」表示已找回。

二者共用同一套开关逻辑，仅端点不同。

## 已确认的决策（用户答复：1b → 2 是 → 3 接受）

1. **已认领本条时「智能匹配」区 → (b) 置灰禁用 + 提示**：
   - 加载详情时若 `item.claimed === true`，匹配按钮即为禁用态（`disabled` + 文案「该物品已认领」）；
   - 用户在本页将开关从关→开（标记已认领）**成功后**：同步禁用匹配区，并收起/清空已展开的匹配结果（`matchesOpen=false`、`matches=null`）；
   - 开关从开→关（取消认领）后恢复可匹配。
2. **「不参与匹配」范围**：已认领物品**不再作为他人匹配的候选**，但**仍正常显示在列表页**（首页 / 我的发布）。列表逻辑不做任何改动。
3. **后端越权响应**：非本人调用 `/claim` 返回 **403**，新增 `ForbiddenException` 并按现有 `GlobalExceptionHandler` 模式接入。

## Approach（推荐方案）

### 后端

1. **实体加 `claimed` 字段**：`LostItem` 与 `FindItem` 各加 `private boolean claimed = false;`
   - `application.yml` 为 `ddl-auto: update`，启动时自动为两表新增该列，无需手动迁移；存量行读到 `false`。
2. **新增本人专属更新端点**（POST）：
   - `POST /api/v1/lost-items/{id}/claim`，body `{ "claimed": true|false }`
   - `POST /api/v1/find-items/{id}/claim`，body `{ "claimed": true|false }`
   - 复用 `AuthInterceptor`：仅拦 POST 并注入 `userId`（`request.getAttribute(USER_ID_ATTR)`），控制器无需再解析 JWT。**不用 PATCH**：`WebMvcConfig` CORS 未放行 PATCH，且拦截器只拦 POST。
   - **本人校验**：Service 中 `item.getUser().getId().equals(userId)`，否则抛 `ForbiddenException`（HTTP 403）。
   - 新增 `exception/ForbiddenException.java`，并在 `GlobalExceptionHandler` 增加 `@ExceptionHandler(ForbiddenException.class)` + `@ResponseStatus(HttpStatus.FORBIDDEN)`。
3. **匹配排除已认领**：`MatchingService`
   - `findMatches`（寻物→拾物，候选 LostItem）：候选流 `.filter(li -> !li.isClaimed())`
   - `findMatchesByLostItem`（拾物→寻物，候选 FindItem）：候选流 `.filter(fi -> !fi.isClaimed())`

### 前端

1. `api/types.ts`：`PublishItem` 增加 `claimed?: boolean`。
2. `api/items.ts`：新增
   - `updateLostItemClaim(id, claimed)` → `post('/lost-items/${id}/claim', { claimed })`
   - `updateFindItemClaim(id, claimed)` → `post('/find-items/${id}/claim', { claimed })`
3. `views/ItemDetailView.vue`：
   - 引入 `useAuthStore`；`isOwner = computed(() => auth.user?.id === item.value?.user?.id)`（已登录且是本人）。
   - 渲染开关（`v-if="isOwner"`），绑定 `item.claimed`。
   - 切换 → 调对应 update 接口 → 成功更新 `item.value.claimed`；失败回滚并提示。
   - **匹配按钮联动**：`matchesDisabled = computed(() => item.value?.claimed === true)`；按钮 `:disabled`，禁用文案「该物品已认领」；标记认领成功时 `matchesOpen=false; matches=null`，取消认领后恢复。
   - UI 用 Lucide 图标 + Tailwind 开关（符合项目「禁用 Emoji 图标、用 `size-*`」规范）。

## Files to modify（待修改文件）

- `backend/.../entity/LostItem.java`
- `backend/.../entity/FindItem.java`
- `backend/.../service/LostItemService.java`
- `backend/.../service/FindItemService.java`
- `backend/.../service/MatchingService.java`
- `backend/.../controller/LostItemController.java`
- `backend/.../controller/FindItemController.java`
- `backend/.../exception/ForbiddenException.java`（新增）
- `backend/.../controller/GlobalExceptionHandler.java`
- `backend/src/test/.../service/LostItemServiceTest.java`
- `backend/src/test/.../service/FindItemServiceTest.java`
- `backend/src/test/.../service/MatchingServiceTest.java`
- `frontend/src/api/types.ts`
- `frontend/src/api/items.ts`
- `frontend/src/views/ItemDetailView.vue`

## Reuse（复用现有代码）

- `AuthInterceptor.USER_ID_ATTR` / `extractUserId`：POST 自动鉴权并注入 userId（`backend/.../config/AuthInterceptor.java`）
- `Result.success` / `Result.error` 统一响应（`backend/.../dto/Result.java`）
- `exception/UnauthorizedException` → 仿写 `ForbiddenException`
- `GlobalExceptionHandler`：仿现有 `@ExceptionHandler` + `@ResponseStatus` 添加 403 分支
- `items.ts` 现有 `post`/`get` 解包封装（`frontend/src/api/request.ts`）
- `useAuthStore` 获取当前用户（`frontend/src/stores/auth.ts`）
- 测试风格仿照 `LostItemServiceTest` / `MatchingServiceTest`（Mockito + AssertJ）

## Steps（实施清单）

后端：

- [x] 1. `LostItem` / `FindItem` 加 `claimed` 字段
- [x] 2. 新增 `ForbiddenException` + `GlobalExceptionHandler` 403 分支
- [x] 3. `LostItemService` / `FindItemService` 加 `updateClaimed(id, userId, claimed)`，含本人校验
- [x] 4. `LostItemController` / `FindItemController` 加 `POST /{id}/claim`
- [x] 5. `MatchingService` 过滤已认领候选（两个方向）
- [x] 6. 补 Service / Matching 测试

前端：

- [x] 7. `types.ts` 加 `claimed`
- [x] 8. `items.ts` 加 `updateLostItemClaim` / `updateFindItemClaim`
- [x] 9. `ItemDetailView` 加本人开关 + 匹配按钮禁用联动

## Verification（验证）

- 后端：`cd backend && .\mvnw.cmd clean compile` → `.\mvnw.cmd test`（BUILD SUCCESS）
- 前端：`cd frontend && npm run build` → `npm run lint`
- 手测：
  - 本人详情页可见开关，可切换；
  - 非本人不显示开关；
  - 标记认领后：本页匹配按钮置灰「该物品已认领」，该物品不再出现在他人匹配结果；
  - 他人直接调用 `/claim` 返回 403。
