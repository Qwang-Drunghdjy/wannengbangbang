# 详情页"智能匹配"功能开发计划

## Context

`ItemDetailView` 详情页（`/item/:id?type=seek|claim`）目前只展示物品信息 + "联系TA"按钮。
需求：增加 **"智能匹配"按钮**，点击后在页面下方显示匹配到的寻物/拾物消息，
匹配卡片**可点击查看详情**。

后端两个公开匹配端点已就绪（上一轮实现）：

- **seek**（寻物启事 / FindItem）→ `GET /api/v1/find-items/{id}/matches` → 匹配结果 item 为**拾物**（LostItem，前端类别 `claim`）
- **claim**（拾物招领 / LostItem）→ `GET /api/v1/lost-items/{id}/matches` → 匹配结果 item 为**寻物**（FindItem，前端类别 `seek`）

即：详情页类型为 `seek` 时匹配结果类别是 `claim`，反之亦然。

## Approach

- `api/items.ts` 新增 `fetchMatchesByLostItem(lostItemId, limit)`（对称补齐反向方向）。
- `ItemDetailView.vue` 增加"智能匹配"按钮 + 下方匹配结果区：
  - 按 `type` 选择调用 `fetchMatches`（seek）或 `fetchMatchesByLostItem`（claim）
  - 复用 `MatchCard` 组件渲染匹配卡片（图 / 标题 / ⭐匹配度 / 地点 / 时间）
  - 点击卡片 → 跳转 `/item/{matchedId}?type=<反向类别>` 查看该消息详情
- 交互与状态：加载中 / 空结果 / 错误 三态展示。

## 已确认的交互决策

1. **展开交互**：toggle——再次点击按钮收起匹配区
2. **匹配数量**：`limit=3`（与匹配结果页一致）
3. **卡片信息**：保留 ⭐ 匹配度百分比展示（与匹配结果页卡片一致）
4. **按钮位置**："联系TA"按钮下方，全宽，文案 "🔍 智能匹配"

## Files to modify

- `frontend/src/api/items.ts` — 新增 `fetchMatchesByLostItem`
- `frontend/src/views/ItemDetailView.vue` — 按钮 + 匹配结果区 + 点击跳转
- （视确认结果）`frontend/src/components/MatchCard.vue` — 点击/跳转支持

## Reuse

- `components/MatchCard.vue` — 匹配卡片展示（已通用化，`result.item` 字段正反向共用）
- `api/items.ts` 现有 `fetchMatches` / `get` — 反向函数对称实现
- 详情页导航模式 `/item/${id}?type=${category}`（`components/ItemListItem.vue`）
- 匹配结果页三态展示模式（`views/MatchResultView.vue`）

## Steps

- [ ] Step 1: `items.ts` 新增 `fetchMatchesByLostItem`（与 `fetchMatches` 对称，默认 limit=3）
- [ ] Step 2: `ItemDetailView.vue` 加"🔍 智能匹配"按钮（联系TA下方，全宽），点击 toggle 展开/收起匹配区
- [ ] Step 3: 匹配区三态：加载中 / 空结果（"未找到匹配消息"）/ 错误提示；成功时渲染 3 张 `MatchCard`（保留⭐匹配度）
- [ ] Step 4: 匹配卡片整体可点击 → `router.push` 或 `<router-link>` 跳转 `/item/{id}?type=<反向类别>`
- [ ] Step 5: 构建验证

## Verification

- `cd frontend && npm run build`（类型检查 + 构建）
- 本地起后端 + 前端联调：打开任一详情页 → 点"智能匹配"展开 → 下方出现 3 张匹配卡片（含⭐匹配度）→ 再次点击收起 → 点击卡片进入对应详情页（该页同样有匹配按钮）

## 待确认问题

~~已全部确认~~（toggle / limit=3 / 保留匹配度 / 按钮在联系TA下方全宽，文案"🔍 智能匹配"）
