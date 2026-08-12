# 前端脚手架（frontend/）搭建计划

## Context

按 `AGENTS.md`（前端架构：Vue 3 + TS + Tailwind）与 `docs/frontend-development.md` v1.1（页面/路由/API 层/组件规范）在仓库根目录新建 `frontend/`，搭建可直接运行的前端脚手架：工程初始化 → 目录结构 → 路由（模型 A）→ 请求层 → 状态管理 → 布局与公共组件 → 页面实现。

后端接口已就绪（Spring Boot :8080，`/api/v1/**`，POST 需 Bearer token），通过 Vite dev proxy 联调，无需处理 CORS。

## 环境事实（已探明）

- Node `v24.17.0`、npm `12.0.1`（满足 Vite 7 / Tailwind v4 要求）；pnpm/yarn 未安装
- `frontend/` 尚不存在，需从零初始化
- 后端联调基址：`http://localhost:8080/api/v1/**`

## 技术选型

| 项 | 方案 | 说明 |
| --- | --- | --- |
| 构建 | **Vite 7 + vue-ts 模板** | 官方脚手架 |
| 语言 | TypeScript（strict） | `@` 别名 → `src` |
| 样式 | **Tailwind CSS v4**（`@tailwindcss/vite`） | CSS-first 配置，规范 1.1 配色用 `@theme` 定义 |
| 路由 | vue-router 4 | 模型 A：3 个一级 tab + 二级页 + 登录/注册 |
| 状态 | Pinia | auth（token+user，localStorage 持久化）、messages（未读数 Mock） |
| HTTP | axios | 统一封装：解包 `Result<T>`、自动带 Bearer、401 清 token 跳登录 |
| 时间 | dayjs | 相对时间（规范 8.5） |
| 质量 | ESLint + Prettier（轻量） | ✅ 已确认纳入 |
| 组件库 | **不引入，纯 Tailwind 手写**（BottomNav/TopBar/UploadArea/MatchCard/MessageItem/CabinetDialog） | ✅ 已确认（规范 7.1 均为简单组件，组件库反而与自定义设计冲突） |

## 目录结构

```
frontend/
├── index.html
├── package.json / vite.config.ts / tsconfig*.json / .env.development
├── eslint.config.js / .prettierrc        # 如确认纳入
└── src/
    ├── main.ts / App.vue
    ├── router/index.ts                   # 8 路由 + 守卫（meta.requiresAuth）
    ├── layouts/
    │   ├── TabLayout.vue                 # 一级 tab：BottomNav + <router-view>
    │   └── SimpleLayout.vue              # 二级页：TopBar（返回）+ <router-view>
    ├── stores/
    │   ├── auth.ts                       # token/user 持久化 + login/logout/init
    │   └── messages.ts                   # 未读数（Mock）
    ├── api/
    │   ├── types.ts                      # Result<T> / PublishItem / MatchResult / Message / UserProfile / LoginResponse / PublishCategory
    │   ├── request.ts                    # axios 实例：baseURL=/api/v1、Bearer、code!==200、401
    │   ├── auth.ts                       # register / login
    │   └── items.ts                      # lost-items / find-items / matches
    ├── components/
    │   ├── BottomNav.vue / TopBar.vue / UploadArea.vue
    │   ├── MatchCard.vue / MessageItem.vue / CabinetDialog.vue
    ├── views/
    │   ├── HomeView.vue                  # 最近拾物列表（真实 API）
    │   ├── LoginView.vue / RegisterView.vue
    │   ├── PublishView.vue               # 类型选择 + 表单 + 单图
    │   ├── ItemDetailView.vue
    │   ├── MatchResultView.vue           # ?findItemId 拉真实匹配
    │   ├── MessagesView.vue              # 纯 Mock（后端未实现）
    │   └── ProfileView.vue
    ├── styles/main.css                   # Tailwind v4 @theme 配色 + 移动端基础样式
    └── utils/
        ├── time.ts                       # dayjs 相对时间（2小时前）
        └── format.ts                     # score×100 取整等
```

## 路由设计（模型 A，规范 1.3 / 2 节）

