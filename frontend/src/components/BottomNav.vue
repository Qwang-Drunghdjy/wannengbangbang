<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useMessagesStore } from '@/stores/messages'

const route = useRoute()
const messagesStore = useMessagesStore()

const tabs = [
  { path: '/', label: '首页', icon: '🏠' },
  { path: '/messages', label: '消息', icon: '🔔' },
  { path: '/profile', label: '我的', icon: '👤' },
]

const activePath = computed(() => route.path)
</script>

<template>
  <!-- 一级 tab 底部导航：桌面端限宽 480px 居中 -->
  <nav
    class="fixed bottom-0 left-1/2 z-20 flex h-[60px] w-full max-w-[480px] -translate-x-1/2 items-stretch border-t border-line bg-white"
  >
    <router-link
      v-for="tab in tabs"
      :key="tab.path"
      :to="tab.path"
      class="relative flex flex-1 flex-col items-center justify-center gap-0.5 text-xs"
      :class="activePath === tab.path ? 'text-primary' : 'text-muted'"
    >
      <span class="relative text-xl leading-none">
        {{ tab.icon }}
        <span
          v-if="tab.path === '/messages' && messagesStore.unreadCount > 0"
          class="absolute -right-2 -top-1 flex h-4 min-w-4 items-center justify-center rounded-full bg-danger px-1 text-[10px] leading-none text-white"
        >
          {{ messagesStore.unreadCount }}
        </span>
      </span>
      <span>{{ tab.label }}</span>
    </router-link>
  </nav>
</template>
