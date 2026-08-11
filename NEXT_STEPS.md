# 下一步执行要点清单

**先读 `AGENTS.md`** 建立项目心智模型。

## 任务：实现寻物启事（find_item）模块

参照 `LostItem` 的完整链路，平行构建 `FindItem`：

| # | 文件 | 说明 |
| --- | ------ | ------ |
| 1 | `entity/FindItem.java` | JPA 实体，表名 `find_item`，字段与 `LostItem` 一一对应 |
| 2 | `repository/FindItemRepository.java` | `JpaRepository<FindItem, Long>` + `findByTitleContaining` |
| 3 | `service/FindItemService.java` | `create` / `findById` / `findAll`，逻辑同 `LostItemService` |
| 4 | `controller/FindItemController.java` | 三接口：`POST/GET/GET{id}`，路径 `/api/v1/find-items` |
| 5 | `service/FindItemServiceTest.java` | Mock Repository，覆盖 create / findById / findAll / notFound |
| 6 | `controller/FindItemControllerTest.java` | MockMvc，覆盖三个端点 + 404 异常用例 |

## 验证标准

```
mvn clean compile  → BUILD SUCCESS
mvn test           → 所有新增测试通过
```

## 注意事项

- 复用全局异常处理器 `GlobalExceptionHandler`（无需再写）
- 复用 `Result<T>`、`application.yml`、`BackendApplication`
- 不修改 `LostItem` 相关代码（精确修改原则）
