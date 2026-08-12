import type { UserProfile } from '@/api/types'

const TOKEN_KEY = 'wb_token'
const USER_KEY = 'wb_user'

/** 读取本地 token */
export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY)
}

/** 写入本地 token */
export function setToken(token: string): void {
  localStorage.setItem(TOKEN_KEY, token)
}

/** 读取本地缓存的用户信息 */
export function getStoredUser(): UserProfile | null {
  const raw = localStorage.getItem(USER_KEY)
  if (!raw) return null
  try {
    return JSON.parse(raw) as UserProfile
  } catch {
    return null
  }
}

/** 写入本地缓存的用户信息 */
export function setStoredUser(user: UserProfile): void {
  localStorage.setItem(USER_KEY, JSON.stringify(user))
}

/** 清除登录态（token + 用户信息） */
export function clearAuth(): void {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(USER_KEY)
}
