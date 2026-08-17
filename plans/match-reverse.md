# 反向智能匹配（指定拾物 → 相似寻物）开发计划

## Context

现有正向匹配已上线：`GET /api/v1/find-items/{id}/matches`（寻物启事 → 相似失物/拾物）。
需求：补齐反向方向——**为指定拾物消息（LostItem）查找相似的寻物消息（FindItem）**，
即 `GET /api/v1/lost-items/{id}/matches`。

核心发现：现有 `MatchingService.calculateScore(FindItem fi, LostItem li)` 按
**title / description / location 同名字段** 加权 Jaccard，天然对称，
反向匹配可直接复用，**无需新算法**。

### 用户已确认的 4 项决策

1. **先出文档，确认后再实现**（本文档即该设计文档）
2. **`MatchResult` 泛化为 `MatchResult<T>` 统一复用**（不再新建 FindMatchResult）
3. **新端点加到现有 `MatchController`**
4. **不过滤发布者本人**（与正向匹配行为一致）

### ⚠️ 泛化的必然副作用（审阅重点）

一个泛型类只有一个序列化字段名：泛化后字段名 `lostItem` → `item`，
**正向匹配的 JSON 响应契约同步变更**：

```
变更前: { "lostItem": {…}, "score": 0.85 }
变更后: { "item": {…}, "score": 0.85 }
```

受影响范围（前端处于开发中，改动成本低，且统一后两个方向共用同一接口更简洁）：

| 文件 | 改动 |
| --- | --- |
| `frontend/src/api/types.ts` | `MatchResult` 接口 `lostItem` → `item` |
| `frontend/src/components/MatchCard.vue` | `result.lostItem` → `result.item`（5 处） |
| `frontend/src/views/MatchResultView.vue` | `r.lostItem.id` → `r.item.id` |
| `docs/frontend-development.md` | MatchResult 接口定义 + 示例响应 + API 表 |
| `AGENTS.md` | API 设计规范表新增反向匹配行 |

## Approach

- 泛化 `MatchResult` 为 `MatchResult<T>`（字段 `item`），正/反向共用。
- `MatchingService` 新增 `findMatchesByLostItem`，复用 `calculateScore(FindItem, LostItem)` 打分。
- `MatchController` 新增端点 `GET /api/v1/lost-items/{id}/matches?limit=3`（公开，GET 无需登录）。
- 权重公式沿用 `0.6×title + 0.3×desc + 0.1×loc`，`limit` 默认 3，与正向一致。
- 测试沿用现有 Mockito / MockMvc 风格，两套测试文件做「泛化适配 + 新增反向用例」。

## Files to Modify

### 后端（5 个）

| # | 文件 | 改动 |
| --- | --- | ------ |
| 1 | `backend/src/main/java/com/uang/backend/dto/MatchResult.java` | 泛化：`class MatchResult<T>`，字段 `LostItem lostItem` → `T item` |
| 2 | `backend/src/main/java/com/uang/backend/service/MatchingService.java` | 构造函数追加注入 `LostItemService` + `FindItemRepository`；新增 `findMatchesByLostItem(Long lostItemId, int limit)` |
| 3 | `backend/src/main/java/com/uang/backend/controller/MatchController.java` | 新增 `@GetMapping("/lost-items/{id}/matches")` |
| 4 | `backend/src/test/java/com/uang/backend/service/MatchingServiceTest.java` | setUp 构造器适配；`.getLostItem()` → `.getItem()`；新增反向用例 |
| 5 | `backend/src/test/java/com/uang/backend/controller/MatchControllerTest.java` | `new MatchResult<>(…)` 泛型适配；jsonPath `$.data[0].lostItem` → `$.data[0].item`；新增反向用例 |

### 前端（3 个，泛化副作用，字段改名）

| # | 文件 | 改动 |
| --- | --- | ------ |
| 6 | `frontend/src/api/types.ts` | `MatchResult` 接口 `lostItem: PublishItem` → `item: PublishItem` |
| 7 | `frontend/src/components/MatchCard.vue` | `props.result.lostItem` → `result.item` |
| 8 | `frontend/src/views/MatchResultView.vue` | `r.lostItem.id` → `r.item.id` |

### 文档（2 个）

