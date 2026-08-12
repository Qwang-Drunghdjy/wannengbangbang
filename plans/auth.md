# 用户认证（登录/注册）+ 物品归属关联 开发计划

## Context

按 `NEXT_STEPS.md` 要求，为"万能帮帮"实现用户系统：`User` 实体 + 手机号/密码登录注册 + JWT 认证（7 天过期），将现有 `LostItem`/`FindItem` 关联到发布者，并为 `POST` 接口加登录保护。技术方案已由 NEXT_STEPS 确定：BCrypt 存密码、jjwt 0.12.6、轻量 `HandlerInterceptor`（不引入完整 Spring Security）、GET 公开 / POST 需登录 / `/api/v1/auth` 公开。

本计划在 NEXT_STEPS 基础上补充了 3 处其未列明的必要改动（见「NEXT_STEPS 之外的补充」），其余严格按文档执行。

## Approach

分层新增用户认证模块（entity → repository → service → controller → config），再为 Lost/Find 物品挂接用户与拦截器，最后适配/新增测试。遵循「精确修改」原则：不动 `MatchingService` / `MatchController` / `GlobalExceptionHandler` / `MatchResult`（主代码）。

## NEXT_STEPS 之外的补充（3 处，均必要）

| # | 文件 | 原因 |
| --- | ------ | ------ |
| 补1 | `application.yml` | JwtUtil 需要 `${JWT_SECRET}`，必须新增 `jwt.secret` / `jwt.expire-days` 配置（NEXT_STEPS 未列，但 A4 依赖它） |
| 补2 | `controller/MatchControllerTest.java` | `@WebMvcTest` 会自动加载 `WebMvcConfigurer`/`HandlerInterceptor`，而普通 `@Component`（JwtUtil）不会 → 不补 `@MockitoBean JwtUtil`，上下文加载失败，该测试类会挂（NEXT_STEPS 的"不要修改"仅指主代码，不含测试） |
| 补3 | `entity/User.java` 内部枚举 | `status` 用 `User.Status` 内部枚举（NORMAL/BANNED）+ `@Enumerated(STRING)`，避免超出 NEXT_STEPS 的 8 个新文件清单 |

## Files to Create（8 个新文件，同 NEXT_STEPS Part A/B + E）

| # | 文件 | 说明 |
| --- | ------ | ------ |
| A1 | `entity/User.java` | 表 `user`：`id`/`phone`(unique)/`password`/`nickname`/`status`/`createTime`；`password` 加 `@JsonIgnore`（防泄露）；内部枚举 `Status{NORMAL,BANNED}` 默认 NORMAL；`createTime` 服务端赋值 |
| A2 | `repository/UserRepository.java` | `JpaRepository<User, Long>` + `Optional<User> findByPhone(String phone)`（风格同现有 Repository，带 `@Repository` + Javadoc） |
| A3 | `service/UserService.java` | `register`（校验字段非空 → 查重 → BCrypt 加密 → 保存）/ `login`（查手机号 → 校验密码 → 生成 JWT）/ `findById`（供 Lost/Find 关联用）；注入 `BCryptPasswordEncoder`（`spring-security-crypto`） |
| A4 | `config/JwtUtil.java` | `@Component`；`Keys.hmacShaKeyFor(secret)` 构造密钥；`generateToken(userId, phone)`（subject=userId，claim=phone，7 天过期，`signWith(key)`）/ `parseToken`（`parser().verifyWith(key).build().parseSignedClaims()`）/ `isTokenExpired`；密钥读 `${jwt.secret}`（即 `${JWT_SECRET}`） |
| A5 | `config/AuthInterceptor.java` | `@Component` implements `HandlerInterceptor`；非 POST 直接放行；校验 `Authorization: Bearer <token>` → `parseToken` → 成功注入 `request.setAttribute("userId", Long)`，失败写 401 |
| A6 | `dto/LoginRequest.java` | `phone` + `password`，Lombok `@Data` |
| A7 | `dto/RegisterRequest.java` | `phone` + `password` + `nickname` |
| A8 | `dto/LoginResponse.java` | `token` + `userId` + `nickname` |
| A9 | `config/WebMvcConfig.java` | `@Configuration` implements `WebMvcConfigurer`：注册 `AuthInterceptor` 于 `/api/v1/**`，排除 `/api/v1/auth/**` |
| B1 | `controller/AuthController.java` | `POST /api/v1/auth/register` → `Result.success(null)`（返回 200）；`POST /api/v1/auth/login` → `Result<LoginResponse>` |
| E1 | `service/UserServiceTest.java` | Mock Repository + Mock JwtUtil（用真实 `BCryptPasswordEncoder`）：注册成功（断言密文非明文）/ 手机号重复 / 登录成功 / 密码错误 / 手机号不存在 |
| E2 | `controller/AuthControllerTest.java` | `@WebMvcTest(AuthController.class)` + `@MockitoBean UserService` + `@MockitoBean JwtUtil`：注册成功 / 登录成功 / 注册缺少字段 |

