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

/** 拾物列表（首页"最近拾物"） */
export function fetchLostItems(
  params: { page?: number; size?: number; title?: string } = {},
): Promise<PageResult<PublishItem>> {
  return get<PageResult<PublishItem>>('/lost-items', params)
}

/** 寻物列表 */
export function fetchFindItems(
  params: { page?: number; size?: number; title?: string } = {},
): Promise<PageResult<PublishItem>> {
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

/** 智能匹配（寻物 → 拾物） */
export function fetchMatches(findItemId: number | string, limit = 3): Promise<MatchResult[]> {
  return get<MatchResult[]>(`/find-items/${findItemId}/matches`, { limit })
}
