# 万能帮帮 - 前端开发规范文档

> **文档用途**：为 AI Agent 提供完整的页面结构、交互逻辑、数据模型和组件规范，用于 Web 应用前端开发。
> **技术栈建议**：Vue 3 + TypeScript + Tailwind CSS + Lucide 图标库（`lucide-vue-next`）。
> **版本**：v1.2
> **最后更新**：2026-08-19
>
> **v1.2 变更说明**（图标体系升级）：
>
> - 全部 Emoji 图标替换为 **Lucide SVG 图标**（`lucide-vue-next`，按需引入 / tree-shaking），新增 §1.4 图标规范与全站 emoji→图标映射表。
> - 全文档 emoji 引用同步替换为对应 Lucide 组件名；尺寸换算：`text-2xl`→`size-6`、`text-3xl`→`size-8`、`text-xl`→`size-5`、`text-6xl`→`size-16`（见 1.4）。
>
> **v1.1 变更说明**（与后端 v1 API 对齐）：
>
> - 新增登录 / 注册页与 token 管理（后端 POST 已强制认证，401 处理统一）。
> - 发布类型 `category` 由 `'lost' | 'found'` 改为 `'seek' | 'claim'`，杜绝与后端 `lost/find` 命名的语义混淆（见 5.1 映射表）。
> - 图片上传由多图改为**单图**；拾物（claim）图片必填、寻物（seek）可选（对齐后端 `image_url` 约束）。
> - 底部导航改为「三个一级 tab」模型（首页 / 消息 / 我的），发布页、详情页、匹配结果页为二级页。
> - 第 9 节示例 API 对齐真实后端端点；消息模块后端未实现，明确标注为纯前端 Mock。

---

## 1. 全局设计规范

### 1.1 配色方案

- **主色**：`#2563EB`（蓝色，用于按钮、顶部栏背景、高亮元素）
- **背景色**：`#F8FAFC`（浅灰，用于页面背景）
- **卡片背景**：`#FFFFFF`（白色）
- **文字主色**：`#1E293B`（深灰）
- **文字辅助色**：`#64748B`（中灰）
- **成功/匹配高亮**：`#10B981`（绿色，用于匹配度标签）
- **未读标记**：`#EF4444`（红色，小红点）

### 1.2 字体与圆角

- **字体**：系统默认字体栈（`-apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif`）
- **圆角**：按钮和卡片统一 `8px`，头像圆形 `50%`

### 1.3 导航层级（模型 A）

- **一级 tab 页**：首页 `/`、消息 `/messages`、我的 `/profile`。底部导航常驻并高亮当前项，**无返回按钮**。
- **二级页**：发布页 `/publish`、物品详情页 `/item/:id`、匹配结果页 `/match-result`。顶部含**返回按钮**（←），**不显示底部导航**。
- **登录 / 注册页**：无底部导航，顶部含返回按钮（←）。
- **返回按钮逻辑**：优先 `router.back()`；无历史记录时兜底 `router.replace('/')`（防止从 tab 直达或深链进入后"回不去"）。

---

### 1.4 图标规范（Lucide，禁止 Emoji）

- **图标库**：`lucide-vue-next`（MIT，tree-shaking 按需引入，仅打包用到的图标）。**禁止用 Emoji 充当图标**。
- **用法**：组件式引入；尺寸用 Tailwind `size-*`（SVG 不响应 `text-*` 字号），颜色继承 `text-*`（`currentColor`）：

```vue
<script setup lang="ts">
import { Search, Star } from 'lucide-vue-next'
</script>
<template>
  <Search class="size-6" aria-hidden="true" />
  <Star class="size-4 fill-current" aria-hidden="true" /> <!-- 实心 fill-current -->
</template>
```

