import { post } from './request'
import type { LoginResponse } from './types'

export interface LoginPayload {
  phone: string
  password: string
}

export interface RegisterPayload {
  phone: string
  password: string
  nickname: string
}

/** 登录（公开）→ { token, userId, nickname } */
export function login(payload: LoginPayload): Promise<LoginResponse> {
  return post<LoginResponse>('/auth/login', payload)
}

/** 注册（公开）→ 成功 code 200，无 data */
export function register(payload: RegisterPayload): Promise<void> {
  return post<void>('/auth/register', payload)
}