| 路径 | 布局 | 需登录 | 说明 |
| --- | --- | --- | --- |
| `/` | TabLayout | 否 | 首页（最近拾物） |
| `/messages` | TabLayout | 否 | 消息（Mock） |
| `/profile` | TabLayout | **是** | 个人中心 |
| `/login` | SimpleLayout | 否 | 登录（回跳原页面） |
| `/register` | SimpleLayout | 否 | 注册 → 跳登录 |
| `/publish` | SimpleLayout | **是** | `?type=seek\|claim`；无 type 先类型选择 |
| `/item/:id` | SimpleLayout | 否 | `?type=` 详情 |
| `/match-result` | SimpleLayout | 否 | `?findItemId=` 匹配结果 |

守卫：未登录访问需登录页 → `/login`；axios 401 → 清 token → `/login`（带回跳）。

## 关键实现细节

1. **术语**：`PublishCategory = 'seek' | 'claim'`（规范 5.1 映射表）；`seek`→`POST /find-items`，`claim`→`POST /lost-items`（拾物图片必填）。
2. **请求封装**：`request.ts` 统一 `data.code !== 200` 视为业务失败并 `reject`；401 时清 token 跳登录；token 自动注入 `Authorization: Bearer`。
3. **Vite proxy**：`/api` → `http://localhost:8080`，避免 CORS；`VITE_API_BASE=/api/v1`（.env.development）。
4. **Tailwind v4 主题**：`@theme` 定义 `--color-primary:#2563EB`、`--color-bg:#F8FAFC`、`--color-ink:#1E293B`、`--color-muted:#64748B`、`--color-success:#10B981`、`--color-danger:#EF4444`、`--color-line:#E2E8F0`；移动端 375~428、桌面 max-width 480 居中。
5. **发布流程**：claim 发布成功回首页；seek 发布成功跳 `/match-result?findItemId=<id>`。
6. **匹配度**：`score`(0.0~1.0)×100 取整 + 颜色（>80 绿 / 60-80 橙 / <60 灰）。
7. **消息页**：纯本地 Mock，不请求后端；"全部已读"仅本地状态。

## Steps

- [x] **Step 1**: 初始化 `frontend/`（`npm create vite@latest frontend -- --template vue-ts`）✅ 已完成
- [x] **Step 2**: 安装依赖（vue-router@4 / pinia / axios / dayjs；dev: tailwindcss@4 + @tailwindcss/vite、ESLint + Prettier）✅ 已完成
- [x] **Step 3**: 配置 `vite.config.ts`（@ 别名、/api proxy、tailwind 插件）+ `tsconfig` strict
- [x] **Step 4**: `styles/main.css` Tailwind v4 `@theme` 配色 + 移动端基础样式
- [x] **Step 5**: `api/types.ts` + `api/request.ts` + `api/auth.ts` + `api/items.ts`
- [x] **Step 6**: `stores/auth.ts` + `stores/messages.ts`
- [x] **Step 7**: `router/index.ts`（8 路由 + 守卫）+ `layouts/`（TabLayout / SimpleLayout）
- [x] **Step 8**: 公共组件 `components/`（BottomNav / TopBar / UploadArea / MatchCard / MessageItem / CabinetDialog）
- [x] **Step 9**: 页面 `views/`（按依赖序：Home → Login/Register → Publish → MatchResult → ItemDetail → Messages → Profile）
- [x] **Step 10**: `utils/time.ts` + `utils/format.ts`
- [x] **Step 11**: ESLint + Prettier 配置 ✅ 已纳入
- [x] **Step 12**: 验证：`npm run build`（vue-tsc 类型检查通过）+ `npm run dev` 路由冒烟；后端联调为可选（需 MySQL）

## Verification

```bash
cd frontend
npm install
npm run dev      # http://localhost:5173 → 检查 / /login /profile 守卫 /publish?type=seek 等路由
npm run build    # vue-tsc -b && vite build → 无类型错误、构建成功
```

联调（可选，需后端 `mvn spring-boot:run`）：注册 → 登录 → 首页拾物列表经 `/api` proxy 拉取真实数据。

## 已确认的决策（用户已确认）

- **Q1 UI 方案**：✅ 纯 Tailwind 手写组件（不引入组件库）
- **Q2 脚手架范围**：✅ B —— 骨架 + 登录/注册/首页列表对接真实后端，其余页面可运行占位
- **默认项**：✅ npm（环境已装）；纳入 ESLint + Prettier；相对时间用 dayjs；消息页保持 Mock；Node v24 满足 Vite 8 要求
