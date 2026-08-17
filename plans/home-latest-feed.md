# 首页「最新消息」混合信息流改造计划

## Context（背景）

- 现状：`HomeView.vue` 首页只有「最近拾物」区域，仅展示**拾物招领（claim）3 条**，数据源为 `GET /api/v1/lost-items?page=0&size=3`。
- 目标：该区域改为「**最新消息**」，同时展示**寻物启事（seek）** 与**拾物招领（claim）** 两类消息，共 **6 条**，按发布时间倒序（最新在最上面）。
- 后端没有"混合最新消息"的统一接口（`/lost-items` 与 `/find-items` 相互独立），因此数据在**前端合并**。

## 已确认需求（用户逐条确认）

| # | 决策 | 内容 |
| --- | ------ | ------ |
| Q1 | 方案 A | 前端分别请求拾物、寻物列表**各 6 条** → 合并 → 按 `createTime` 倒序取前 6；某类不足 6 条时由另一类补足 |
| Q2 | 推荐样式 | 不同类型用**不同图标 + 彩色标签**：寻物 🔍 + 橙色「寻物」/ 拾物 🎒 + 绿色「拾物」 |
| Q3 | 确认 | 拾物 → `/item/:id?type=claim`；寻物 → `/item/:id?type=seek`（详情页已支持两种类型） |
| Q4 | 修改 | 标题定为「**最新消息**」（非"最新失物消息"） |

## Approach（方案）

1. **取数**：`HomeView.vue` 用 `Promise.all` 并行请求：
   - `fetchLostItems({ page: 0, size: 6, sort: 'createTime,desc' })` → 打标签 `category: 'claim'`
   - `fetchFindItems({ page: 0, size: 6, sort: 'createTime,desc' })` → 打标签 `category: 'seek'`
   - 合并 → 按 `createTime` 倒序（防御：缺失视为最早，排最后）→ `slice(0, 6)`。
   - **客户端排序为权威逻辑**：即使后端忽略 `sort` 参数，结果依然正确。
2. **渲染**：标题「最新消息」；列表项按 `category` 显示图标（🔍/🎒）与彩色标签（`bg-orange-100 text-orange-600`「寻物」/ `bg-green-100 text-green-600`「拾物」），保留标题、地点、相对时间；点击按类型拼接 `?type=`。
3. **状态**：保留现有 `loading` / `error` 态；空态文案「暂无拾物信息」→「暂无消息」。
4. **文档同步**：更新 `docs/frontend-development.md` 中首页规范（3.1.3 节、API 速查表、示例注释）；`plans/frontend.md` 为历史开发记录，**不改**。

## Files to modify（改动文件）

| 文件 | 改动 |
| ------ | ------ |
| `frontend/src/views/HomeView.vue` | 核心改动：标题、双列表并行取数 + 合并排序、类型图标/标签、按类型跳转、空态文案 |
| `frontend/src/api/items.ts` | `fetchLostItems` 注释「首页"最近拾物"」→ 更新为通用描述（接口本身不变） |
| `docs/frontend-development.md` | §3.1.3 首页消息区规范、§6 API 速查表（第 277 行附近）、示例注释（第 416 行附近）同步为「最新消息 + 双类型 + 6 条」 |

## Reuse（复用现有代码）

- `fetchLostItems` / `fetchFindItems`（`frontend/src/api/items.ts`）：两个分页接口已存在，直接复用。
- `relativeTime`（`frontend/src/utils/time.ts`）：相对时间格式化，无需新写。
- `PublishItem.category`（`frontend/src/api/types.ts`）：后端不返回该字段，由前端在合并时打标（仅前端用，不影响提交体）。
- `ItemDetailView.vue`：已支持 `?type=seek` / `?type=claim`，点击跳转无需改详情页。
- Tailwind v4 默认色板（orange / green）+ 现有自定义色（`primary/ink/muted/danger`，`frontend/src/styles/main.css` `@theme`）。

## Steps（实施清单）

- [ ] 1. `HomeView.vue` script：`onMounted` 改为并行请求两个列表（各 6 条 + `sort=createTime,desc`），合并后打 `category` 标签、按时间倒序取 6 条
- [ ] 2. `HomeView.vue` template：标题改「最新消息」；列表项按 `category` 显示 🔍/🎒 图标 + 橙色/绿色「寻物」「拾物」标签；跳转 `:to` 按类型拼接 `?type=seek|claim`；空态文案改「暂无消息」
- [ ] 3. `api/items.ts`：更新 `fetchLostItems` 注释（不再特指首页）
- [ ] 4. `docs/frontend-development.md`：同步 §3.1.3、API 速查表行、示例注释
- [ ] 5. 构建验证（见下）

## Verification（验证）

1. `cd frontend && npm run build`（vue-tsc + vite 构建通过，无类型错误）；如有 lint 配置跑 `npm run lint`。
2. 本地或对已部署后端联调：首页「最新消息」区域应同时出现寻物与拾物，**恰好 ≤6 条**、**按发布时间倒序**（最新在最上）。
3. 点击拾物条目 → `/item/:id?type=claim`；点击寻物条目 → `/item/:id?type=seek`，详情页正常加载。
4. 边界：某类数据不足 6 条时由另一类补足；总数不足 6 条时显示实际条数；空库时显示「暂无消息」。