- **尺寸约定**（对应原 emoji 字号）：行内小图标 `size-4`（按钮内 / 文案旁，≈14-16px）；菜单 / 导航 `size-5`（原 text-xl）；列表 / 功能入口 `size-6`（原 text-2xl）；卡片占位 / 大图标 `size-8`（原 text-3xl）；详情页大占位图 `size-16`（原 text-6xl）。
- **可访问性**：装饰性图标（语义已由相邻文字表达）一律加 `aria-hidden="true"`。
- **新增图标**：先到 lucide.dev 检索确认存在；日后换库 / 换风格只改 import 行与映射，组件用法不变。
- **全站图标映射表**（emoji → Lucide）：

| 语义 | Lucide 组件 | 使用位置 |
| ---- | ----------- | -------- |
| 寻物 / 搜索 | `Search` | 发布寻物入口、ItemListItem 寻物图标、详情页智能匹配按钮 |
| 拾物 / 包裹 | `Package` | 发布拾物入口、无图占位、帮帮柜标题 |
| 拾物列表图标 | `Backpack` | ItemListItem 拾物图标 |
| 首页导航 | `Home` | BottomNav 首页 |
| 消息导航 | `Bell` | BottomNav 消息（未读角标） |
| 用户 | `User` | BottomNav 我的、头像占位、消息头像 |
| 地图定位 | `MapPin` | 首页右上角学校名 |
| 互助 | `Handshake` | 首页帮帮柜入口 |
| 评分 / 星级 | `Star` | 信用分、匹配度（实心 `fill-current`） |
| 联系 | `Link` | 「联系TA」按钮 |
| 上传 | `Camera` | 上传图片占位 |
| 收起匹配 | `FolderOpen` | 详情页「收起匹配」 |
| 匹配成功 | `PartyPopper` | 匹配结果页标题 |
| 我的发布 | `ClipboardList` | 个人中心菜单 |
| 设置 | `Settings` | 个人中心菜单 |
| AI 生成 | `Sparkles` | 发布页「自动生成描述」 |

---

## 2. 页面路由与层级

| 路由路径 | 页面名称 | 层级 | 需登录 | 是否显示底部导航 |
| --------- | ---------- | ------ | -------- | ------------------ |
| `/` | 首页 | 一级 tab | 否 | 是 |
| `/login` | 登录页 | — | 否 | 否 |
| `/register` | 注册页 | — | 否 | 否 |
| `/publish` | 发布页 | 二级页 | 是 | 否 |
| `/item/:id` | 物品详情页 | 二级页 | 否 | 否 |
| `/match-result` | 匹配结果页 | 二级页 | 否 | 否 |
| `/messages` | 消息通知页 | 一级 tab | 否 | 是 |
| `/profile` | 个人中心页 | 一级 tab | 是 | 是 |
| （弹窗） | 帮帮柜介绍弹窗 | 不涉及路由 | — | — |

---

## 3. 页面详细规格

### 3.1 首页 (`/`)

#### 3.1.1 顶部区域

- **左上角**：文本 `"万能帮帮"`，字体加粗，字号 `20px`。
- **右上角**：定位图标（`MapPin`）+ 文本 `"南京审计大学"`，点击可触发位置选择（暂不实现）。
- **顶部下方**：副标题 `"让失物有处寻 · 求助有回应"`，居中，字号 `14px`，颜色 `#64748B`。

#### 3.1.2 四个功能入口（2行×2列网格）

- 每个入口包含一个 **图标（Lucide，见 1.4 映射表）** 和 **文字标签**。
- 点击行为：
  - **发布寻物**（`Search`）→ 跳转 `/publish?type=seek`（寻物启事：我丢了东西，找它）
  - **发布拾物**（`Package`）→ 跳转 `/publish?type=claim`（拾物招领：我捡到东西，还给它）
  - **帮帮柜**（`Handshake`）→ 触发 **帮帮柜介绍弹窗**（见第 4 节）
  - **我的**（`User`）→ 跳转 `/profile`（需登录，未登录先跳 `/login`）

#### 3.1.3 最新消息列表区域（寻物 + 拾物混合）

