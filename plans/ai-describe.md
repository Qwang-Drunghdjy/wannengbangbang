# 计划：AI 自动生成物品描述（后端）

## Context（为什么做）

发布寻物/拾物时用户常留空 description，而匹配系统权重为
`0.6×Jaccard(title) + 0.3×Jaccard(description) + 0.1×Jaccard(location)`，
留空即浪费 30% 匹配权重。本功能让用户上传图片后一键调用免费视觉模型
**GLM-4V-Flash**（智谱）生成「物品名称建议 + 关键词描述」，填入表单，加快发布效率并提升匹配质量。

后端新增一个需登录的 AI 代理接口（API Key 只存在服务端，绝不下发前端）。

## 需求（已与用户确认 ✅）

- [x] **Q1** 同时生成「物品名称建议（title）」+「关键词描述（description）」，前端一键填入（title 占匹配权重 60%，AI 建议更规范）
- [x] **Q2** 区分 `seek`/`claim` 使用不同 prompt（claim=外观特征便于认领；seek=独有标识便于匹配）
- [x] **Q3** 后端简单限流：**每用户每分钟 5 次**（内存滑动窗口，无新依赖），超限返回 429
- [x] **Q4** 图片以 base64 提交给后端（前端压缩；后端不落盘、只转发给 GLM；正式对象存储上传另立计划）
- [x] **Q5** `GLM_API_KEY` 环境变量留空默认值：**没有 Key 也能编译/测试/正常运行**，仅 AI 接口返回友好错误；部署到微信云托管只需在控制台配置环境变量 `GLM_API_KEY`，无需改代码（详见下方「Q5 说明」）
- [x] 接口 `POST /api/v1/ai/describe`，需登录（复用 JWT 拦截器）；**不引入智谱 SDK**，用 Spring 内置 `RestClient` 裸调，AiService 对模型调用做薄封装，换模型只改一处

## Q5 说明（部署方式确认）

- 本意正是如此：代码中 `glm.api-key: ${GLM_API_KEY:}` 为空时，**应用照常启动、其他功能不受影响**；仅调用 AI 接口时返回友好错误 `AI 服务未配置（GLM_API_KEY 未设置）`，不会崩溃。
- 部署到微信云托管：控制台 → 服务配置 → 环境变量 → 添加 `GLM_API_KEY=<智谱 Key>` → 重启生效，**无需重新构建或改代码**。
- 你唯一需要做的：到 <https://open.bigmodel.cn> 用手机号注册（免费）→ 控制台创建 API Key（免费，GLM-4V-Flash 调用本身也免费）。

## Approach（推荐方案）

```text
POST /api/v1/ai/describe  (JWT 拦截器自动拦截，需登录)
  body: { "imageBase64": "...", "category": "seek" | "claim" }
  ↓
AiController → AiService（限流 → 组装 prompt → 调 GlmClient → 解析 JSON → DTO）
  ↓
GlmClient（薄封装：RestClient → open.bigmodel.cn/api/paas/v4/chat/completions，
          Bearer <GLM_API_KEY>，超时集中在此）
  ↓
返回 { "title": "黑色钱包", "description": "黑色 皮质 男士 拉链 长款" }
```

- **薄封装设计**：
  - `GlmClient`：只做 HTTP 调用 + 鉴权 + 超时；入参 OpenAI 兼容 `messages`，出参原始文本；`apiKey` 为空时抛友好异常。换模型只改这一个类。
  - `AiService`：业务编排——限流、prompt 组装（seek/claim 差异化）、JSON 解析、异常转友好信息。`GlmClient` 构造注入，测试可 mock。
- **限流**：`RateLimitService`（`ConcurrentHashMap<Long, Deque<Long>>` 滑动窗口 60s，上限 `glm.rate-limit-per-minute` 默认 5，线程安全），超限抛 `RateLimitException` → 429。
- **Prompt 设计**（AiService 常量）：
  - system：角色 = 失物招领平台物品识别助手；要求输出简短中文关键词短语（空格分隔，利于 HanLP 分词匹配）；禁止"这是一张图片…"式废话与长句；只输出 JSON。
  - user（claim）：识别拾获物品，输出 `{"title": "物品名称", "description": "外观特征关键词"}`，重点：颜色/材质/品牌/大小/新旧，便于失主认领。
  - user（seek）：识别丢失物品，输出 `{"title": "物品名称", "description": "独有标识关键词"}`，重点：品牌/颜色/尺寸/刻字/图案/破损痕迹，便于与拾物匹配。
  - 请求参数：`response_format: {"type":"json_object"}`、`max_tokens: 256`、`temperature: 0.3`（低随机、稳定）。
- **配置**（application.yml，沿用 `@Value` 风格）：

  ```yaml
  glm:
    api-key: ${GLM_API_KEY:}          # 空 → AI 接口返回友好错误，其余功能正常
    model: glm-4v-flash
    base-url: https://open.bigmodel.cn/api/paas/v4
    read-timeout: 30s                 # GLM 免费版响应较慢，需放宽
    rate-limit-per-minute: 5
  ```

