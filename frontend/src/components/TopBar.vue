<script setup lang="ts">
import { useRouter } from 'vue-router'

defineProps<{ title?: string }>()

const router = useRouter()

/** 返回：优先 history.back()，无历史时兜底回首页（规范 1.3） */
function goBack() {
  if (window.history.length > 1) router.back()
  else router.replace('/')
}
</script>

<template>
  <header class="sticky top-0 z-10 flex h-12 items-center border-b border-line bg-white">
    <button
      class="flex h-full w-12 shrink-0 items-center justify-center text-xl text-ink"
      aria-label="返回"
      @click="goBack"
    >
      ←
    </button>
    <h1 class="flex-1 truncate text-center text-base font-semibold">{{ title }}</h1>
    <slot name="right" />
  </header>
</template>
