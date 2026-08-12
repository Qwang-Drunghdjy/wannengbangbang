import axios, { type AxiosError } from 'axios'
import { clearAuth, getToken } from '@/utils/auth'
import type { Result } from './types'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE ?? '/api/v1',
  timeout: 10000,
})

// 请求拦截：自动附带 Bearer token
request.interceptors.request.use((config) => {
  const token = getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 响应拦截：解包 Result<T>；业务失败（code !== 200）reject；HTTP 401 清登录态并跳登录
request.interceptors.response.use(
  (response) => {
    const body = response.data as Result<unknown>
    if (body && typeof body.code === 'number' && body.code !== 200) {
      return Promise.reject(new Error(body.message || '请求失败'))
    }
    return response
  },
  async (error: AxiosError) => {
    if (error.response?.status === 401) {
      clearAuth()
      const { default: router } = await import('@/router')
      const current = router.currentRoute.value
      if (current.path !== '/login') {
        await router.replace({ path: '/login', query: { redirect: current.fullPath } })
      }
    }
    return Promise.reject(error)
  },
)

/** GET 请求，返回已解包的 data */
export async function get<T>(url: string, params?: Record<string, unknown>): Promise<T> {
  const res = await request.get<Result<T>>(url, { params })
  return res.data.data
}

/** POST 请求，返回已解包的 data */
export async function post<T>(url: string, body?: unknown): Promise<T> {
  const res = await request.post<Result<T>>(url, body)
  return res.data.data
}

export default request
