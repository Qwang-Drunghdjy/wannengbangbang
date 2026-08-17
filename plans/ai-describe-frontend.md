# 计划：前端「自动生成描述」功能

## Context（为什么做）

后端已完成 `POST /api/v1/ai/describe`（见 `plans/ai-describe.md`，88 tests 全绿 + 冒烟通过）。
本计划实现前端接入：发布页上传图片后一键生成关键词描述，填入 title/description，加快发布效率并提升匹配质量（description 权重 30%、title 权重 60%）。

## 需求（已与用户确认 ✅）

- [x] `UploadArea` 增加 **canvas 压缩**：选图后压缩为 JPEG（宽边 ≤1024、质量 0.8），输出**纯 base64**（剥离 data URL 前缀，对齐后端 `DescribeImageRequest.imageBase64` 要求）
- [x] `PublishView` 增加 **「✨ 自动生成描述」按钮**：
  - 仅当已上传图片时可用（seek 无图置灰/隐藏）
  - 点击 → loading 态（按钮禁用 + "生成中..."）→ 调后端 → 成功填充 title/description
  - 失败 → 页内提示，**不阻塞手动发布**
  - 传 `category`（seek/claim 由当前发布类型决定，后端差异化 prompt）
- [x] 新 API 函数 + 类型（`api/ai.ts`、`types.ts`）
- [x] AI 接口单独超时（后端 read-timeout 30s，前端 axios 默认 10s 不够 → 该请求用 30s）
- [x] **Q1（填充规则）**：`title` 仅当为空时填充；`description` 总是覆盖（可再编辑）
- [x] **Q2（换图处理）**：重新选图 → 清空 `description`（保留 title）；删除图片 → 同时清空 `description`

## Approach（推荐方案）

```text
[UploadArea] 选图 → Image 解码 → canvas 压缩(≤1024, JPEG 0.8) → 剥离前缀得纯 base64
    │ emit change({ previewUrl, base64 })   （previewUrl 仍用于本地预览；发布提交逻辑本次不变）
    ▼
[PublishView] 有图 → 显示「✨ 自动生成描述」按钮
    │ 点击 → generating=true
    ▼
api/ai.ts  describeImage({ imageBase64, category })   （request 实例 + timeout 30s）
    ▼
成功 → 填充 title / description（规则待确认 Q1）→ 按钮恢复
失败 → error 页内提示 → 按钮恢复（发布不受影响）
```

- **压缩实现**（UploadArea 内部，无新依赖）：

  ```ts
  const img = await loadImage(file)          // URL.createObjectURL → new Image
  canvas 宽边 ≤ 1024（等比缩放）
  canvas.toDataURL('image/jpeg', 0.8)        // 体积远小于 4MB 后端上限
  base64 = dataUrl.split(',')[1]             // 剥离 data:image/jpeg;base64, 前缀
  ```

- **按钮位置**：详细描述输入框上方一行（左侧按钮，右侧可放辅助说明），样式沿用项目 Tailwind 风格。
- **UI 细节**：生成中按钮禁用 + `生成中...`；成功后按钮显示 `已生成`（短暂）或保持可用；**Q2 已确认**：重新选图 → 清空 description（保留 title）；删除图片 → 清空 description。

## Files to modify

- `frontend/src/components/UploadArea.vue` — canvas 压缩 + emit 形状改为 `{ previewUrl, base64 }`
- `frontend/src/views/PublishView.vue` — 持有 base64；新增生成按钮（loading/失败兜底/填充）
- `frontend/src/api/ai.ts` — **新增**：`describeImage()` 调用 + 30s 超时
- `frontend/src/api/types.ts` — **新增** `DescribeResult { title, description }`（对齐后端 DescribeImageResult）

## Reuse（复用现有代码）

- `frontend/src/api/request.ts` — 默认导出 axios 实例（拦截器自动带 token、401 跳登录、解包 `Result<T>`）；AI 请求需 `timeout: 30000`，用实例直接调 `request.post(url, body, { timeout })`（现有 `post()` 封装不支持第三个参数）
- `frontend/src/api/types.ts` — `PublishCategory`（seek/claim）用于 category 参数
- `PublishView.vue` 现有 `error` ref + 红色提示样式 — 复用做失败兜底提示
- `UploadArea.vue` 现有文件选择/预览/删除逻辑 — 保留，仅扩展压缩与输出

## Steps

- [x] 1. `types.ts` 新增 `DescribeResult`
- [x] 2. `api/ai.ts` 新增 `describeImage(payload)`（request 实例 + timeout 30s，错误透传）
- [x] 3. `UploadArea.vue`：选图后 canvas 压缩（宽边 ≤1024、JPEG 0.8）→ emit `{ previewUrl, base64 }`；删除 emit null
- [x] 4. `PublishView.vue`：接收 base64；新增「✨ 自动生成描述」按钮（有图才可用；loading；成功填充；失败页内提示）
- [x] 5. `npm run build`（vue-tsc 类型检查 + vite build）通过
- [x] 6. `npm run lint` 通过
- [x] 7. 联调验证（后端 8080 + 前端 dev 5173 代理链路）：401 无 token ✓；无 Key 友好错误透传 ✓（成功路径需 GLM_API_KEY，待用户配置后浏览器验证）

## Verification

- `cd frontend && npm run build` → 无类型错误
- `cd frontend && npm run lint` → 无 ESLint 错误
- 手动联调（后端 `spring-boot:run` + 配置 `GLM_API_KEY`）：
  1. seek/claim 发布页上传图片 → 「自动生成描述」按钮出现
  2. 点击 → loading → 成功填充 title + description（可编辑）
  3. 无图时按钮不可用
  4. 未配置 Key / 断网 → 页内错误提示，发布仍可用
  5. 连续点击（限流 429）→ 提示"操作过于频繁"
  6. 未登录点击 → 跳登录

## 待确认问题

已全部确认（Q1 方案 A、Q2 方案 A），无需待确认项。