- 标题：`"最新消息"`，字号 `18px`，加粗；**标题行右侧**为 `"全部消息 →"` 文字按钮（主色 `#2563EB`），点击进入 `/all-messages`（见 3.7 节）。
- 数据源：分别请求 `GET /api/v1/lost-items?page=0&size=6&sort=createTime,desc`（拾物招领）与 `GET /api/v1/find-items?page=0&size=6&sort=createTime,desc`（寻物启事），**前端合并后按发布时间倒序取前 6 条**（某类不足 6 条时由另一类补足；总数不足 6 条显示实际条数）。
- 列表项（每条显示）：
  - **类型图标 + 彩色标签**：寻物启事 `Search` + 橙色标签 `"寻物"`；拾物招领 `Backpack` + 绿色标签 `"拾物"`。
  - **物品名称** | **地点** | **相对时间**
  - 复用 `ItemListItem` 组件（新页「全部消息」同款）。
- 列表项点击 → 按类型跳转：拾物 → `/item/:id?type=claim`；寻物 → `/item/:id?type=seek`。
- 列表可滚动，支持下拉刷新（暂不实现）。

#### 3.1.4 底部导航栏（3项，一级 tab）

- 固定底部，高度 `60px`，背景白色，上边框 `1px solid #E2E8F0`。
- 每一项：图标 + 文字，点击切换页面；**当前页高亮**（图标和文字颜色为主色 `#2563EB`）。
- 导航项：
  - `Home` 首页 → `/`
  - `Bell` 消息 → `/messages`（有未读时显示红色角标，数量来自全局状态）
  - `User` 我的 → `/profile`
- 底部导航仅在一级 tab 页显示，二级页不显示（见 1.3）。

---

### 3.2 发布页 (`/publish`)

#### 3.2.1 URL 参数

- `?type=seek` 或 `?type=claim`，用于确定发布类型：
  - `seek`（寻物启事）→ 提交到 `POST /api/v1/find-items`
  - `claim`（拾物招领）→ 提交到 `POST /api/v1/lost-items`
- **无 `type` 参数时**（如直接访问）：先显示**类型选择界面**（"寻物启事 / 拾物招领"两个卡片按钮），选定后再进入表单。

#### 3.2.2 顶部导航栏

- 左侧：返回按钮（←），逻辑见 1.3
- 中间：标题 `"发布信息"`
- 右侧：空

#### 3.2.3 上传图片区域（单图）

- 虚线边框区域，高度 `120px`，居中显示 `Camera` 图标 + `"点击上传图片"`。
- 点击后触发文件选择器（仅图片），选择后显示缩略图；可重新选择或删除。
- **必填规则**：`claim`（拾物招领）**必填**（后端 `image_url` 非空约束）；`seek`（寻物启事）**可选**。
- **压缩**：选图后 canvas 压缩为 JPEG（宽边 ≤1024、质量 0.8），输出**纯 base64**（剥离 data URL 前缀，对齐后端 `DescribeImageRequest.imageBase64`）；`change` 事件上报 `{ previewUrl, base64 }`。
- 上传后弹出 `alert("已上传")` 并显示缩略图。

#### 3.2.3A 「自动生成描述」按钮（已实现，配合后端 `POST /api/v1/ai/describe`）

- 位置：详细描述输入框上方一行（左侧按钮 + 右侧辅助说明）。
- 触发：仅当已上传图片时显示；点击后 loading（按钮禁用 + "生成中..."，`Sparkles` 图标隐藏）。
- 填充规则：`title` **仅当为空时**填充；`description` **总是覆盖**（可再编辑）。
- 换图规则：重新选图 / 删除图片 → 清空 `description`（保留 `title`）。
- 失败兜底：页内红色提示（复用表单错误样式），**不阻塞手动发布**；AI 请求 30s 超时；触发后端限流时提示"操作过于频繁"。

#### 3.2.4 表单字段

| 字段名 | 组件类型 | 是否必填 | 占位文字 / 选项 |
| -------- | ---------- | ---------- | ----------------- |
| 物品名称 | 文本输入框 | 是 | "请输入物品名称" |
| 详细描述 | 多行文本框 | 否 | "请描述物品的颜色、品牌、特征..."，行数 4 |
| 丢失/拾到地点 | 下拉选择框 | 是 | 选项：图书馆、食堂、教学楼、操场、宿舍、商场、社区、其他。**标签随类型动态显示**：`seek`=丢失地点、`claim`=拾到地点 |
| 联系方式 | 文本输入框 | 否 | "手机号/微信号"；**预填登录用户手机号**，可编辑；留空则后端默认取手机号 |
| 图片 | 文件上传 | 见 3.2.3 | — |