## Files to Modify（同 NEXT_STEPS Part C/D + 补1/补2）

| # | 文件 | 改动 |
| --- | ------ | ------ |
| C1 | `entity/LostItem.java` | 加 `@ManyToOne @JoinColumn(name = "user_id") User user`（见决策 D5：可空外键）；`contact` 移除 `nullable=false`（当前本就无该约束，保持可选即可） |
| C2 | `entity/FindItem.java` | 同上 |
| C3 | `service/LostItemService.java` | 构造器加 `UserService`；`create(item, Long userId)`：`userService.findById(userId)` → `setUser`，`contact` 为空则取 `user.getPhone()` |
| C4 | `service/FindItemService.java` | 同上 |
| C5 | `controller/LostItemController.java` | `create` 加 `HttpServletRequest` 参数，`Long userId = (Long) request.getAttribute("userId")` 传入 Service |
| C6 | `controller/FindItemController.java` | 同上 |
| C7 | `pom.xml` | 加 `jjwt-api/impl/jackson:0.12.6`（impl+jackson 为 runtime）+ `spring-security-crypto`（版本由 Spring Boot parent 管理） |
| 补1 | `src/main/resources/application.yml` | 加 `jwt.secret: ${JWT_SECRET:<默认≥32字符>}` + `jwt.expire-days: 7`（HS256 要求密钥 ≥32 字节，默认值必须满足，生产用环境变量覆盖） |
| D1 | `service/LostItemServiceTest.java` | `new LostItemService(repo, userService)`；`create` 测试传入 userId 并 Mock `userService.findById`，断言 contact 默认取手机号 |
| D2 | `service/FindItemServiceTest.java` | 同上 |
| D3 | `controller/LostItemControllerTest.java` | 加 `@MockitoBean JwtUtil`（上下文必需）；POST 测试加 `Authorization: Bearer test-token` + stub `parseToken` 返回 subject=1 的 Claims；`service.create(item, 1L)` 断言 |
| D4 | `controller/FindItemControllerTest.java` | 同上 |
| 补2 | `controller/MatchControllerTest.java` | 仅加 `@MockitoBean JwtUtil`（GET 请求不需 header，仅保证上下文可加载）；断言逻辑零改动 |

## Reuse（不修改的文件）

- `dto/Result.java` — 统一响应体（拦截器 401 也用它写 body）
- `controller/GlobalExceptionHandler.java` — 不动；Service 层抛 `RuntimeException` 由其统一转 `Result.error(500, msg)`（注册/登录参数校验走此路径）
- `service/MatchingService.java` / `controller/MatchController.java` / `dto/MatchResult.java` — 禁止修改
- `repository/LostItemRepository.java` / `FindItemRepository.java` — 不动（`@ManyToOne` 用 `user_id` 外键，无需改查询）
- 现有测试构造方式 — `MatchingServiceTest` / `MatchControllerTest` 只用 `new LostItem()` 无参构造 + setter，不受实体新增字段影响（`@AllArgsConstructor` 签名变化无调用方）

## 关键实现细节（踩坑点）

1. **jjwt 0.12.6 是新 API**：构造 `Jwts.builder()...signWith(SecretKey).compact()`；解析 `Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload()`；旧 `setSigningKey`/`parseClaimsJws` 已废弃不可用。
2. **HS256 密钥长度**：`Keys.hmacShaKeyFor` 要求 ≥32 字节，否则启动抛 `WeakKeyException`；默认开发密钥必须 ≥32 字符。
3. **`@WebMvcTest` 拦截器链**：`@WebMvcTest` 会加载 `WebMvcConfigurer`/`HandlerInterceptor` 但排除普通 `@Component` → 4 个 `@WebMvcTest` 测试类都必须补 `@MockitoBean JwtUtil`，否则 `NoSuchBeanDefinitionException`。
4. **拦截器 401 响应**：interceptor 在 controller 之前运行，抛异常会走 `HandlerExceptionResolver` 而非 `GlobalExceptionHandler` → 直接 `response.setStatus(401)` + ObjectMapper 写 `Result.error(401, ...)` JSON，返回 false。
5. **拦截器放行规则**：方法级判断「非 POST 直接放行」+ 路径级排除 `/api/v1/auth/**`（双重保险，与 NEXT_STEPS「GET 全放行」一致）。
6. **`user` 表名**：MySQL 8.0 中 `user` 为非保留字，`@Table(name = "user")` 无需转义。
7. **`@ManyToOne` 默认 EAGER**：GET 列表/详情会自动 join 加载发布者（当前数据量小，无 N+1 担忧）；`open-in-view: false` 下 EAGER 保证事务内序列化不报 LazyInitializationException。

