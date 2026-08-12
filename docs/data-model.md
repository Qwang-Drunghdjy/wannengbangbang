# 数据模型 - 万能帮帮

> 本文档是 `AGENTS.md` 的补充，包含完整的数据库表字段定义。

## 用户表 (`user`)

| 字段 | 类型 | 必填 | 说明 |
| ------ | ------ | ------ | ------ |
| `id` | `Long` | — | 主键，自增 |
| `phone` | `String(20)` | ✅ | 手机号，唯一，用于登录 |
| `password` | `String(200)` | ✅ | BCrypt 哈希密文 |
| `nickname` | `String(50)` | ✅ | 昵称 |
| `status` | `enum` | — | `NORMAL` / `BANNED`，默认 NORMAL |
| `create_time` | `LocalDateTime` | — | 注册时间（服务端赋值） |

## 失物表 (`lost_item`) — 捡到的物品

有人捡到物品后发布，供失主浏览认领。

| 字段 | 类型 | 必填 | 说明 |
| ------ | ------ | ------ | ------ |
| `id` | `Long` | — | 主键，自增 |
| `title` | `String(100)` | ✅ | 物品名称 |
| `description` | `TEXT` | ❌ | 物品描述 |
| `location` | `String(200)` | ❌ | 拾获地点 |
| `contact` | `String(100)` | ❌ | 联系方式（可选，默认取发布者手机号） |
| `image_url` | `String(500)` | ✅ | 图片 URL |
| `create_time` | `LocalDateTime` | — | 发布时间（服务端赋值） |
| `user_id` | `Long` | ✅ | 发布者 ID，外键关联 `user.id` |

## 寻物表 (`find_item`) — 丢失的物品

失主发布丢失物品的描述，供捡到者匹配。

| 字段 | 类型 | 必填 | 说明 |
| ------ | ------ | ------ | ------ |
| `id` | `Long` | — | 主键，自增 |
| `title` | `String(100)` | ✅ | 物品名称 |
| `description` | `TEXT` | ❌ | 物品描述 |
| `location` | `String(200)` | ❌ | 丢失地点 |
| `contact` | `String(100)` | ❌ | 联系方式（可选，默认取发布者手机号） |
| `image_url` | `String(500)` | ❌ | 图片 URL（可选） |
| `create_time` | `LocalDateTime` | — | 发布时间（服务端赋值） |
| `user_id` | `Long` | ✅ | 发布者 ID，外键关联 `user.id` |
