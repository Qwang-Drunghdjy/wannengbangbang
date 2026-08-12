<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { fetchFindItem, fetchLostItem } from '@/api/items'
import type { PublishItem } from '@/api/types'
import { relativeTime } from '@/utils/time'

const route = useRoute()
const type = (route.query.type as string) ?? 'claim'
const isClaim = computed(() => type === 'claim')

const item = ref<PublishItem | null>(null)
const error = ref('')

onMounted(async () => {
  try {
    const id = route.params.id as string
    item.value = isClaim.value ? await fetchLostItem(id) : await fetchFindItem(id)
  } catch (e) {
    error.value = e instanceof Error ? e.message : '加载失败'
  }
})

function contact() {
  if (!item.value) return
  window.alert(`联系方式：${item.value.contact || item.value.user?.phone || '暂无'}`)
}
</script>

<template>
  <div v-if="item" class="space-y-4">
    <div
      class="flex h-56 items-center justify-center overflow-hidden rounded bg-slate-100 text-6xl"
    >
      <img v-if="item.imageUrl" :src="item.imageUrl" class="h-full w-full object-cover" alt="" />
      <span v-else>📦</span>
    </div>
    <h1 class="text-xl font-bold">{{ item.title }}</h1>
    <p class="text-sm text-muted">{{ isClaim ? '拾到地点' : '丢失地点' }}：{{ item.location }}</p>
    <p v-if="item.description" class="text-sm text-ink">{{ item.description }}</p>
    <p class="text-xs text-muted">发布时间：{{ relativeTime(item.createTime) }}</p>
    <p class="text-xs text-muted">发布者：{{ item.user?.nickname ?? '匿名' }}</p>
    <button class="w-full rounded bg-primary py-3 text-white" @click="contact">🔗 联系TA</button>
  </div>
  <p v-else-if="error" class="text-sm text-danger">{{ error }}</p>
  <p v-else class="text-sm text-muted">加载中...</p>
</template>
