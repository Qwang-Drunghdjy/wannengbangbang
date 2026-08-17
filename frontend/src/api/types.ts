/** 统一响应包装（后端 Result<T>） */
export interface Result<T> {
  code: number
  message: string
  data: T
}

/**
 * 发布类型（规范 5.1 术语映射）：
 * - 'seek'  = 寻物启事（我丢了东西）→ POST /api/v1/find-items
 * - 'claim' = 拾物招领（我捡到东西）→ POST /api/v1/lost-items
 * 注意：后端 LostItem=拾获物品、FindItem=丢失物品，命名与直觉相反。
 */
export type PublishCategory = 'seek' | 'claim'

/** 物品发布（对应后端 LostItem / FindItem 实体） */
export interface PublishItem {
  id: number
  /** 仅前端使用：决定调用哪个端点 / 页面上下文，不参与提交体 */
  category?: PublishCategory
  /** 物品名称（后端 title） */
  title: string
  description?: string | null
  location?: string | null
  /** 联系方式（可选，后端默认取发布者手机号） */
  contact?: string | null
  /** 图片 URL（单图；claim 必填、seek 可选） */
  imageUrl?: string | null
  /** ISO 时间（后端 createTime） */
  createTime?: string | null
  /** 发布者（GET 详情/列表返回，后端嵌套 user） */
  user?: UserProfile | null
}

/**
 * 匹配结果（后端正/反向匹配共用，字段 item）：
 * - 寻物→拾物：GET /api/v1/find-items/{id}/matches → item 为 LostItem（拾物招领）
 * - 拾物→寻物：GET /api/v1/lost-items/{id}/matches → item 为 FindItem（寻物启事）
 */
export interface MatchResult {
  item: PublishItem
  /** 匹配度 0.0 ~ 1.0，展示时 ×100 取整 */
  score: number
}

/** 消息（纯前端 Mock，后端暂未实现） */
export interface Message {
  id: string
  title: string
  summary: string
  isRead: boolean
  createdAt: string
  type: 'match' | 'help' | 'system'
}

/** 用户信息（后端 User + LoginResponse） */
export interface UserProfile {
  id: number
  nickname: string
  /** 用于联系方式预填 / "联系TA" */
  phone: string
  avatar?: string | null
  creditScore: number
  publishCount: number
  matchCount: number
}

/** 登录响应（后端 POST /api/v1/auth/login） */
export interface LoginResponse {
  token: string
  userId: number
  nickname: string
}

/** 分页元数据（Spring Boot 3.4 新 Page 序列化：data = { content, page }） */
export interface PageMeta {
  size: number
  number: number
  totalElements: number
  totalPages: number
}

/** 分页结构（列表接口 data 形状：content 在顶层，元数据在 page 子对象） */
export interface PageResult<T> {
  content: T[]
  page: PageMeta
}