#### 3.2.5 提交按钮

- 蓝色背景（`#2563EB`），白色文字，圆角，宽度 100%，高度 `48px`。
- 点击后：
  1. **登录校验**：未登录先跳 `/login`，登录成功后回跳本页。
  2. 前端校验必填字段（含 `claim` 类型的图片）。
  3. 提交对应端点（携带 `Authorization: Bearer <token>`）：
     - `claim` → `POST /api/v1/lost-items`，成功提示后返回首页。
     - `seek` → `POST /api/v1/find-items`，成功后跳转 `/match-result?findItemId=<新记录id>` 拉取真实匹配结果。

---

### 3.3 匹配结果页 (`/match-result`)

#### 3.3.1 顶部区域

- 标题：`"匹配成功！"`（前置 `PartyPopper` 图标，主色 `#2563EB`），字号 `24px`，居中。
- 副标题：`"已为您找到以下匹配物品"`，字号 `14px`，颜色 `#64748B`，居中。
- **数据来源**：`GET /api/v1/find-items/{id}/matches?limit=3`，`id` 取自 URL 参数 `?findItemId=`；若参数缺失则提示"无匹配数据"并显示返回首页按钮。

#### 3.3.2 匹配结果卡片

- **主卡片**（大号，高亮边框，取 score 最高的一条）：
  - 左侧：物品图片（无图时灰色方块 + `Package` 图标）
  - 右侧信息：
    - 物品名称：如 `"黑色双肩包"`（后端 `item.title`）
    - 匹配度：`Star` 95%（实心 `fill-current`，= 后端 `score` 0.95 × 100 取整，绿色文字）
    - 地点：如 `图书馆 2楼`
    - 时间：如 `2小时前`
  - 底部按钮：`"联系TA"`（前置 `Link` 图标），蓝色背景。点击展示发布者联系方式（`item.contact` 或 `item.user.phone`）；演示阶段弹提示（模拟联系）。
- **下方小卡片×2**（score 次高的两条，横向排列或纵向）：
  - 卡片1：`白色水杯 | 匹配度78% | 食堂`
  - 卡片2：`蓝色笔袋 | 匹配度62% | 教学楼`
  - 样式：白色背景，圆角，字体较小，点击可切换主卡片（交互暂不实现）。

#### 3.3.3 底部按钮

- `"返回首页"`，灰色边框按钮，点击跳转 `/`。

---

### 3.4 消息通知页 (`/messages`，一级 tab)

> **后端暂无消息接口**，本页为**纯前端 Mock**（本地模拟数据，"全部已读"仅更新本地状态），待后端实现后替换。

#### 3.4.1 顶部导航栏

- 一级 tab 页：**无返回按钮**，底部导航高亮"消息"。
- 左侧：空（或与标题对齐）
- 中间：`"我的消息"`
- 右侧：`"全部已读"` 文字按钮，点击将所有未读标记为已读（前端本地模拟）。

#### 3.4.2 消息列表（按时间倒序）

- 每条消息包含：
  - 头像（`User` 占位）
  - 标题（加粗）
  - 内容摘要（单行省略）
  - 相对时间
  - 未读状态：显示红色小红点（圆点 `●`）在标题左侧。
- 示例数据（硬编码，待 API 替换）：
  1. **未读**：标题 `【匹配成功】您的背包已找到！`，摘要 `"拾物者已存入帮帮柜，请凭取件码..."`，时间 `10分钟前`
  2. **已读**：标题 `【互助请求】求教高数题`，摘要 `"同学你好，看到你发布的信息..."`，时间 `2小时前`
  3. **已读**：标题 `【系统通知】您的物品已存放超时`，摘要 `"请于24小时内取回..."`，时间 `1天前`

