# AGENTS.md - 万能帮帮

## 🎯 项目概述

"万能帮帮"是一个基于 **Spring Boot 3** 的失物招领 Web 应用。用户可以发布两类信息：**捡到的物品**（等待失主认领）和**丢失的物品**（寻物启事）。两张表字段一一对应，"丢失的物品" 的 "图片"字段为可选。

系统提供 **智能匹配** 功能：对每条寻物启事，基于中文分词（HanLP）和 Jaccard 相似度，从失物库中自动推荐最相似的 3 条，帮助用户快速定位可能匹配的物品。

- **后端**: Java 17, Spring Boot 3, Spring Data JPA, MySQL 8, Maven
- **分词**: HanLP portable-1.8.4（中文 NLP 工具包）
- **部署**: 微信云托管
- **前端**: Vue.js（移动端优先，运行在手机浏览器）

## 🛠️ 快速命令

| 场景 | 命令 |
| ------ | ------ |
| 编译 | `cd backend && mvn clean compile` |
| 测试 | `cd backend && mvn test` |
| 打包 | `cd backend && mvn clean package` |
| 本地启动 | `cd backend && mvn spring-boot:run` |

环境变量（微信云托管自动注入，本地可通过 IDE 或 export 设置）：

| 变量名 | 说明 | 默认值 |
| -------- | ------ | -------- |
| `MYSQL_ADDRESS` | MySQL 地址:端口 | `localhost:3306` |
| `MYSQL_USERNAME` | MySQL 用户名 | `root` |
| `MYSQL_PASSWORD` | MySQL 密码 | `root` |

## 📁 后端架构

```
backend/
    src/main/java/com/uang/backend/
        controller/     # REST 控制器 + 全局异常处理
        dto/            # 数据传输对象（Result<T> 统一响应体）
        entity/         # JPA 实体（LostItem / FindItem）
        repository/     # Spring Data JPA 仓库
        service/        # 业务逻辑层
    src/main/resources/
        application.yml # 数据源 & JPA 配置
    src/test/java/      # 单元测试（MockMvc / Mockito）
    pom.xml
```

## 🗄️ 核心数据模型

两表字段一一对应，职责不同：

### 失物表 (`lost_item`) — 捡到的物品

有人捡到物品后发布，供失主浏览认领。

| 字段 | 类型 | 必填 | 说明 |
| ------ | ------ | ------ | ------ |
| `id` | `Long` | — | 主键，自增 |
| `title` | `String(100)` | ✅ | 物品名称 |
| `description` | `TEXT` | ❌ | 物品描述 |
| `location` | `String(200)` | ❌ | 拾获地点 |
| `contact` | `String(100)` | ❌ | 联系方式 |
| `image_url` | `String(500)` | ✅ | 图片 URL |
| `create_time` | `LocalDateTime` | — | 发布时间（服务端赋值） |

### 寻物表 (`find_item`) — 丢失的物品

失主发布丢失物品的描述，供捡到者匹配。

| 字段 | 类型 | 必填 | 说明 |
| ------ | ------ | ------ | ------ |
| `id` | `Long` | — | 主键，自增 |
| `title` | `String(100)` | ✅ | 物品名称 |
| `description` | `TEXT` | ❌ | 物品描述 |
| `location` | `String(200)` | ❌ | 丢失地点 |
| `contact` | `String(100)` | ❌ | 联系方式 |
| `image_url` | `String(500)` | ❌ | 图片 URL（可选） |
| `create_time` | `LocalDateTime` | — | 发布时间（服务端赋值） |

## 🔌 API 设计规范

- 采用 **RESTful** 风格，统一返回 `Result<T>` 响应体：`{ "code": 200, "message": "success", "data": … }`
- 异常同样走 `Result.error(code, message)`，由 `GlobalExceptionHandler` 统一处理
- 分页使用 Spring Data `Pageable`（参数：`page`、`size`、`sort`）

