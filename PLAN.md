# 万能帮帮失物招领 — 后端开发计划

## 背景

根据 AGENTS.md 规划，为"万能帮帮"失物招领应用搭建后端核心功能。采用 Spring Boot 3.4.x + JPA + MySQL，前后端分离，最终部署微信云托管。

## 总体方案

在 `com.uang.backend` 包下按 controller / service / repository / entity / dto 分层搭建，实现失物信息的发布、列表查询（分页+标题搜索）、详情查询三个 RESTful API，统一使用 `Result<T>` 响应体。

---

> ✅ 全部 8 步骤已完成（含实施中补充的 3 项调整）。

## 实施结果

| 项目 | 状态 |
|------|------|
| `mvn compile` | ✅ BUILD SUCCESS |
| `mvn test` | ✅ 10 tests, 0 failures, 1 skipped (需 MySQL 的集成测试) |

## 实施中补充的调整

- **`BackendApplication.java`**：增加 `@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)`，消除 Page 序列化不稳定警告
- **`BackendApplicationTests.java`**：加 `@Disabled`（需 MySQL 连接），日常开发跳过
- **`GlobalExceptionHandler.java`**：新增 `@RestControllerAdvice` 将 `RuntimeException` 转为 `Result.error(500, …)`，保证异常也走统一响应格式
- **pom.xml**：`spring-boot-starter-webmvc` → `spring-boot-starter-web`，测试依赖合并为 `spring-boot-starter-test`（Spring Boot 3.x 标准命名）

---

## 文件变更清单

| 操作 | 文件路径 |
| ------ | --------- |
| 修改 | `backend/pom.xml` |
| 修改 | `backend/src/main/java/com/uang/backend/BackendApplication.java` |
| 修改 | `backend/src/test/java/com/uang/backend/BackendApplicationTests.java` |
| 删除 | `backend/src/main/resources/application.properties` |
| 新增 | `backend/src/main/resources/application.yml` |
| 新增 | `backend/src/main/java/com/uang/backend/entity/LostItem.java` |
| 新增 | `backend/src/main/java/com/uang/backend/repository/LostItemRepository.java` |
| 新增 | `backend/src/main/java/com/uang/backend/dto/Result.java` |
| 新增 | `backend/src/main/java/com/uang/backend/service/LostItemService.java` |
| 新增 | `backend/src/main/java/com/uang/backend/controller/LostItemController.java` |
| 新增 | `backend/src/main/java/com/uang/backend/controller/GlobalExceptionHandler.java` |
| 新增 | `backend/src/test/java/com/uang/backend/service/LostItemServiceTest.java` |
| 新增 | `backend/src/test/java/com/uang/backend/controller/LostItemControllerTest.java` |

---

## 详细步骤

### 步骤 1：修改 pom.xml — Spring Boot 版本降级

- 将 `spring-boot-starter-parent` 版本从 `4.1.0` 改为 `3.4.5`（3.4.x 最新稳定版）
- 增加 `spring-boot-starter-validation` 依赖（用于请求参数校验）
- 其余依赖保持不变

### 步骤 2：数据库配置 — application.yml

- 删除 `application.properties`
- 新建 `application.yml`，配置：
  - 数据源：`url`、`username`、`password` 使用 `${MYSQL_ADDRESS}`、`${MYSQL_USERNAME}`、`${MYSQL_PASSWORD}` 环境变量占位符
  - JPA：`hibernate.ddl-auto: update`（开发阶段自动建表），`show-sql: true`
  - 数据库名：`wannengbangbang`

### 步骤 3：创建 LostItem Entity

`com.uang.backend.entity.LostItem`：

| 字段 | 类型 | 注解 |
| ------ | ------ | ------ |
| `id` | `Long` | `@Id`, `@GeneratedValue(IDENTITY)` |
| `title` | `String` | `@Column(nullable=false)` |
| `description` | `String` | `@Column(columnDefinition="TEXT")` |
| `location` | `String` | — |
| `contact` | `String` | — |
| `imageUrl` | `String` | `@Column(name="image_url")` |
| `createTime` | `LocalDateTime` | `@Column(name="create_time")`, 由 Service 层赋值 |

- 使用 Lombok：`@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@Entity`, `@Table(name="lost_item")`
- `createTime` 不自动生成（不算审计字段），由 Service 层在创建时显式设置 `LocalDateTime.now()`

### 步骤 4：创建 LostItemRepository

`com.uang.backend.repository.LostItemRepository`：

- 继承 `JpaRepository<LostItem, Long>`
- 新增标题模糊搜索方法：`Page<LostItem> findByTitleContaining(String title, Pageable pageable)`

### 步骤 5：创建 Result 统一响应体

`com.uang.backend.dto.Result<T>`：

- `int code` — 状态码
- `String message` — 提示消息
- `T data` — 数据体
- 静态工厂方法：`Result.success(T data)` / `Result.error(int code, String message)`

### 步骤 6：创建 LostItemService

`com.uang.backend.service.LostItemService`：

- `LostItem create(LostItem item)` — 设置 `createTime`，保存
- `LostItem findById(Long id)` — 查询单个，不存在抛 `RuntimeException`
- `Page<LostItem> findAll(String title, Pageable pageable)` — 有 title 则模糊搜索，无则查全部

### 步骤 7：创建 LostItemController

`com.uang.backend.controller.LostItemController`：

- `@RestController` + `@RequestMapping("/api/v1/lost-items")`
- `POST /` → `Result<LostItem>` — 接收 `@RequestBody LostItem`
- `GET /` → `Result<Page<LostItem>>` — 接收 `@RequestParam(required=false) title` + `Pageable`
- `GET /{id}` → `Result<LostItem>` — 路径参数

### 步骤 8：编写单元测试

- `LostItemServiceTest`：Mock Repository，测试 create/findById/findAll
- `LostItemControllerTest`：MockMvc 测试三个接口的请求/响应

---

## 复用与注意事项

- 复用现有的 `BackendApplication.java`（无需修改）
- 复用 pom.xml 中已有的 Lombok / JPA / WebMvc / MySQL 依赖
- 遵循 AGENTS.md 中的 Lombok 规范、JSON 返回格式
- 数据库表名 `lost_item` 与 AGENTS.md 一致

---

## 验证方式

1. **构建**：`cd backend && mvn clean package`，确保编译通过
2. **测试**：`mvn test`，确保单元测试全部通过
3. **启动**：配置 MySQL 后 `mvn spring-boot:run`，用 curl / Postman 调用三个 API 验证
