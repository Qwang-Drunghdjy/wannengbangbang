# 物品详情页 · 编辑功能开发

## Context（背景）

延续已完成的 kebab 菜单，实现其中「编辑」功能。点击详情页右上 kebab → 菜单 → 「编辑」后，进入编辑页面修改该物品并保存。
已确认决策：

- **更新端点走后端现有 POST 约定**（沿用 `AuthInterceptor` 只拦 POST，与 `/claim` 一致，改动最小）。
- **编辑时类别（seek/claim）绝对锁定**，不可切换。
- **编辑页保留「自动生成描述」**（仅在用户重新选图后有 base64 时可用）。

「删除」「复制链接」不在本次范围，保持不做事。

## 关键现状（已核实代码）

### 后端：目前**没有更新接口**

- `LostItemController/FindItemController` 仅有 `POST /`（创建）、`POST /{id}/claim`（认领）、`GET /{id}`（详情）、`GET /`（列表）。
- `LostItemService.updateClaimed()` / `FindItemService.updateClaimed()` 是现成"仅本人 + `ForbiddenException`(403)"范式，可直接仿写 `update()`。
- `AuthInterceptor`：**只拦截 `!= POST` 一律放行** → 新建 `POST /{id}` 自动获得鉴权，无需改拦截器。
- `POST /lost-items` 创建用 `@RequestBody LostItem item`；`create()` 里 `id=null`、`createTime=now`、关联 `user`、`contact` 空则取手机号。Listed NotFound 抛 `RuntimeException`(→500)。

### 前端（复用基础齐全）

- `POST /request.ts` 的 `post<T>(url, body)` 自动带 token、解包 `Result`、401 跳登录 — 更新直接复用。
- `api/items.ts` 已有 `fetchLostItem/fetchFindItem`（详情，编辑预填直接用）。
- `api/types`：`PublishPayload`（title/description/location/contact/imageUrl）、`PublishItem`（含可空字段 & user）。
- `UploadArea.vue`：**当前不支持预填已有图**——只接受"新选一张图"（内部 `fileUrl` ref 初始 null，无 `initialSrc`）。编辑展示已有图需加 prop。
- `PublishView.vue`：单表单（type 选择屏 + 表单）；类别→端点映射、校验、AI 描述逻辑都可复用；`effectiveType=category||picked`。
- 路由 `router/index.ts` 已含 `/item/:id`（SimpleLayout，子页详情）。
- 详情 kebab `ItemActionMenu.vue` 现为纯展示（无 emit），需为「编辑」加事件。
- `DesktopMenu` 上一步仅 owner 显示「编辑/删除」，非 owner 仅「复制链接」。

## Approach

### 1) 后端：新增 `update`（POST /{id}）

两套 controller+service 对称加：

- Controller：`@PostMapping("/{id}")`，参数 `@PathVariable Long id`、`@RequestBody LostItem item`、`request`；调 `service.update(id, userId, item)`。
- Service `update(Long id, Long userId, T item)`：
  - `findById(id)` → 非本人 `throw new ForbiddenException(...)`（仿 `updateClaimed`）。
  - 保留原记录 `id / createTime / user / claimed`；仅覆盖可编辑字段：`title / description / location / contact / imageUrl`。
  - `contact` 兜底：空则取发布者手机号（与 create 一致）。
  - `save` 返回。
- **复用**：`updateClaimed()` 的越权写法、`ForbiddenException`（`GlobalExceptionHandler` 已有 403 映射）。

### 2) 前端 UploadArea 增加 `initialSrc`

- 加 `initialSrc?: string` prop；`onMounted` 时若提供则 `fileUrl.value = initialSrc`（展示已有图，`fileName` 保持空）。
- 选新图/删图逻辑不变（重选上报 `{previewUrl, base64}`，删除上报 `null`）。复用现有压缩 `compressToBase64`。

### 3) 前端 `PublishView.vue` 编辑模式（复用同一视图）

