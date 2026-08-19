# 完善「我的发布」总数显示

## Context

「我的」页（`ProfileView`）的「我的发布」行显示 `auth.user?.publishCount`，但该值在登录时被前端硬编码为 `0`，从不刷新 → 永远显示 0 条，无法反映用户真实发布总数。

**根因**：`frontend/src/stores/auth.ts` 的 `login()` 注释明确写着"登录响应仅含 token/userId/nickname；其余字段待后端用户详情接口补充"，`publishCount` 直接写死为 0。后端 `User` 实体无该字段，`LoginResponse` 也无。

**已确认决策**：

- Q1：**方案 A（纯前端）**——复用现有列表接口的 `mine=true` + `Page.totalElements` 计算，后端零改动 ✅
- Q2：**计数口径** = 拾物招领（`lost_item`）条数 + 寻物启事（`find_item`）条数，两表之和 ✅

## Approach

进入「我的」页（`ProfileView` `onMounted`）时并行拉取两个列表接口各 1 条（`size=1`），取 `page.totalElements` 之和，写入 auth store 的 `user.publishCount`（并同步 localStorage）。后端不修改。

- `GET /api/v1/lost-items?mine=true&size=1` → `page.totalElements`（拾物招领数，走 `findByUserId` 派生查询，Spring Data 计数查询返回全量总数，与 size 无关）
- `GET /api/v1/find-items?mine=true&size=1` → `page.totalElements`（寻物启事数）
- 求和 → `auth.user.publishCount`

> ⚠️ 必须用 `size=1` 而非 `size=0`：Spring Data 将 `size=0` 视为"不限量"（返回全部行），`size=1` 才安全拿到总数。
>
> ⚠️ `mine=true` 需登录；`ProfileView` 路由有 `requiresAuth`，正常情况下 token 必然存在。仍包 try/catch：失败时保持原值（token 过期场景由请求层统一跳登录）。

## Files to modify

| 文件 | 改动 |
| ------ | ------ |
| `frontend/src/stores/auth.ts` | 新增 action `updateUser(patch: Partial<UserProfile>)`：合并 `this.user` 并 `setStoredUser` 同步 localStorage（沿用现有 login/logout/clear 模式） |
| `frontend/src/views/ProfileView.vue` | `onMounted` 新增 `refreshPublishCount()`：并行 `fetchLostItems({ mine: true, size: 1 })` + `fetchFindItems({ mine: true, size: 1 })`，求和后 `auth.updateUser({ publishCount })`；新增 import `onMounted`、`fetchLostItems`、`fetchFindItems` |

不改动：后端任何代码、`api/items.ts`、`api/types.ts`（`PageResult` / `PageMeta.totalElements` 均已存在）、`AllMessagesView.vue`、`LoginResponse`。

## Reuse

- `frontend/src/api/items.ts`：`fetchLostItems / fetchFindItems` 已支持 `PageQuery.mine: true`，返回 `PageResult<T>`（含 `page.totalElements`）——**零新增 API 代码**
- `frontend/src/api/types.ts`：`PageResult` / `PageMeta.totalElements` 已定义
- `frontend/src/views/AllMessagesView.vue`：`mine` 传参与 `res.page.totalElements` 消费的现成范式
- `frontend/src/stores/auth.ts`：`setStoredUser` 已存在，`updateUser` 沿用其持久化模式

## Steps

- [x] 1. `stores/auth.ts`：新增 `updateUser(patch)` action——`this.user = this.user ? { ...this.user, ...patch } : null`（user 为空则忽略），非空时 `setStoredUser(this.user)`
- [x] 2. `ProfileView.vue`：script 区新增 `onMounted` 刷新逻辑（见 Approach），try/catch 静默失败；模板「我的发布」行不变（仍显示 `auth.user?.publishCount ?? 0`）
- [x] 3. 验证（见下）

## 执行记录（2026-07-09）

- 改动：`frontend/src/stores/auth.ts`（+updateUser action）、`frontend/src/views/ProfileView.vue`（+onMounted refreshPublishCount）；后端零改动
- `npm run lint` 通过；`npm run build`（vue-tsc + vite）通过
- 端到端验证（已部署后端，测试手机号 13900009999）：注册/登录 OK → 发布 2 拾物 + 3 寻物 成功 → `mine=true&size=1` 的 `totalElements` = 2 / 3，求和 = 5（与前端展示逻辑一致）
- 本机后端未启动（MySQL root 密码非默认，需向项目所有者确认），故用已部署后端（文档约定的冒烟目标）验证 API 契约 + 求和逻辑；前端渲染仅为既有插值，模板未改
- 遗留：测试数据（1 用户 + 5 物品）留存于已部署测试后端，可忽略或后续清理

## Verification

- `cd frontend && npm run lint` — 无 ESLint 错误
- `cd frontend && npm run build` — `vue-tsc` 类型检查通过
- 手动（`npm run dev`，需后端 `mvnw spring-boot:run` + MySQL）：
  - 注册/登录新用户 → 「我的」页「我的发布」显示 `0条`
  - 发布 2 条拾物 + 3 条寻物 → 重新进入「我的」页 → 显示 `5条`
  - 再发布 1 条 → 返回「我的」页 → 显示 `6条`（每次进入页面刷新）
  - 未登录访问 `/profile` 仍跳登录（回归确认）
- 后端无改动，无需跑后端测试
