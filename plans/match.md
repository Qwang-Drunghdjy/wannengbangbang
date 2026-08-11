# 智能匹配（Match）模块开发计划

## Context

基于已完成的 `LostItem` / `FindItem` 模块，新增智能匹配功能：对任意寻物启事（FindItem），遍历全部失物（LostItem），使用 HanLP 中文分词 + 加权 Jaccard 相似度，返回 Top-N 匹配结果。

## Approach

新增独立的 `MatchingService`（分词/相似度/加权/排序）和 `MatchController`（REST 端点），不改动任何现有 LostItem/FindItem 代码。遵循 AGENTS.md 定义的方法粒度拆分为 5 个方法。

## Files to Create (5)

| # | 文件 | 说明 |
| --- | ------ | ------ |
| 1 | `dto/MatchResult.java` | 匹配结果 DTO：`LostItem lostItem` + `double score` |
| 2 | `service/MatchingService.java` | 5 个方法：tokenize / jaccard / fieldSimilarity / calculateScore / findMatches |
| 3 | `controller/MatchController.java` | `GET /api/v1/find-items/{id}/matches?limit=3` |
| 4 | `service/MatchingServiceTest.java` | 覆盖所有方法：分词/相似度/空字段/加权/查无此人 |
| 5 | `controller/MatchControllerTest.java` | MockMvc：正常匹配 / 空结果 / 寻物不存在 |

## Files to Modify (1)

| # | 文件 | 说明 |
|---|------|------|
| 1 | `pom.xml` | 新增 `com.hankcs:hanlp:portable-1.8.4` 依赖 |

## Reuse（不修改的文件）

- 所有 `LostItem*` / `FindItem*` 文件 — 精确修改原则
- `Result.java` — 统一响应体
- `GlobalExceptionHandler.java` — 全局异常处理
- `BackendApplication.java` / `application.yml` — 启动和配置
- `LostItemRepository.java` — MatchingService 会注入它来加载全量失物

## MatchingService 方法设计

| 方法 | 签名 | 职责 |
| ------ | ------ | ------ |
| `tokenize` | `Set<String> tokenize(String text)` | HanLP 分词 → 词集合 |
| `jaccard` | `double jaccard(Set<String> a, Set<String> b)` | Jaccard 相似度 |
| `fieldSimilarity` | `double fieldSimilarity(String a, String b)` | 封装空值检查 + 分词 + Jaccard |
| `calculateScore` | `double calculateScore(FindItem fi, LostItem li)` | 0.6×title + 0.3×desc + 0.1×loc |
| `findMatches` | `List<MatchResult> findMatches(Long findItemId, int limit)` | 入口：验证 → 全量打分 → 排序截断 |

## Steps

- [ ] **Step 1: pom.xml 加 HanLP 依赖** — `com.hankcs:hanlp:portable-1.8.4`
- [ ] **Step 2: 创建 MatchResult DTO** — `LostItem` + `double score`，Lombok `@Data` `@AllArgsConstructor`
- [ ] **Step 3: 创建 MatchingService** — 5 个方法，注入 `FindItemService` + `LostItemRepository`
- [ ] **Step 4: 创建 MatchController** — `@RestController`，注入 `MatchingService`
- [ ] **Step 5: 创建 MatchingServiceTest** — Mock FindItemService + LostItemRepository，覆盖 tokenize/jaccard/fieldSimilarity/calculateScore/findMatches + 空值场景
- [ ] **Step 6: 创建 MatchControllerTest** — MockMvc + `@MockitoBean MatchingService`，覆盖正常匹配 / 空结果 / 寻物不存在
- [ ] **Step 7: 编译 & 测试验证** — `mvn clean compile` → `mvn test`

## Verification

```bash
cd backend
mvn clean compile   # → BUILD SUCCESS
mvn test            # → 所有测试通过（含新增 MatchingServiceTest + MatchControllerTest）
```