- 读取 `route.params.id`；有 `id` → **编辑模式** `isEdit`；否则新建行为不变。
- 分类：仍走现有 `urlType`（编辑路由带 `?type=`）；编辑时 `effectiveType` 已定，`类型选择屏 v-if="!effectiveType"` 不触发 → **类别天然锁定**。
- `onMounted`（编辑）：按 `urlType` 调 `fetchLostItem(id)` 或 `fetchFindItem(id)` 预填 `title/description/location/contact`（判空），`imageUrl=item.imageUrl`、传给 `<UploadArea :initial-src="item.imageUrl">`；`imageBase64=''`（旧图无 base64）。
  - 轻量属主防护：`item.user?.id !== auth.user?.id` → 页内 error + 引导返回，`route` 兜底由后端 403。
- AI 按钮：`v-if="imageUrl"` 保留；`:disabled="!imageBase64 || generating"` —— 编辑旧图（无 base64）时禁用，换图后可点。
- 提交：校验沿用；`isEdit` 时 `updateLostItem/updateFindItem(id, payload)` 成功 → `router.replace('/item/'+id+'?type='+effectiveType)` + alert "保存成功"；按钮文案 `保存`（新建：`发布`）。

### 4) 前端路由

- `router/index.ts` 新增 `/item/:id/edit`（SimpleLayout，`title:'编辑信息'`，`requiresAuth`）→ `PublishView`。

### 5) 前端 kebab 菜单接线「编辑」

- `ItemActionMenu.vue` 加 `emit('edit')`（仅 owner 行的编辑按钮）。
- `ItemDetailView.vue`：`@edit` → 关闭菜单 + `router.push('/item/' + id + '/edit?type=' + type)`。 **删除 / 复制链接**仍无动作。

## Files to modify

后端（对称两套）：

- `backend/.../controller/LostItemController.java`（+`POST /{id}`）
- `backend/.../controller/FindItemController.java`（+`POST /{id}`）
- `backend/.../service/LostItemService.java`（+`update()`）
- `backend/.../service/FindItemService.java`（+`update()`）

前端：

- `frontend/src/api/items.ts`（+`updateLostItem()`/`updateFindItem()`）
- `frontend/src/components/UploadArea.vue`（+`initialSrc`）
- `frontend/src/views/PublishView.vue`（编辑模式）
- `frontend/src/router/index.ts`（+/`item/:id/edit`）
- `frontend/src/components/ItemActionMenu.vue`（+emit edit）
- `frontend/src/views/ItemDetailView.vue`（接线 编辑→跳转）

## Reuse（现有可复用，不新造轮子）

- `GET /lost-items/{id}`、`GET /find-items/{id}`（`fetchLostItem/fetchFindItem`）做预填
- `request.post()`（`auth`）更新接口/新建都走同一封装
- `PublishPayload / PublishItem` 类型
- `updateClaimed()` 越权+403 范式；`ForbiddenException` + `GlobalExceptionHandler`
- 现有 `UploadArea` 压缩、`PublishView` 校验 / AI 描述 / 类别→端点映射

## Steps

- [x] 后端 `LostItem`：controller `POST /{id}` + service `update()`（属主 403、保留 id/createTime/user/claimed、contact 兜底）
- [x] 后端 `FindItem`：同上对称实现
- [x] 后端编译 + 既有测试通过（96 tests / 0 failures）
- [x] `api/items.ts` 加 `updateLostItem/updateFindItem`
- [x] `UploadArea.vue` 加 `initialSrc` prop 展示已有图
- [x] `PublishView.vue` 编辑模式：id 判定、预填、锁定类别、AI 按钮按 base 禁用、提交走 update、按钮「保存」
- [x] `router/index.ts` 加 `/item/:id/edit`
- [x] `ItemActionMenu` emit edit + `ItemDetailView` 跳转编辑并关闭菜单
- [x] 前端 build + lint 通过

## Verification

- 后端：`cd backend && .\mvnw.cmd clean compile`（BUILD SUCCESS）与 `.\mvnw.cmd test`（0 failures）
- 前端：`cd frontend && npm run build` + `npm run lint`（无类型 / 无 ESLint 错）
- 手动（需登录并是发布者）：
  1. 详情页 kebab → 编辑 => 编辑页表单已预填当前值、类别不可切、图片展示旧图。
  2. 不改图 AI 按钮禁用；换新图后 AI 可用、`description` 可再覆盖。
  3. 改字段点「保存」→ 回详情页，内容已更新；非本人直接进 `/item/:id/edit?type=` 保存被后端 403 拦。
