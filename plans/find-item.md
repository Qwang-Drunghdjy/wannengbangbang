# 寻物启事（FindItem）模块开发计划

## Context

参照已完成的 `LostItem` 模块，平行构建 `FindItem` 模块。两个模块字段一一对应，职责不同：LostItem 是捡到的物品（供失主认领），FindItem 是丢失的物品（寻物启事）。复用全局异常处理器、`Result<T>`、`application.yml` 等全部公共设施。

## Approach

完全镜像 `LostItem` 的实现模式：Entity → Repository → Service → Controller → Tests，逐层复制并替换类名/表名/路径/错误消息。

## 差异对照

| 维度 | LostItem | FindItem |
| ------ | ---------- | ---------- |
| 表名 | `lost_item` | `find_item` |
| API 路径 | `/api/v1/lost-items` | `/api/v1/find-items` |
| location 字段 Javadoc | 拾获地点 | 丢失地点 |
| 类名 | LostItem | FindItem |
| 错误提示 | "失物信息不存在" | "寻物信息不存在" |

## Files to Create (6 个)

| # | 文件 | 说明 |
| --- | ------ | ------ |
| 1 | `backend/src/main/java/com/uang/backend/entity/FindItem.java` | JPA 实体，表名 `find_item` |
| 2 | `backend/src/main/java/com/uang/backend/repository/FindItemRepository.java` | `JpaRepository<FindItem, Long>` + `findByTitleContaining` |
| 3 | `backend/src/main/java/com/uang/backend/service/FindItemService.java` | `create` / `findById` / `findAll` |
| 4 | `backend/src/main/java/com/uang/backend/controller/FindItemController.java` | `POST/GET/GET{id}`，路径 `/api/v1/find-items` |
| 5 | `backend/src/test/java/com/uang/backend/service/FindItemServiceTest.java` | Mock Repository，5 个测试用例 |
| 6 | `backend/src/test/java/com/uang/backend/controller/FindItemControllerTest.java` | MockMvc，4 个测试用例 |

## Reuse (不修改任何现有文件)

- `dto/Result.java` — 统一响应体
- `controller/GlobalExceptionHandler.java` — 全局异常处理
- `BackendApplication.java` — 启动类
- `application.yml` — 数据库配置
- `pom.xml` — 依赖管理
- 所有 `LostItem*` 文件 — 精确修改原则

## Steps

- [ ] **Step 1: 创建 FindItem Entity** — 镜像 `LostItem.java`，改表名为 `find_item`，Javadoc 中 "拾获地点" → "丢失地点"
- [ ] **Step 2: 创建 FindItemRepository** — 镜像 `LostItemRepository.java`，泛型替换为 `FindItem`
- [ ] **Step 3: 创建 FindItemService** — 镜像 `LostItemService.java`，错误消息 "失物信息不存在" → "寻物信息不存在"
- [ ] **Step 4: 创建 FindItemController** — 镜像 `LostItemController.java`，路径 `/api/v1/lost-items` → `/api/v1/find-items`
- [ ] **Step 5: 创建 FindItemServiceTest** — 镜像 `LostItemServiceTest.java`，5 个测试：create / findById(找到) / findById(未找到) / findAll(有title) / findAll(无title)
- [ ] **Step 6: 创建 FindItemControllerTest** — 镜像 `LostItemControllerTest.java`，4 个测试：create / list / getById(成功) / getById(404)
- [ ] **Step 7: 编译 & 测试验证** — `mvn clean compile` 和 `mvn test` 全部通过

## Verification

```bash
cd backend
mvn clean compile   # → BUILD SUCCESS
mvn test            # → 19 tests total (10 已有 + 9 新增), 0 failures
```
