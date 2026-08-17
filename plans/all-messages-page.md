# 「全部消息」列表页改造计划（草稿）

## Context（背景）

- 首页「最新消息」区只展示最新 6 条（混合寻物 + 拾物）。
- 需求：在「最新消息」标题旁新增「**全部消息**」按钮，点击进入**新页面**：
  - 页面顶端可切换「**寻物**」「**拾物**」两个标签页
  - 提供「**仅查看我的**」开关，默认关闭；开启后仅显示当前用户发布的消息
- 目标：让用户能浏览完整的寻物/拾物列表，并快速筛出自己发布的内容。

## 已探明的代码事实

| 项 | 结论 |
| ------ | ------ |
| 路由/布局 | 二级页统一 `SimpleLayout` + `TopBar`（返回按钮 + 标题，`route.meta.title`），注册在 `frontend/src/router/index.ts`；一级 tab 用 `TabLayout`。新页面应走 SimpleLayout 二级页 |
| 后端列表接口 | `GET /api/v1/lost-items` / `GET /api/v1/find-items` 支持 `page/size/sort/title`，**不支持按用户过滤**（repository 只有 `findByTitleContaining`） |
| 列表响应 | 已部署后端实测返回 `{ content: [...], page: { size, number, totalElements, totalPages } }`（Spring Boot 3.4 新 Page 序列化）；`content` 内**含 `user` 对象**（id/nickname/phone…）——纯前端按 `item.user.id` 过滤可行 |
| 当前用户 | `frontend/src/stores/auth.ts` 的 `authStore.user.id` 保存登录用户 id（登录时写入） |
| 前端类型 | `types.ts` 的 `PageResult` 还是旧形状（顶层 totalElements），与新后端不符——需更新为 `{ content, page: {...} }`（首页只用到 `.content`，更新安全） |
| 列表项样式 | 首页 `HomeView.vue` 已有：类型图标（🔍/🎒）+ 彩色标签 + 标题 + 地点 + 相对时间，点击跳详情 —— 直接复用 |
| 文档 | `docs/frontend-development.md` §3.1.3 首页区、§3.5.2「我的发布列表页（暂不实现）」——新页面与「我的发布」需求有重叠，本次范围不含改 ProfileView |

## 待确认问题（Q）

1. **「仅查看我的」的数据实现**（后端无按用户过滤接口）：
   - **方案 B（推荐）**：后端两个列表接口新增 `mine=true` 参数，controller 解析 Bearer token 拿 userId，repository 加 `findByUserId` → 精确分页。成本：后端改动 + 重新部署微信云托管。
   - **方案 A'**：纯前端——开启开关后拉取足够大的页（如 size=100）再按 `item.user.id` 过滤。成本：零后端改动，demo 数据量下结果正确，但数据超过单页上限时会漏。
2. **分页交互**：「全部消息」页加载更多的方式：**加载更多按钮（推荐）** / 无限滚动 / 传统分页？
3. **标签页顺序与默认**：「寻物 | 拾物」从左到右，默认选中**寻物**？（可改为 拾物 在前）
4. **未登录时「仅查看我的」开关**：置灰不可点（点击提示需登录）？还是隐藏？

## Approach（待确认后完善）

（待定——取决于 Q1-Q4 答案）

## Files to modify（待确认后完善）

（待定）

## Reuse（复用）

- `fetchLostItems` / `fetchFindItems` + `PageQuery`（`frontend/src/api/items.ts`）
- `relativeTime`（`frontend/src/utils/time.ts`）
- `PublishItem.category` / `PublishItem.user`（`frontend/src/api/types.ts`）
- `SimpleLayout` + `TopBar`（新页路由注册）
- `authStore.user.id`（仅查看我的 的前端依据）
- HomeView 列表项模板样式

## Steps（待确认后完善）

（待定）

## Verification（待确认后完善）

（待定）
