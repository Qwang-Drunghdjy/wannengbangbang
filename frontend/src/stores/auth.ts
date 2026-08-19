import { defineStore } from 'pinia'
import { login as loginApi, register as registerApi } from '@/api/auth'
import type { LoginPayload, RegisterPayload } from '@/api/auth'
import type { UserProfile } from '@/api/types'
import { clearAuth, getStoredUser, getToken, setStoredUser, setToken } from '@/utils/auth'

interface AuthState {
  token: string | null
  user: UserProfile | null
}

/** 认证状态：token 与用户信息持久化到 localStorage */
export const useAuthStore = defineStore('auth', {
  state: (): AuthState => ({
    token: getToken(),
    user: getStoredUser(),
  }),
  getters: {
    isLoggedIn: (state) => Boolean(state.token),
  },
  actions: {
    /** 登录：成功后保存 token + 用户信息 */
    async login(payload: LoginPayload): Promise<void> {
      const res = await loginApi(payload)
      this.token = res.token
      // 登录响应仅含 token/userId/nickname；其余字段待后端用户详情接口补充
      this.user = {
        id: res.userId,
        nickname: res.nickname,
        phone: '',
        creditScore: 0,
        publishCount: 0,
        matchCount: 0,
      }
      setToken(res.token)
      setStoredUser(this.user)
    },
    /** 注册（成功后跳登录页，由页面处理） */
    async register(payload: RegisterPayload): Promise<void> {
      await registerApi(payload)
    },
    /** 退出登录：清除本地登录态 */
    logout(): void {
      this.token = null
      this.user = null
      clearAuth()
    },
    /** 合并更新用户信息（如发布数刷新），同步持久化到 localStorage */
    updateUser(patch: Partial<UserProfile>): void {
      if (!this.user) return
      this.user = { ...this.user, ...patch }
      setStoredUser(this.user)
    },
    /** 清除登录态（401 时由请求层调用） */
    clear(): void {
      this.token = null
      this.user = null
      clearAuth()
    },
  },
})