- **校验与错误**：
  - DTO 校验：`imageBase64` `@NotBlank` + `@Size(max=4MB)`（前端压缩后通常 <500KB）；`category` 可选，非法值按 claim 风格处理。
  - `GlobalExceptionHandler` **新增两个 handler**（现只有 RuntimeException→500、Unauthorized→401）：
    - `MethodArgumentNotValidException` → HTTP 400 + `Result.error(400, 首个字段错误信息)`（现有代码未用 @Valid，需补齐）
    - `RateLimitException` → HTTP 429 + `Result.error(429, "操作过于频繁，请稍后再试")`

## Files to modify

新增（`backend/src/main/java/com/uang/backend/`）：

- `dto/DescribeImageRequest.java` — 请求体（imageBase64 必填 + 校验；category 可选）
- `dto/DescribeImageResult.java` — 响应体（title + description）
- `client/GlmClient.java` — 薄封装 HTTP 调用（RestClient）
- `service/AiService.java` — 业务编排（限流/prompt/解析）
- `service/RateLimitService.java` — 内存滑动窗口限流
- `exception/RateLimitException.java` — 限流异常（→ 429）
- 测试：`controller/AiControllerTest.java`、`service/AiServiceTest.java`、`service/RateLimitServiceTest.java`

修改：

- `backend/src/main/resources/application.yml` — 新增 `glm.*` 配置节
- `backend/src/main/java/com/uang/backend/controller/GlobalExceptionHandler.java` — 新增 400 / 429 两个 handler
- `backend/pom.xml` — **零新增依赖**（RestClient + Jackson 均为 Spring Boot 内置）

## Reuse（复用现有代码）

- `Result<T>`（dto/Result.java）— 统一响应
- `AuthInterceptor`（config/AuthInterceptor.java）— POST 自动拦截；`request.getAttribute(USER_ID_ATTR)` 取 userId（限流 key）
- `GlobalExceptionHandler`（controller/GlobalExceptionHandler.java）— 扩展而非新建
- `JwtUtil`（config/JwtUtil.java）— `@Value("${...}")` 配置读取风格
- 测试模式：`@WebMvcTest` + `@MockitoBean` + MockMvc（参考 LostItemControllerTest；@WebMvcTest 需 mock JwtUtil）
- Spring Boot 3.4 内置 `RestClient` + Jackson `ObjectMapper`

## Steps

- [x] 1. application.yml 增加 `glm.*` 配置（api-key / model / base-url / read-timeout / rate-limit-per-minute）
- [x] 2. 新增 DTO：DescribeImageRequest（jakarta validation 注解）、DescribeImageResult
- [x] 3. 新增 `RateLimitException` + `RateLimitService`（滑动窗口，限流逻辑独立可测）
- [x] 4. 新增 `GlmClient` 薄封装：RestClient 调 chat/completions，Bearer 鉴权、超时、apiKey 为空友好报错、返回原始文本
- [x] 5. 新增 `AiService`：限流 → 组装 system/user prompt（seek/claim 分支）→ 调 GlmClient → 解析 JSON → DescribeImageResult；解析失败转友好异常
- [x] 6. 新增 `AiController`：`POST /api/v1/ai/describe`（`@Valid`，构造注入 AiService）
- [x] 7. `GlobalExceptionHandler` 新增 `MethodArgumentNotValidException`(400) 与 `RateLimitException`(429) handler
- [x] 8. 测试：AiControllerTest（401 / 200 / 400 参数校验）、AiServiceTest（mock GlmClient 验证 prompt 分支与解析）、RateLimitServiceTest（窗口内放行/超限拒绝）＋ GlmClientTest（无 Key 友好报错）
- [x] 9. `mvnw.cmd clean compile` + `mvnw.cmd test` 全绿（88 tests, 0 failures）
- [x] 10. 本地 `spring-boot:run` + curl 冒烟（见 Verification）—— 全部通过：401 / 400 / 无 Key 友好错误(500) / 限流第 6 次 429

## Verification

- `cd backend && .\mvnw.cmd clean compile` → BUILD SUCCESS
- `cd backend && .\mvnw.cmd test` → 0 failures, 0 errors
- curl 冒烟：
  1. 无 token POST → 401
  2. 带 token + 空/超限 imageBase64 → 400 参数错误
  3. 带 token + 合法 base64 + **未配置 GLM_API_KEY** → 500 + 友好错误「AI 服务未配置（GLM_API_KEY 未设置）」
  4. 带 token + 合法 base64 + 已配置 Key → 200 + `{ title, description }`
  5. 同一用户 1 分钟内第 6 次调用 → 429
- 前端暂不接入（本次纯后端），后续前端计划再联调
