# 下一步执行要点清单

**先读 `AGENTS.md`** 建立项目心智模型，然后按本文档执行。

## 任务：实现用户认证（登录/注册）+ 物品归属关联

建设用户系统（User 实体 + JWT 认证），将现有 `LostItem` / `FindItem` 关联到发布者，并为 `POST` 接口加上登录保护。

### 技术方案（已确定）

| 决策点 | 方案 |
| ------ | ------ |
| 登录方式 | 手机号 + 密码 |
| 密码存储 | BCrypt |
| 认证机制 | JWT（jjwt 0.12.6），7 天过期，轻量拦截器 |
| 用户标识 | `User` 实体：`id`/`phone`/`password`/`nickname`/`status`/`createTime` |
| 物品归属 | `LostItem`/`FindItem` 各加 `@ManyToOne User`，`contact` 改为可选 |
| 权限 | GET 公开，POST 需登录，`/api/v1/auth` 公开 |

---

### Part A：新建文件（8 个）

| # | 文件 | 说明 |
| --- | ------ | ------ |
| A1 | `entity/User.java` | JPA 实体，表名 `user`，字段：`id`/`phone`(unique)/`password`/`nickname`/`status`(NORMAL,BANNED)/`createTime` |
| A2 | `repository/UserRepository.java` | `JpaRepository<User, Long>` + `Optional<User> findByPhone(String phone)` |
| A3 | `service/UserService.java` | `register`（BCrypt 加密密码） + `login`（校验密码 + 生成 JWT） |
| A4 | `config/JwtUtil.java` | `generateToken` / `parseToken` / `isTokenExpired`，读取 `${JWT_SECRET}` |
| A5 | `config/AuthInterceptor.java` | `HandlerInterceptor`：从 `Authorization` Header 提取 JWT → 解析 userId → 注入 `request.setAttribute("userId", ...)` |
| A6 | `dto/LoginRequest.java` | `phone` + `password` |
| A7 | `dto/RegisterRequest.java` | `phone` + `password` + `nickname` |
| A8 | `dto/LoginResponse.java` | `token` + `userId` + `nickname` |

### Part B：新建认证接口（2 个端点）

| # | 文件 | 端点 | 说明 |
| --- | ------ | ------ | ------ |
| B1 | `controller/AuthController.java` | `POST /api/v1/auth/register` | 注册成功返回 200 |
| B2 | （同上） | `POST /api/v1/auth/login` | 返回 `LoginResponse` |

### Part C：修改已有文件 — 物品关联用户 + 认证保护（8 个）

| # | 文件 | 改动 |
| --- | ------ | ------ |
| C1 | `entity/LostItem.java` | 加 `@ManyToOne` `user` 字段，`contact` 列移除 `nullable=false`（改为可选） |
| C2 | `entity/FindItem.java` | 同上 |
| C3 | `service/LostItemService.java` | `create` 方法加 `Long userId` 参数，从数据库查 User 并 `setUser`；如 `contact` 为空则默认取 `user.getPhone()` |
| C4 | `service/FindItemService.java` | 同上 |
| C5 | `controller/LostItemController.java` | `create` 端点：从 `request.getAttribute("userId")` 提取 userId 传入 Service |
| C6 | `controller/FindItemController.java` | 同上 |
| C7 | `config/WebMvcConfig.java`（新建） | 注册 `AuthInterceptor`，拦截 `/api/v1/**`，放行 `GET`/`auth` |
| C8 | `pom.xml` | 加 `jjwt-api:0.12.6` + `jjwt-impl:0.12.6` + `jjwt-jackson:0.12.6` + `spring-security-crypto` |

### Part D：修改已有测试 — 适配新签名（4 个）

| # | 文件 | 改动 |
| --- | ------ | ------ |
| D1 | `service/LostItemServiceTest.java` | `create` 测试传入 `userId`，Mock User 查询 |
| D2 | `service/FindItemServiceTest.java` | 同上 |
| D3 | `controller/LostItemControllerTest.java` | `POST` 测试加 `Authorization` Header，Mock 认证 |
| D4 | `controller/FindItemControllerTest.java` | 同上 |

### Part E：新增测试（2 个）

| # | 文件 | 说明 |
| --- | ------ | ------ |
| E1 | `service/UserServiceTest.java` | Mock Repository + Mock JwtUtil，覆盖注册/登录/手机号重复/密码错误 |
| E2 | `controller/AuthControllerTest.java` | MockMvc，覆盖注册/登录/注册缺少字段 |

---

## 开发注意事项

- **注册端点公开**，不经过拦截器（拦截器放行 `/api/v1/auth/**`）
- `BCryptPasswordEncoder` 来自 `spring-security-crypto`，不引入全套 Spring Security
- 拦截器仅拦截 `POST`，`GET` 全部放行
- **不要修改** `MatchingService`、`MatchController`、`GlobalExceptionHandler`、`MatchResult`
- LostItem 的 `image_url` 保持必填（现有约束不变）
- 精确修改原则：只改任务相关，不改无关代码

## 验证标准

```
mvn clean compile  → BUILD SUCCESS
mvn test           → 所有测试通过（不能有 failures）
```

## 第三方依赖坐标

```xml
<!-- JWT -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.6</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>

<!-- BCrypt -->
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-crypto</artifactId>
</dependency>
```