---

### 3.5 个人中心页 (`/profile`，一级 tab，需登录)

#### 3.5.1 顶部区域（蓝色背景 `#2563EB`）

- 头像：圆形，直径 `64px`，显示 `User` 图标占位（有头像则显示头像 URL）。
- 昵称：如 `"拾光者用户"`，字号 `18px`，白色。
- 信用评分：`Star` 图标（实心 `fill-current`）+ `4.8分`，字号 `14px`，白色。

#### 3.5.2 功能列表（白色卡片）

- 每个列表项为一行，左侧图标 + 文字，右侧显示附加信息 + 箭头（→）。
- 列表项（左侧 Lucide 图标 + 文字）：
  - `ClipboardList` 我的发布 → 右侧 `N条 →`（点击跳转 `/all-messages?mine=1`，见 3.7 节，自动开启"仅查看我的"）
  - `Link` 我的匹配 → 右侧 `5次 →`（点击跳转至匹配记录页，暂不实现）
  - `Star` 信用评分 → 右侧 `4.8分 →`（点击跳转至信用详情，暂不实现）
  - `Settings` 设置 → 右侧 `→`（点击跳转至设置页，暂不实现）

#### 3.5.3 底部

- `"退出登录"` 文字按钮，灰色，居中，点击弹出确认框；确认后**清除本地 token 与用户信息**并跳转 `/login`。

---

### 3.6 登录 / 注册页（`/login`、`/register`）

#### 3.6.1 登录页

- 顶部返回按钮（←），无底部导航。
- 表单字段：手机号、密码。
- 提交：`POST /api/v1/auth/login`（公开接口）→ 成功后**保存 token 与用户信息到 localStorage**，跳回登录前页面（或首页）。
- 登录失败（如"手机号或密码错误"）：表单下方红字提示，不清空已填内容。
- 底部：`"还没有账号？去注册"` → `/register`。

#### 3.6.2 注册页

- 顶部返回按钮（←），无底部导航。
- 表单字段：手机号、密码、昵称。
- 提交：`POST /api/v1/auth/register`（公开接口）→ 成功后自动跳转 `/login`（提示"注册成功，请登录"）。
- 注册失败（手机号已注册 / 字段缺失）：红字提示。

---

### 3.7 全部消息页 (`/all-messages`，二级页)

> 入口：首页「最新消息」标题右侧的「全部消息 →」按钮。二级页（SimpleLayout + 返回按钮），标题 `"全部消息"`。

#### 3.7.1 标签页 + 「仅查看我的」开关

- **标签页**：`寻物 | 拾物`，默认选中**寻物**；切换后重置为第一页重新加载。
  - 寻物 tab → `GET /api/v1/find-items`
  - 拾物 tab → `GET /api/v1/lost-items`
- **「仅查看我的」开关**（默认关闭）：开启后请求附加 `mine=true`，仅返回当前用户发布的内容。
  - **未登录**：开关置灰（`opacity-50`），点击 `alert("请先登录")`，不切换。
  - 已登录：切换开关重置为第一页重新加载；切换时会通过 `router.replace` 同步 URL query（`mine=1` 表示开启），刷新 / 返回后状态保持一致。
  - **入口**：「我的」→「我的发布」携带 `?mine=1` 进入，开关自动开启（见 3.5.2）。

#### 3.7.2 列表与分页

- 每页 10 条，`sort=createTime,desc`（最新在上）。
- 底部「加载更多」按钮：追加下一页（按 id 去重）；`content.length < page.totalElements` 时显示。
- 列表项复用 `ItemListItem` 组件（`Search`/`Backpack` + 彩色标签 + 标题 + 地点 + 相对时间），点击跳详情 `/item/:id?type=seek|claim`。
- 空态文案：未开 mine → `"暂无寻物消息"` / `"暂无拾物消息"`；开 mine → `"您还没有发布寻物消息"` / `"您还没有发布拾物消息"`。

#### 3.7.3 数据形状

