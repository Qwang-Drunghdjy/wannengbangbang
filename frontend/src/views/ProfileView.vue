<script setup lang="ts">
import { useRouter } from 'vue-router'
import { ClipboardList, Link, Settings, Star, User } from 'lucide-vue-next'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const router = useRouter()

function onLogout() {
  if (window.confirm('确定退出登录吗？')) {
    auth.logout()
    router.replace('/login')
  }
}
</script>

<template>
  <div>
    <div class="flex flex-col items-center bg-primary py-8 text-white">
      <span class="flex h-16 w-16 items-center justify-center rounded-full bg-white/20"
        ><User class="size-8" aria-hidden="true"
      /></span>
      <p class="mt-2 text-lg font-semibold">{{ auth.user?.nickname || '未登录' }}</p>
      <p class="mt-1 flex items-center gap-1 text-sm text-white/80">
        <Star class="size-4 fill-current" aria-hidden="true" />
        {{ auth.user?.creditScore ?? 0 }}分
      </p>
    </div>

    <div class="mt-3 divide-y divide-line rounded-lg bg-white">
      <router-link to="/all-messages?mine=1" class="flex items-center justify-between px-4 py-3">
        <span class="flex items-center gap-2">
          <ClipboardList class="size-5" aria-hidden="true" />
          我的发布
        </span>
        <span class="text-muted">{{ auth.user?.publishCount ?? 0 }}条 →</span>
      </router-link>
      <div class="flex items-center justify-between px-4 py-3">
        <span class="flex items-center gap-2">
          <Link class="size-5" aria-hidden="true" />
          我的匹配
        </span>
        <span class="text-muted">{{ auth.user?.matchCount ?? 0 }}次 →</span>
      </div>
      <div class="flex items-center justify-between px-4 py-3">
        <span class="flex items-center gap-2">
          <Star class="size-5" aria-hidden="true" />
          信用评分
        </span>
        <span class="text-muted">{{ auth.user?.creditScore ?? 0 }}分 →</span>
      </div>
      <div class="flex items-center justify-between px-4 py-3">
        <span class="flex items-center gap-2">
          <Settings class="size-5" aria-hidden="true" />
          设置
        </span>
        <span class="text-muted">→</span>
      </div>
    </div>

    <button
      class="mt-6 w-full rounded border border-line bg-white py-3 text-center text-sm text-muted"
      @click="onLogout"
    >
      退出登录
    </button>
  </div>
</template>
