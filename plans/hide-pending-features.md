# 隐藏待完善功能（保留代码，前端不显示）

## Context

用户希望**保留代码但不在前端显示**以下内容，等待后续完善后再恢复显示：

1. 首页（`HomeView`）的「南京审计大学」标识
2. 「消息」页面（`MessagesView`）及底部导航栏（`BottomNav`）的「消息」按钮
3. 「我的」页面（`ProfileView`）中「我的匹配」「信用评分」两行，**以及**头部蓝色横幅中的信用分（用户确认一并隐藏）

> 现状：消息 store（`stores/messages.ts`）为纯前端 Mock（后端无消息接口）；匹配/信用数据来自 `auth.user.matchCount / creditScore`。

**已确认的决策**：

- Q1：ProfileView 头部信用分一并隐藏 ✅
- Q2：只隐藏底部导航按钮，`/messages` 路由**保留可直接访问**（不加重定向）✅
- Q3：集中式功能开关配置文件 ✅
- `AllMessagesView`（全部消息列表页）**不在隐藏范围** ✅

## 关键技术约束

`frontend/tsconfig.app.json` 开了 `noUnusedLocals: true`。**不能直接注释掉模板代码**——`MapPin`（HomeView）、`Bell` + `messagesStore`（BottomNav）、`Link`（ProfileView）等 import 会变未使用，导致 `vue-tsc` / ESLint 报错、`npm run build` 失败。

因此统一采用 **`v-if` 条件渲染 / 数组条件过滤**：模板与 import 全部保留在源码中，只是不渲染。恢复时只需把开关改为 `true`，无需翻模板。

## Approach

新建集中开关文件 `frontend/src/config/features.ts`（`src/config/` 目录当前不存在，一并创建），导出三个布尔开关；各页面引用开关做条件渲染/过滤。开关为 `false` 时隐藏，为 `true` 时恢复，各页面代码零改动即可恢复。

```ts
/**
 * 功能开关：以下功能已实现但暂不在前端显示，等待后续完善后再恢复。
 * 恢复显示：将对应开关改为 true（无需改动各页面代码）。
 */
export const features = {
  /** 首页「南京审计大学」学校标识 */
  showSchoolBadge: false,
  /** 底部导航「消息」Tab（页面与路由已保留，URL 可直接访问 /messages） */
  showMessagesTab: false,
  /** 「我的」页：我的匹配 / 信用评分（含头部信用分） */
  showProfileStats: false,
}
```

## Files to modify

| 文件 | 改动 |
| ------ | ------ |
| `frontend/src/config/features.ts` | **新增**：集中开关 |
| `frontend/src/views/HomeView.vue` | 学校标识 `<span>` 加 `v-if="features.showSchoolBadge"`；新增 import（`MapPin` 仍被模板引用，无未使用问题） |
| `frontend/src/components/BottomNav.vue` | `tabs` 改为 `allTabs` + `computed` 过滤：`path === '/messages'` 时按 `features.showMessagesTab` 决定是否保留；`Bell`、`messagesStore`、未读角标逻辑原样保留（`tab.path === '/messages'` 分支代码不动） |
| `frontend/src/views/ProfileView.vue` | 头部信用分 `<p>` 与「我的匹配」「信用评分」两行加 `v-if="features.showProfileStats"`（两行相邻，可用一个 `<template v-if>` 包裹）；新增 import |

不改动：`router/index.ts`（路由保留）、`MessagesView.vue`、`stores/messages.ts`、`AllMessagesView.vue`、后端任何代码。

## Reuse

项目中无现成功能开关工具（`src/` 下无 `config/` 目录），为全新小文件。复用现有模式：`BottomNav` 已有 `computed` + `useRoute`；`ProfileView` / `HomeView` 已有 `v-if` 用法。

## Steps

- [x] 1. 新建 `frontend/src/config/features.ts`（上述开关文件）
- [x] 2. `HomeView.vue`：顶部 `import { features } from '@/config/features'`；学校标识 `<span>` 加 `v-if="features.showSchoolBadge"`（标题行 `justify-between` 单子元素布局不受影响）
- [x] 3. `BottomNav.vue`：`tabs` 常量改名 `allTabs`，新增 `computed` 版 `tabs` 按开关过滤 `/messages`；模板 `v-for="tab in tabs"` 与未读角标不动
- [x] 4. `ProfileView.vue`：头部信用分 `<p>` 加 `v-if="features.showProfileStats"`；「我的匹配」「信用评分」两行包进 `<template v-if="features.showProfileStats">`
- [x] 5. 验证（见下）

## 执行记录（2026-08-19）

- 全部 5 步完成；`git diff --stat`：3 个文件 +33/-17（另新增 `src/config/features.ts`）
- `npm run lint` 通过（无 ESLint 错误）；`npm run build`（vue-tsc + vite）通过
- 恢复演练：三个开关置 `true` 构建通过 → 置回 `false` 构建通过
- 未改动：`router/index.ts`（/messages 路由保留）、`MessagesView.vue`、`stores/messages.ts`、`AllMessagesView.vue`、后端

## Verification

- `cd frontend && npm run lint` — 无 ESLint 错误（重点确认无未使用 import）
- `cd frontend && npm run build` — `vue-tsc` 类型检查通过、构建成功
- 手动（`npm run dev`）：
  - 首页：右上无「南京审计大学」标识，布局正常
  - 底部导航：仅剩「首页」「我的」两项
  - URL 直接访问 `/messages`：页面仍可打开（方案 A，路由保留）
  - 「我的」页：蓝色横幅无信用分、列表无「我的匹配」「信用评分」行
  - **恢复演练**：把 `features.ts` 三个开关临时改 `true` → 上述内容全部恢复显示；改回 `false` 后再次构建通过