- 列表接口 data 为 `{ content: [...], page: { size, number, totalElements, totalPages } }`（Spring Boot 3.4 新 Page 序列化），前端取 `content` 与 `page.totalElements` 判断是否还有更多。

---

## 4. 帮帮柜介绍弹窗（非独立页面）

### 4.1 触发方式

- 首页点击 `Handshake` 帮帮柜入口时弹出。

### 4.2 弹窗内容

- **标题**：`"万能帮帮柜"`（前置 `Package` 图标）
- **功能说明**（文本）：
  - 24小时自助存取
  - 扫码即可开柜
  - 全程录像追溯
- **使用流程**（3步，带数字标号）：
  1. 小程序发布/匹配成功 → 获得取件码
  2. 到帮帮柜扫码/输码 → 柜门弹开
  3. 存入或取出物品 → 关闭柜门
- **部署位置**：`"图书馆、食堂、宿舍楼下、商场、社区"`
- **底部按钮**：`"我知道了"`，点击关闭弹窗。

---

## 5. 数据模型（TypeScript 接口）

### 5.1 前后端术语映射（重要，务必对照）

> 后端实体命名与直觉相反：**`LostItem`（`lost_item` 表）= 拾获物品（拾物招领）**，**`FindItem`（`find_item` 表）= 丢失物品（寻物启事）**。前端一律使用 `seek / claim`，禁止按名称猜测端点，以本表为准。

| 前端概念 | category 值 | 后端端点 | 后端表 / 实体 | 备注 |
| ---------- | ------------- | ---------- | --------------- | ------ |
| 寻物启事（我丢了东西） | `'seek'` | `POST /api/v1/find-items` | `find_item` / `FindItem` | 发布成功后可查匹配 |
| 拾物招领（我捡到东西） | `'claim'` | `POST /api/v1/lost-items` | `lost_item` / `LostItem` | 图片必填 |
| 首页"最新消息"列表 | — | 分别请求 `GET /api/v1/lost-items?page=0&size=6&sort=createTime,desc` 与 `GET /api/v1/find-items?page=0&size=6&sort=createTime,desc`，前端合并取前 6 | `lost_item` + `find_item` | 拾物 + 寻物混合 |
| "全部消息"页列表 | — | 按 tab：寻物 `GET /api/v1/find-items` / 拾物 `GET /api/v1/lost-items`，支持 `?page=&size=&sort=&mine=` | 按 tab 选择 | `mine=true` 需登录，未登录/无效 token → HTTP 401 |
| 物品详情 | — | `GET /api/v1/lost-items/{id}` 或 `GET /api/v1/find-items/{id}` | 按 type 选择 | — |
| 智能匹配（寻物→拾物） | — | `GET /api/v1/find-items/{id}/matches?limit=3` | — | `data = [{ item, score }]`，`item` 为拾物 LostItem |
| 智能匹配（拾物→寻物） | — | `GET /api/v1/lost-items/{id}/matches?limit=3` | — | `data = [{ item, score }]`，`item` 为寻物 FindItem |
| 注册 / 登录 | — | `POST /api/v1/auth/register` / `POST /api/v1/auth/login` | `user` | 返回 Bearer token |

### 5.2 类型定义