## 已确认的决策（用户已全部确认）

- **D1 BANNED 用户登录**：✅ 登录时拒绝 `BANNED` 用户，返回"账号已被封禁"
- **D2 登录失败提示**：✅ 手机号不存在 / 密码错误统一返回"手机号或密码错误"（防账号枚举）
- **D3 认证失败 HTTP 状态**：✅ 拦截器返回 HTTP 401 + body `Result.error(401, "未登录或登录已过期")`
- **D4 物品响应的 user 字段**：✅ `@ManyToOne` EAGER 嵌套发布者对象（`password` 已 `@JsonIgnore`），列表/详情 JSON 暴露发布者 id/phone/nickname 等
- **D5 user_id 外键可空性**（技术权衡，已定）：实体层 `@JoinColumn(name="user_id")` 可空 + 应用层保证（POST 必须登录）；数据模型文档标注"必填"保持不变

## Steps

- [x] **Step 1: pom.xml 加依赖** — jjwt-api / jjwt-impl(runtime) / jjwt-jackson(runtime) 0.12.6 + spring-security-crypto
- [x] **Step 2: application.yml 加 JWT 配置** — `jwt.secret`（`${JWT_SECRET:默认≥32字符}`）+ `jwt.expire-days: 7`
- [x] **Step 3: 创建 User 实体 + 内部 Status 枚举**（A1）
- [x] **Step 4: 创建 UserRepository**（A2）
- [x] **Step 5: 创建 LoginRequest / RegisterRequest / LoginResponse**（A6-A8）
- [x] **Step 6: 创建 JwtUtil**（A4，jjwt 0.12.6 新 API）
- [x] **Step 7: 创建 AuthInterceptor**（A5）+ **WebMvcConfig**（A9）
- [x] **Step 8: 创建 UserService**（A3）
- [x] **Step 9: 创建 AuthController**（B1）
- [x] **Step 10: LostItem/FindItem 实体加 `@ManyToOne user`**（C1/C2）
- [x] **Step 11: LostItemService/FindItemService 的 create 接 userId**（C3/C4）
- [x] **Step 12: LostItemController/FindItemController 提取 request attribute userId**（C5/C6）
- [x] **Step 13: 新增 UserServiceTest**（E1）
- [x] **Step 14: 新增 AuthControllerTest**（E2）
- [x] **Step 15: 适配 LostItemServiceTest / FindItemServiceTest**（D1/D2）
- [x] **Step 16: 适配 LostItemControllerTest / FindItemControllerTest / MatchControllerTest**（D3/D4/补2）
- [x] **Step 17: 编译 + 测试验证** — `mvn clean compile` → `mvn test`

## Verification

```bash
cd backend
mvn clean compile   # → BUILD SUCCESS
mvn test            # → 所有测试通过，0 failures / 0 errors
```

手工冒烟（可选，需本地 MySQL）：

```bash
# 注册
curl -X POST :8080/api/v1/auth/register -H "Content-Type: application/json" \
  -d '{"phone":"13800001111","password":"123456","nickname":"张三"}'
# 登录 → 取 token
curl -X POST :8080/api/v1/auth/login -H "Content-Type: application/json" \
  -d '{"phone":"13800001111","password":"123456"}'
# 未带 token 发布 → 401
curl -X POST :8080/api/v1/lost-items -H "Content-Type: application/json" \
  -d '{"title":"黑色钱包","description":"在图书馆捡到","location":"图书馆","image_url":"https://..."}'
# 带 token 发布 → 200，且 user_id / contact 自动填充
curl -X POST :8080/api/v1/lost-items -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"title":"黑色钱包","description":"在图书馆捡到","location":"图书馆","image_url":"https://..."}'
```