### 失物接口（捡到的物品）— `/api/v1/lost-items`

| 方法 | 路径 | 说明 |
| ------ | ------ | ------ |
| `POST` | `/api/v1/lost-items` | 发布捡到的物品信息 |
| `GET` | `/api/v1/lost-items` | 分页查询，`?title=` 模糊搜索 |
| `GET` | `/api/v1/lost-items/{id}` | 查询单条详情 |

### 寻物接口（丢失的物品）— `/api/v1/find-items`

| 方法 | 路径 | 说明 |
| ------ | ------ | ------ |
| `POST` | `/api/v1/find-items` | 发布丢失物品的寻物启事 |
| `GET` | `/api/v1/find-items` | 分页查询，`?title=` 模糊搜索 |
| `GET` | `/api/v1/find-items/{id}` | 查询单条详情 |

### 智能匹配接口 — `/api/v1/find-items/{id}/matches`

| 方法 | 路径 | 说明 |
| ------ | ------ | ------ |
| `GET` | `/api/v1/find-items/{id}/matches?limit=3` | 对指定寻物启事，返回相似度最高的 N 条失物 |

响应格式：

```json
{
  "code": 200,
  "message": "success",
  "data": [
    { "lostItem": { ... }, "score": 0.72 },
    { "lostItem": { ... }, "score": 0.58 },
    { "lostItem": { ... }, "score": 0.41 }
  ]
}
```

## 🔍 智能匹配算法

### 核心思路

对一条寻物启事（FindItem），遍历所有失物（LostItem），计算每对文本的加权 Jaccard 相似度，取 Top-N。

### 分词

使用 **HanLP portable-1.8.4**（`com.hankcs:hanlp:portable-1.8.4`）对中文字段进行分词，得到词集合。

> 选择 portable 版的原因：免下载模型文件，jar 包自带词典，适合云托管环境。

### 相似度公式

```
Jaccard(A, B) = |A ∩ B| / |A ∪ B|
score = 0.6 × Jaccard(title) + 0.3 × Jaccard(description) + 0.1 × Jaccard(location)
```

| 字段 | 权重 | 说明 |
| ------ | ------ | ------ |
| title | 0.6 | 名称是匹配的核心 |
| description | 0.3 | 描述提供补充信息 |
| location | 0.1 | 地点相近也有参考价值 |

### 空字段处理

若某个字段在 FindItem 或 LostItem 中为 null / 空字符串，该字段的 Jaccard 相似度记为 **0**（不参与加权，但也不影响其他字段的计算）。

### Service 方法粒度

为方便后续加缓存注解（如 `@Cacheable`），`MatchingService` 方法拆分如下：

| 方法 | 职责 |
| ------ | ------ |
| `tokenize(String text)` | HanLP 分词，返回 `Set<String>` |
| `jaccard(Set<String> a, Set<String> b)` | 计算两个词集合的 Jaccard 相似度 |
| `fieldSimilarity(String a, String b)` | 单字段相似度（封装空值检查 + 分词 + Jaccard） |
| `calculateScore(FindItem, LostItem)` | 加权总分 |
| `findMatches(Long findItemId, int limit)` | 入口：验证寻物存在 → 加载全量失物 → 逐一打分排序 → 返回 Top-N |

## ⚙️ 编码与协作规范

- 遵循 Controller → Service → Repository 分层
- 使用 **Lombok**（`@Data`、`@NoArgsConstructor`、`@AllArgsConstructor`）
- 所有 public 方法写清晰的 Javadoc 注释
- 精确修改：只改任务相关的代码，不顺手重构或格式化无关内容

## 🧩 给 AI 的特别指引

- 数据库交互用 Spring Data JPA，禁止手写 SQL
- 返回 JSON，考虑移动端流量——避免冗余字段
- 图片上传后续对接微信云托管对象存储，当前先存 URL 字符串
- 计划完成之后要分步进行工作，每一步完成之后要告诉用户，等用户确认后再继续工作。