```typescript
// 发布类型：'seek' = 寻物启事（→ POST /api/v1/find-items）
//           'claim' = 拾物招领（→ POST /api/v1/lost-items）
type PublishCategory = 'seek' | 'claim';

// 物品发布（对应后端 LostItem / FindItem 实体）
interface PublishItem {
  id: number;                    // 后端 Long
  category: PublishCategory;     // 仅前端使用：决定调用哪个端点，不参与提交体
  title: string;                 // 物品名称（后端字段 title）
  description?: string;          // 详细描述
  location: string;              // 地点（后端为自由文本，前端用下拉枚举约束输入）
  contact?: string;              // 联系方式（可选，后端默认取发布者手机号）
  imageUrl?: string;             // 图片 URL（单图；claim 必填、seek 可选，后端字段 image_url）
  createTime: string;            // ISO 时间（后端字段 createTime）
  user?: UserProfile;            // 发布者（GET 详情/列表返回，后端嵌套 user 对象）
}

// 匹配结果（后端正/反向匹配共用，公开）：字段为 item（泛化前的 lostItem）
// - 寻物→拾物：GET /api/v1/find-items/{id}/matches → item 为 LostItem（拾物招领）
// - 拾物→寻物：GET /api/v1/lost-items/{id}/matches → item 为 FindItem（寻物启事）
interface MatchResult {
  item: PublishItem;             // 匹配到的物品（后端字段 item）
  score: number;                 // 匹配度 0.0 ~ 1.0，展示时 ×100 取整为百分比
}

// 消息（纯前端 Mock，后端暂未实现）
interface Message {
  id: string;
  title: string;
  summary: string;
  isRead: boolean;
  createdAt: string;             // ISO 时间
  type: 'match' | 'help' | 'system';
}

// 用户信息（后端 User 实体 + LoginResponse）
interface UserProfile {
  id: number;
  nickname: string;
  phone: string;                 // 用于联系方式预填 / "联系TA"
  avatar?: string;               // URL
  creditScore: number;           // 0-5
  publishCount: number;
  matchCount: number;
}

// 登录响应（后端 POST /api/v1/auth/login）
interface LoginResponse {
  token: string;
  userId: number;
  nickname: string;
}
```

---

## 6. 交互与状态管理要点

### 6.1 全局状态（建议使用 Pinia / Context）

- **token**：登录后保存到 `localStorage`（key 如 `wb_token`），作为唯一登录凭证。
- 当前用户信息（`UserProfile`）：从 `localStorage` 读取，登录/注册后写入。
- 未读消息数量（底部导航"消息"tab 红色角标；Mock 阶段本地维护）。
- 发布类型（`seek` / `claim`）在跳转发布页时通过 URL 参数传递。

### 6.2 路由守卫与认证

- **公开页**：首页 `/`、登录 `/login`、注册 `/register`、物品详情 `/item/:id`、匹配结果 `/match-result`（数据源为公开 GET 接口）。
- **需登录页**：发布 `/publish`、个人中心 `/profile`。
- 未登录访问需登录页 → 跳转 `/login`，登录成功后**回跳原页面**。
- 所有请求统一经过请求封装层：
  1. 自动附带 `Authorization: Bearer <token>`（token 不存在时仅对需登录接口拦截）。
  2. 响应为统一包装 `{ code, message, data }`，`code !== 200` 视为业务失败。
  3. **收到 HTTP 401**：清除 token 与用户信息 → 跳转 `/login`。

### 6.3 数据持久化

- token 与用户信息存 `localStorage`；退出登录时清除。
- 物品列表 / 详情 / 发布 / 匹配**对接真实后端 API**（见第 9 节），替换原 Mock。
- 消息通知页保持**纯前端 Mock**（后端未实现），"全部已读"仅更新本地状态。

---

## 7. 组件拆分建议（按页面）

### 7.1 公共组件

- `BottomNav`（底部导航，仅一级 tab 页使用：首页 / 消息 / 我的）
- `TopBar`（顶部导航，二级页使用：返回按钮 + 标题 + 右侧操作）
- `UploadArea`（单图上传组件）
- `MatchCard`（匹配结果卡片，分大小两种尺寸）
- `MessageItem`（消息列表项）

### 7.2 页面级组件

- `HomePage`
- `LoginPage`
- `RegisterPage`
- `PublishPage`（含类型选择子界面）
- `MatchResultPage`
- `MessagesPage`
- `ProfilePage`
- `ItemDetailPage`
- `CabinetDialog`（帮帮柜弹窗）

---

## 8. 开发注意事项