| # | 文件 | 改动 |
| --- | --- | ------ |
| 9 | `AGENTS.md` | API 设计规范表新增 `GET /api/v1/lost-items/{id}/matches` 行 |
| 10 | `docs/frontend-development.md` | MatchResult 接口、示例响应 `lostItem` → `item`；新增反向端点说明 |

## Reuse（不修改语义，仅复用）

- `MatchingService.calculateScore(FindItem, LostItem)` — 对称算法，正反向共用
- `MatchingService.tokenize / jaccard / fieldSimilarity` — 不动
- `LostItemService.findById(Long)` — 验证拾物存在（与 `FindItemService.findById` 同款）
- `FindItemRepository.findAll()` / `LostItemRepository.findAll()` — 加载全量候选
- `Result<T>`、`GlobalExceptionHandler`、`WebMvcConfig`（GET 放行，无需改认证）— 不动
- `MatchResult` 现有使用点仅 3 个后端文件 + 2 个测试文件，均已列入修改清单

## 新增方法设计

```java
// MatchingService 新增（返回类型为泛化后的 MatchResult<FindItem>）
public List<MatchResult<FindItem>> findMatchesByLostItem(Long lostItemId, int limit) {
    LostItem lostItem = lostItemService.findById(lostItemId);   // 不存在 → RuntimeException
    List<FindItem> allFindItems = findItemRepository.findAll();
    return allFindItems.stream()
            .map(fi -> new MatchResult<>(fi, calculateScore(fi, lostItem)))  // 复用对称公式
            .sorted(Comparator.comparingDouble(mr -> mr.getScore()).reversed())  // 用 lambda 避免泛型方法引用歧义
            .limit(limit)
            .collect(Collectors.toUnmodifiableList());
}
```

```java
// MatchController 新增（与正向端点对称）
@GetMapping("/lost-items/{id}/matches")
public Result<List<MatchResult<FindItem>>> findMatchesByLostItem(
        @PathVariable Long id,
        @RequestParam(defaultValue = "3") int limit) {
    return Result.success(matchingService.findMatchesByLostItem(id, limit));
}
```

> 注意：泛化后 `MatchResult::getScore` 裸方法引用可能产生泛型推断歧义，
> 改用 lambda（`mr -> mr.getScore()`），正向 `findMatches` 内的写法同步调整。

## Steps

- [ ] **Step 1: 泛化 `MatchResult<T>`** — `dto/MatchResult.java`：类声明加 `<T>`，字段改 `T item`，Javadoc 更新
- [ ] **Step 2: `MatchingService` 扩展** — 构造器追加 `LostItemService` + `FindItemRepository`；新增 `findMatchesByLostItem`；正向 `findMatches` 返回类型改 `List<MatchResult<LostItem>>` 并适配 lambda 排序
- [ ] **Step 3: `MatchController` 新增端点** — `GET /api/v1/lost-items/{id}/matches?limit=3`；正向方法返回类型适配
- [ ] **Step 4: 更新 `MatchingServiceTest`** — setUp 注入两个新 mock；`.getLostItem()` → `.getItem()`；新增反向用例：正常排序 / 拾物不存在抛错 / 无寻物返回空
- [ ] **Step 5: 更新 `MatchControllerTest`** — 泛型 `new MatchResult<>(…)`；jsonPath 改 `item`；新增反向用例：正常 / 空结果 / 拾物不存在（`code=500`）
- [ ] **Step 6: 前端字段改名（3 文件）** — `types.ts` / `MatchCard.vue` / `MatchResultView.vue` 的 `lostItem` → `item`
- [ ] **Step 7: 文档更新** — `AGENTS.md` API 表 + `docs/frontend-development.md`（接口、示例响应、反向端点）
- [ ] **Step 8: 验证** — 后端编译 + 全量测试；前端 `npm run build` 类型检查

## Verification

```bash
# 后端
cd backend && .\mvnw.cmd clean compile   # → BUILD SUCCESS
cd backend && .\mvnw.cmd test            # → 0 failures, 0 errors（含新增反向用例）

# 前端（字段改名后类型检查）
cd frontend && npm run build

# 手动联调（可选，本地启动后端后）
curl.exe "http://localhost:8080/api/v1/lost-items/1/matches?limit=3"
# 预期：{ "code": 200, "data": [ { "item": {…}, "score": 0.0~1.0 } ] }；拾物 id 不存在 → code 500
```
