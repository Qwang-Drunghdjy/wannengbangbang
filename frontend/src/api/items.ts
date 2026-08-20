import { get, post } from './request'
import type { MatchResult, PageResult, PublishItem } from './types'

export interface PublishPayload {
  title: string
  description?: string
  location?: string
  contact?: string
  imageUrl?: string
}

/** 发布拾物招领（claim）→ POST /api/v1/lost-items（需登录，imageUrl 必填） */
export function createLostItem(payload: PublishPayload): Promise<PublishItem> {
  return post<PublishItem>('/lost-items', payload)
}

/** 发布寻物启事（seek）→ POST /api/v1/find-items（需登录） */
export function createFindItem(payload: PublishPayload): Promise<PublishItem> {
  return post<PublishItem>('/find-items', payload)
}

/** 分页查询参数（page / size / title 模糊搜索 / sort 排序，如 'createTime,desc' / mine 仅查看我的，需登录） */
export interface PageQuery extends Record<string, unknown> {
  page?: number
  size?: number
  title?: string
  sort?: string
  /** 仅查看当前用户发布的内容（需 Bearer token，后端 mine=true） */
  mine?: boolean
}

/** 拾物列表（claim，拾物招领） */
export function fetchLostItems(params: PageQuery = {}): Promise<PageResult<PublishItem>> {
  return get<PageResult<PublishItem>>('/lost-items', params)
}

/** 寻物列表（seek，寻物启事） */
export function fetchFindItems(params: PageQuery = {}): Promise<PageResult<PublishItem>> {
  return get<PageResult<PublishItem>>('/find-items', params)
}

/** 拾物详情 */
export function fetchLostItem(id: number | string): Promise<PublishItem> {
  return get<PublishItem>(`/lost-items/${id}`)
}

/** 寻物详情 */
export function fetchFindItem(id: number | string): Promise<PublishItem> {
  return get<PublishItem>(`/find-items/${id}`)
}

/** 更新拾物认领状态（仅发布者本人）→ POST /api/v1/lost-items/{id}/claim */
export function updateLostItemClaimed(id: number | string, claimed: boolean): Promise<PublishItem> {
  return post<PublishItem>(`/lost-items/${id}/claim`, { claimed })
}

/** 更新寻物认领状态（仅发布者本人）→ POST /api/v1/find-items/{id}/claim */
export function updateFindItemClaimed(id: number | string, claimed: boolean): Promise<PublishItem> {
  return post<PublishItem>(`/find-items/${id}/claim`, { claimed })
}

/** 智能匹配（寻物 → 拾物） */
export function fetchMatches(findItemId: number | string, limit = 3): Promise<MatchResult[]> {
  return get<MatchResult[]>(`/find-items/${findItemId}/matches`, { limit })
}

/** 智能匹配（拾物 → 寻物） */
export function fetchMatchesByLostItem(
  lostItemId: number | string,
  limit = 3,
): Promise<MatchResult[]> {
  return get<MatchResult[]>(`/lost-items/${lostItemId}/matches`, { limit })
}
