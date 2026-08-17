import { isAxiosError } from 'axios'
import request from './request'
import type { DescribeResult, PublishCategory, Result } from './types'

/** AI 自动生成描述请求体 */
export interface DescribePayload {
  /** 压缩后的图片纯 base64（不含 data URL 前缀） */
  imageBase64: string
  /** 发布类型：seek=寻物 / claim=拾物（后端差异化 prompt） */
  category?: PublishCategory
}

/**
 * 自动生成物品描述 → POST /api/v1/ai/describe（需登录）
 * 30s 超时（AI 生成较慢）；后端业务错误（400/429/500）透传其 message
 */
export async function describeImage(payload: DescribePayload): Promise<DescribeResult> {
  try {
    const res = await request.post<Result<DescribeResult>>('/ai/describe', payload, {
      timeout: 30000,
    })
    return res.data.data
  } catch (e) {
    if (isAxiosError(e)) {
      const message = (e.response?.data as Result<unknown> | undefined)?.message
      if (message) throw new Error(message, { cause: e })
    }
    throw e
  }
}