1. **图标**：统一使用 Lucide（`lucide-vue-next`），**禁止 Emoji 充当图标**（规范与全站映射见 1.4）；新增图标先到 lucide.dev 检索确认存在，换库只改 import 行。
2. **响应式**：页面适配移动端（375px ~ 428px 宽度），桌面端居中且最大宽度 `480px`。
3. **表单验证**：提交前校验必填字段，给出提示；联系方式非必填（预填手机号）；`claim` 类型图片必填。
4. **匹配度显示**：后端 `score` 为 0.0 ~ 1.0，展示时 ×100 取整为百分比；颜色根据数值变化（>80% 绿色，60-80% 橙色，<60% 灰色）。
5. **时间显示**：使用 `dayjs` 或 `date-fns` 将 `createTime` 转换为相对时间（如 "2小时前"）。
6. **弹窗**：帮帮柜弹窗为模态框，点击遮罩或"我知道了"关闭。
7. **认证**：所有写操作（POST）统一携带 `Authorization: Bearer <token>`；401 统一处理为跳转登录（见 6.2）。

---

## 9. 示例 API 接口（真实后端）

> 统一响应包装：`{ code, message, data }`（`code === 200` 为成功）。
> 列表接口的 `data` 为 Spring Data Page 结构（含 `content` / `totalElements` 等），前端取 `content`。
> 除标注"公开"外，**POST 均需携带 `Authorization: Bearer <token>`**。

```javascript
// ── 认证（公开）──
// 注册 → code 200
POST /api/v1/auth/register      { phone, password, nickname }
// 登录 → { token, userId, nickname }
POST /api/v1/auth/login         { phone, password }

// ── 拾物招领（lost_item，图片必填）──
// 拾物列表（公开）：data 为分页对象（{ content, page }），取 data.content；首页"最新消息"区按时间倒序取 6 条
GET  /api/v1/lost-items?page=0&size=6&sort=createTime,desc
// 拾物列表·仅查看我的（需登录，无/无效 token 返回 HTTP 401）
GET  /api/v1/lost-items?mine=true&page=0&size=10&sort=createTime,desc
// 拾物详情（公开）
GET  /api/v1/lost-items/{id}
// 发布拾物（需登录）
POST /api/v1/lost-items         { title, description?, location, contact?, imageUrl }

// ── 寻物启事（find_item，图片可选）──
// 寻物列表（公开）：首页"最新消息"区同样按时间倒序取 6 条
GET  /api/v1/find-items?page=0&size=6&sort=createTime,desc
// 寻物列表·仅查看我的（需登录，无/无效 token 返回 HTTP 401）
GET  /api/v1/find-items?mine=true&page=0&size=10&sort=createTime,desc
// 寻物详情（公开）
GET  /api/v1/find-items/{id}
// 发布寻物（需登录）
POST /api/v1/find-items         { title, description?, location, contact?, imageUrl }
// 寻物启事的智能匹配（公开）：data = [{ item: {...}, score: 0.0~1.0 }]，item 为拾物 LostItem
GET  /api/v1/find-items/{id}/matches?limit=3
// 拾物消息的智能匹配（公开）：data = [{ item: {...}, score: 0.0~1.0 }]，item 为寻物 FindItem
GET  /api/v1/lost-items/{id}/matches?limit=3

// ── AI 自动生成描述（需登录；每用户每分钟 5 次，超限 HTTP 429）──
// 上传图片后点击"自动生成描述"调用；data = { title, description }
POST /api/v1/ai/describe        { imageBase64, category?: "seek" | "claim" }

// ── 消息（后端未实现，前端保持 Mock）──
// 获取消息列表 / 标记全部已读：本地模拟数据 + 本地状态，不请求后端
```

---

## 10. 测试数据（用于演示）

- 拾物列表（首页）：见 3.1.3（黑色双肩包 / 白色iPhone / 钥匙串，均为"拾物招领"示例）。
- 匹配结果主卡片：黑色双肩包（95%，即 `score=0.95`），匹配地点"图书馆 2楼"，时间"2小时前"。
- 小卡片：白色水杯（78%，食堂），蓝色笔袋（62%，教学楼）。
- 消息列表：见 3.4.2（纯前端 Mock）。
- 个人中心：昵称"拾光者用户"，信用 4.8 分，发布 3 条，匹配 5 次。
- 登录 / 注册：任意未注册手机号可注册；登录页输入 `13800001111 / 123456`（后端冒烟数据）可登录。

---

文档结束
