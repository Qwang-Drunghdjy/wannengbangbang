# 物品详情页 · 顶部 kebab 菜单（仅菜单制作）

## Context（背景）

在物品详情页 `ItemDetailView` 的顶端栏最右侧加一个"三个点"（kebab）按钮，点击后从屏幕底部弹出菜单。
本次**只做菜单本身**（弹出 / 关闭 / 布局），菜单里的「编辑 / 删除 / 复制链接」三个功能按钮**暂不实现功能**（后续再做）。

## 现状（已读代码）

- 详情页由 `SimpleLayout` 布局渲染，其内部 `TopBar.vue` 已有右侧 `<slot name="right" />`，但 SimpleLayout 未往该 slot 塞内容，路由子组件（ItemDetailView）**够不到** TopBar 的 right slot。
- 弹窗参考：`CabinetDialog.vue` 用 `Teleport to="body"` + `fixed inset-0` 遮罩点 `@click.self` 关闭（居中对齐）；本次是**底部弹出的 bottom sheet**，样式不同但遮罩/关闭模式可复用。
- 图标一律用 `lucide-vue-next`，装饰性图标加 `aria-hidden`（规范 §1.4）。
- 详情页已有 `isOwner` 计算属性（当前登录用户 == 发布者）。

## 已确认（用户答复）

1. **kebab 与菜单所有访客可见**；但菜单内的动作按钮按权限显示：
   - 登录发布者本人（`isOwner`）：显示「编辑 删除 复制链接」三个按钮。
   - 非发布者本人（含未登录）：**仅显示「复制链接」** 一个按钮（同一行，居中）。
2. 「删除」用**红色 danger** 突出。
3. 三个功能按钮**点了完全没反应**（菜单保持打开，不做事）。
4. 图标：编辑=`Edit`、删除=`Trash2`（红）、复制链接=`Copy`；kebab 用 `MoreVertical`。

## Approach（方案）

**两个新东西**：

1. 让详情页内容注入到 TopBar 的右侧 slot（因为 ItemDetailView 是 route-view 子组件，够不到布局里的 TopBar slot）。
   - 方案：新建一个极简模块级 `topBarRight` shallowRef（组件），`SimpleLayout` 在 TopBar 的 `#right` 里渲染 `<component :is="topBarRight" />`；`ItemDetailView` 在 onMounted 设置、onUnmounted 清空。`TopBar.vue` 本身不用改。
   - kebab 按钮用 lucide `MoreVertical`（三个点，垂直）。
2. 新建底部弹出菜单组件（如 `ItemActionMenu.vue`）：
   - `Teleport to="body"` + `fixed inset-0 z-50` 遮罩，`@click.self` 关闭；
   - 底部白色圆角面板：**动作按钮行**（图标在上、文字在下；按 `isOwner` 过滤：本人三个 `编辑/删除/复制链接`，非本人仅 `复制链接` 居中）+ 一条独立分隔的「取消」按钮；
   - 三个按钮图标：编辑 `Edit`、删除 `Trash2`（红）、复制链接 `Copy`；
   - 收到 `open` prop、`isOwner` prop、`close` emit；点「取消」或遮罩关闭；
   - 本版点三个动作按钮**不做事、菜单保持打开**。

## Files（涉及文件）

- `frontend/src/composables/useTopBarRight.ts`（新，极简右槽注入）
- `frontend/src/layouts/SimpleLayout.vue`（TopBar 传入 `#right`）
- `frontend/src/components/ItemActionMenu.vue`（新，底部弹出菜单）
- `frontend/src/views/ItemDetailView.vue`（挂 kebab 注入 + 菜单开关）

## Reuse（复用）

- `TopBar.vue` 现有 `right` slot（无需改）。
- `CabinetDialog.vue` 的 `Teleport to="body"` + 遮罩点 `@click.self` 关闭模式。
- 图标统一 `lucide-vue-next`（官方规范 §1.4）。

## Steps（实现清单）

- [x] 建 `useTopBarRight` composable（模块级 shallowRef）
- [x] `SimpleLayout` 将 `topBarRight` 渲染进 TopBar 的 `#right`
- [x] 新建 `ItemActionMenu.vue`（底部弹出面板 + icon-over-text 动作行，按 `isOwner` 过滤按钮 + 独立「取消」）
- [x] `ItemDetailView` 挂载 kebab 注入 + `open/close` 状态，向菜单传 `isOwner`
- [x] 关闭方式：点「取消」、点遮罩外

## Verification（验证）

- `cd frontend && npm run build`（无类型 / 无 ESLint 错误）与 `npm run lint`
- 手动：进入 `/item/:id?type=claim|seek`，右上出现 kebab；点击弹底部菜单；点遮罩外 / 「取消」均关闭。
