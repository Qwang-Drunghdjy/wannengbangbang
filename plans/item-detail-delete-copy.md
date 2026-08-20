# 物品详情页 · 「删除」与「复制链接」功能开发

## Context（背景）

延续已完成的 kebab 菜单（编辑已实现），本次实现剩余两个动作按钮的功能：

- **删除**（仅发布者本人可见）：删除当前物品 → 成功后离开当前详情。
- **复制链接**（所有访客可见）：把当前物品详情的可分享链接拷到剪贴板。

## 已确认决策（沿用上一步）

- **后端仍走 POST 约定**（`AuthInterceptor` 只拦 POST），删除端点拟用 `POST /api/v1/{lost-items|find-items}/{id}/delete`。
- **仅本人可删除**：后端必须做属主校验（非本人 403），与 `updateClaimed()` 同一范式。
- **复制链接用「方案 C」**：`base = VITE_PUBLIC_BASE_URL || window.location.origin`，拼 `/item/{id}?type=seek|claim`。前后端分离下 `window.location.origin` 即前端域名，正确。
- 前端需在构建环境提供一个 `VITE_PUBLIC_BASE_URL`（可留空）。
- **删除前**用浏览器原生 `window.confirm()` 二次确认。
- **删除成功后** `router.back()`（无历史兜底回首页 `/`，与 TopBar 返回一致）。
- **复制成功反馈**：kebab 菜单「复制链接」按钮临时变为「已复制」约 2s 恢复，不弹 alert。

## 现状（已核实代码，从已实现编辑功能沿袭）

- `items.ts` 已有 `fetchLostItem/fetchFindItem`（详情）、`updateXxx`；`request.ts` 只有 `get/post`，无 del。
- 详情页 `ItemDetailView.vue`：已有 `item`、`type`（seek/claim）、`isOwner`、菜单 `@edit="onEdit"`。
- `ItemActionMenu.vue`：`actions` 数组含 编辑/删除(owner)/复制链接；`onAction` 仅 `type==='edit'` 才 emit；需扩为可 emit 删除 / 复制链接。
- 删除需加后端接口（controller + service），复用 `ForbiddenException`(403) 与 repository。

## Approach（推荐）

### 1) 后端删除（对称两套 lost-item / find-item）

- Controller：`@PostMapping("/{id}/delete")` → `service.delete(id, userId)`（POST 自动鉴权）。
- Service `delete(id, userId)`：`findById` → 非本人 `ForbiddenException`（403）→ `repository.delete(...)`。
- 复用 `updateClaimed` 越权写法；不校验认领状态（已认领也允许删，是否要先提示由前端确认）。

### 2) 前端 request：新增通用 del（或直接复用 post）

- 倾向复用现有 `post()`：`deleteLostItem(id) = post('/lost-items/'+id+'/delete')` / `deleteFindItem(id)` 对称。
- 如后续有其它 DELETE 语义，也可在 `request.ts` 加 `del`；本次以复用 POST 为主。

### 3) 前端 `ItemActionMenu.vue`

- `Action.type` 扩为 `'edit' | 'delete' | 'copy'`；`onAction` 按 type emit `edit/delete/copy`（删除/复制需在非 owner 时不出现——已由 `isOwner` 过滤）。

### 4) 前端 `VITE_PUBLIC_BASE_URL`

- 在 `.env.production`（或 vite env）加 `VITE_PUBLIC_BASE_URL=`（留空占位）。

### 5) 前端 `ItemDetailView.vue`

- `onCopy()`：构造 `link = (VITE_PUBLIC_BASE_URL || location.origin) + '/item/'+id+'?type='+type`；`navigator.clipboard.writeText(link)`；给出反馈（见待确认）。
- `onDelete()`：`window.confirm('确定要删除该物品吗？删除后无法恢复')` → 调删除接口 → 成功提示 `alert('已删除')` → `router.back()`（无历史回 `/`）。非本人调用被后端 403 栏。

## Files to modify

后端：

- `backend/.../controller/LostItemController.java`（+`POST /{id}/delete`）
- `backend/.../controller/FindItemController.java`（+`POST /{id}/delete`）
- `backend/.../service/LostItemService.java`（+`delete()`）
- `backend/.../service/FindItemService.java`（+`delete()`）

前端：

- `frontend/src/components/ItemActionMenu.vue`（type 扩为 edit/delete/copy，emit）
- `frontend/src/views/ItemDetailView.vue`（onDelete / onCopy / 接线）
- `frontend/src/api/items.ts`（+`deleteLostItem/deleteFindItem`）
- `frontend/.env.production`（+`VITE_PUBLIC_BASE_URL` 占位）

## Reuse（现成可复用）

- `ForbiddenException` + `GlobalExceptionHandler`（403）+ `updateClaimed` 越权写法
- `request.post()`、`PublishItem/PublishPayload` 类型
- `items.ts` 内部封装、`useAuthStore`（`auth.user`）
- `ItemActionMenu` `actions` 数组与 `isOwner` 过滤

## Steps

- [x] 后端 `LostItem`/`FindItem` controller+service：`delete()`（属主 403）
- [x] 后端编译 + 既有测试通过（96 tests / 0 failures）
- [x] `api/items.ts` 加 `deleteLostItem/deleteFindItem`
- [x] `ItemActionMenu` type 扩为 edit/delete/copy，emit
- [x] `ItemDetailView` 加 `onDelete`（confirm → 删除接口 → 提示 → back）与 `onCopy`（拼链接 → writeText → 按钮「已复制」2s）并对齐组件 emit
- [x] `.env.production` 加 `VITE_PUBLIC_BASE_URL` 占位
- [x] 前端 build + lint 通过

## Verification

- 后端编译/测试、前端 build/lint。
- 手动：详情 kebab → 复制链接（剪贴板得到 `…/item/{id}?type=…`，临时/正式域名按 env 取）；删除（确认后移除并跳转）；非本人调用删除被 403。
