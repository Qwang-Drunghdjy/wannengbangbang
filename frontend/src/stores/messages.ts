import { defineStore } from 'pinia'
import type { Message } from '@/api/types'

// 后端暂无消息接口，本 store 为纯前端 Mock（规范 3.4）
const MOCK_MESSAGES: Message[] = [
  {
    id: '1',
    title: '【匹配成功】您的背包已找到！',
    summary: '拾物者已存入帮帮柜，请凭取件码...',
    isRead: false,
    createdAt: '10分钟前',
    type: 'match',
  },
  {
    id: '2',
    title: '【互助请求】求教高数题',
    summary: '同学你好，看到你发布的信息...',
    isRead: true,
    createdAt: '2小时前',
    type: 'help',
  },
  {
    id: '3',
    title: '【系统通知】您的物品已存放超时',
    summary: '请于24小时内取回...',
    isRead: true,
    createdAt: '1天前',
    type: 'system',
  },
]

/** 消息状态（纯前端 Mock，"全部已读"仅更新本地状态） */
export const useMessagesStore = defineStore('messages', {
  state: () => ({
    messages: [...MOCK_MESSAGES] as Message[],
  }),
  getters: {
    unreadCount: (state) => state.messages.filter((m) => !m.isRead).length,
  },
  actions: {
    markAllRead(): void {
      this.messages.forEach((m) => {
        m.isRead = true
      })
    },
    markRead(id: string): void {
      const msg = this.messages.find((m) => m.id === id)
      if (msg) msg.isRead = true
    },
  },
})
