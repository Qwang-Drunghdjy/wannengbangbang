# 开发环境与运维手册

> 本文件收纳从 `AGENTS.md` 下沉的开发细节：本机工具链、部署地址、踩坑记录、curl 冒烟模板。
> ⚠️ 新会话开发前务必先读「1. 本机开发环境」，避免试错。

## 1. 本机开发环境（Windows 11 + PowerShell 7）

工具链与常见 Linux/macOS 环境差异较大：

| 工具 | 状态 | 说明 |
| ------ | ------ | ------ |
| **bash 工具** | ✅ 实测可用（2026-08） | Git 已加入 PATH 后 bash 工具可正常使用；Windows 命令用 `cmd //c "..."` 调用，路径用 `/d/WOD/...` 风格 |
| **替代 shell** | ✅ | 文件/HTTP 操作用 **ctx_execute（JavaScript/Node）** 或 `cmd /c`；`curl.exe` 在 `C:\Windows\System32\curl.exe` |
| **Maven** | ⚠️ 未全局安装 | `mvn` 不在 PATH。用项目自带 wrapper：`cd backend && .\mvnw.cmd <目标>`（首次运行会自动下载 Maven） |
| **Java** | ✅ | JDK 17，`JAVA_HOME=D:\Program Files\Java\jdk-17` |
| **Node / npm** | ✅ | Node v24.17.0 / npm 12.0.1（前端构建直接 `npm run build`） |
| **Git** | ✅ | `D:\Program Files\Git\cmd\git.exe`，已加入 PATH |

**API 联调约定**：用 `ctx_execute`（JS `fetch`）直接打已部署后端，或 `curl.exe`；不要在会话里反复试探 bash。

## 2. 部署地址（临时，测试用）

- 后端（微信云托管）：`https://wannengbangbang-back-294764-10-1466165089.sh.run.tcloudbase.com`
- 前端（EdgeOne Pages）：`https://wannengbangbang-j74p1pmq.edgeone.cool`（访问需 `eo_token`，临时）

> 临时地址可能变更，部署配置以云托管控制台为准。

## 3. 已踩坑记录（不要再犯）

1. **`VITE_API_BASE` 必须带 `/api/v1` 后缀**：前端 `request.ts` 的 `baseURL = VITE_API_BASE ?? '/api/v1'`，`auth.ts` 里路径是 `/auth/register`（不含前缀）。部署时若 `VITE_API_BASE` 只填域名 → 请求打到 `/auth/register`（缺前缀）→ 网关 403 / CORS 报错。正确值：`https://…sh.run.tcloudbase.com/api/v1`
2. **后端 JSON 字段是驼峰**：发布拾物用 `imageUrl`（不是 `image_url`）；Jackson 不认蛇形
3. **CORS 已配置**：`WebMvcConfig.addCorsMappings` 允许所有源（生产建议收紧为前端域名）
4. **`MethodArgumentNotValidException` 在 `org.springframework.web.bind` 包**（不是 `jakarta.validation`），import 别写错
5. **Spring Boot 3.4 已移除 `ClientHttpRequestFactories` 类**（3.2/3.3 有）；RestClient 设超时改用 `JdkClientHttpRequestFactory`（`setReadTimeout(Duration)`）+ `HttpClient.newBuilder().connectTimeout(...)`
6. **一个 Bean 有多个构造函数时须 `@Autowired` 标注**（如 `RateLimitService` 的测试构造），否则启动报 “No default constructor found”
7. **Git Bash 中 `2>nul` 会创建真实空文件 `nul`**（Windows 保留名在 MSYS 下不生效）→ 抑制输出用 `2>/dev/null`
8. **本机 MySQL57 服务可能未启动且 root 密码非默认 `root`**：本地冒烟先 `net start MySQL57`，启动后端前设置 `MYSQL_PASSWORD=<真实密码>`（向项目所有者确认，**勿写进文档/代码**）；应用启动需数据库 `wannengbangbang` 已存在

## 4. 本地开发及验证流程

闭环：**改代码 → `mvnw.cmd clean compile` → `mvnw.cmd spring-boot:run` → curl 验证 → `mvnw.cmd test`**

```bash
# 1. 注册（公开）
curl -X POST :8080/api/v1/auth/register -H "Content-Type: application/json" \
  -d '{"phone":"13800001111","password":"123456","nickname":"张三"}'

# 2. 登录（公开）→ 取 TOKEN；token 缺失/过期时发布接口返回 401
curl -X POST :8080/api/v1/auth/login -H "Content-Type: application/json" \
  -d '{"phone":"13800001111","password":"123456"}'

# 3. 发布拾物（需带 token，注意字段为驼峰 imageUrl）
curl -X POST :8080/api/v1/lost-items -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"title":"黑色钱包","description":"在图书馆捡到","location":"图书馆","imageUrl":"https://..."}'

# 4. AI 自动生成描述（需登录；未配置 GLM_API_KEY 时返回友好错误；同一用户连续第 6 次返回 429）
curl -X POST :8080/api/v1/ai/describe -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"imageBase64":"<压缩后的图片 base64>","category":"seek"}'
```

日志：Spring Boot 默认输出到控制台（application.yml 未配置文件日志）。
