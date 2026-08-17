<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { fetchMatches } from '@/api/items'
import type { MatchResult } from '@/api/types'
import MatchCard from '@/components/MatchCard.vue'

const route = useRoute()
const findItemId = route.query.findItemId as string | undefined

const results = ref<MatchResult[]>([])
const loading = ref(false)
const error = ref('')

onMounted(async () => {
  if (!findItemId) {
    error.value = '缺少匹配参数'
    return
  }
  loading.value = true
  try {
    results.value = await fetchMatches(findItemId, 3)
  } catch (e) {
    error.value = e instanceof Error ? e.message : '匹配失败'
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div>
    <h1 class="text-center text-2xl font-bold">🎉 匹配成功！</h1>
    <p class="mt-1 text-center text-sm text-muted">已为您找到以下匹配物品</p>

    <p v-if="loading" class="mt-6 text-center text-sm text-muted">匹配中...</p>
    <p v-else-if="error" class="mt-6 text-center text-sm text-danger">{{ error }}</p>
    <p v-else-if="results.length === 0" class="mt-6 text-center text-sm text-muted">
      未找到匹配物品
    </p>

    <div v-else class="mt-4 space-y-3">
      <MatchCard :result="results[0]" large />
      <MatchCard v-for="r in results.slice(1)" :key="r.item.id" :result="r" />
    </div>

    <router-link
      to="/"
      class="mt-6 block rounded border border-line bg-white py-3 text-center text-sm text-muted"
    >
      返回首页
    </router-link>
  </div>
</template>
