<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { fetchFindItem, fetchLostItem, fetchMatches, fetchMatchesByLostItem } from '@/api/items'
import type { MatchResult, PublishItem } from '@/api/types'
import { FolderOpen, Link, Package, Search } from 'lucide-vue-next'
import MatchCard from '@/components/MatchCard.vue'
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

// ── 智能匹配 ─────────────────────────────────────────────

const matchesOpen = ref(false)
/** null = 尚未成功加载（失败可重试），[] = 已加载但无匹配 */
const matches = ref<MatchResult[] | null>(null)
const matchesLoading = ref(false)
const matchesError = ref('')

/** 匹配结果的方向：当前详情为拾物(claim)时匹配到的是寻物(seek)，反之亦然 */
const matchCategory = computed<'seek' | 'claim'>(() => (isClaim.value ? 'seek' : 'claim'))

async function loadMatches() {
  if (!item.value) return
  matchesLoading.value = true
  matchesError.value = ''
  try {
    const id = route.params.id as string
    // claim(拾物)详情 → 匹配寻物：GET /lost-items/{id}/matches；seek(寻物)详情 → 匹配拾物：GET /find-items/{id}/matches
    matches.value = isClaim.value ? await fetchMatchesByLostItem(id, 3) : await fetchMatches(id, 3)
  } catch (e) {
    matchesError.value = e instanceof Error ? e.message : '匹配失败'
    matches.value = null
  } finally {
    matchesLoading.value = false
  }
}

/** 点击按钮展开/收起匹配区；首次展开时懒加载（成功后缓存，失败可重试） */
function toggleMatches() {
  if (matchesOpen.value) {
    matchesOpen.value = false
    return
  }
  matchesOpen.value = true
  if (matches.value === null && !matchesLoading.value) {
    loadMatches()
  }
}
</script>

<template>
  <div v-if="item" class="space-y-4">
    <div class="flex h-56 items-center justify-center overflow-hidden rounded bg-slate-100">
      <img v-if="item.imageUrl" :src="item.imageUrl" class="h-full w-full object-cover" alt="" />
      <Package v-else class="size-16" aria-hidden="true" />
    </div>
    <h1 class="text-xl font-bold">{{ item.title }}</h1>
    <p class="text-sm text-muted">{{ isClaim ? '拾到地点' : '丢失地点' }}：{{ item.location }}</p>
    <p v-if="item.description" class="text-sm text-ink">{{ item.description }}</p>
    <p class="text-xs text-muted">发布时间：{{ relativeTime(item.createTime) }}</p>
    <p class="text-xs text-muted">发布者：{{ item.user?.nickname ?? '匿名' }}</p>
    <button
      class="flex w-full items-center justify-center gap-1.5 rounded bg-primary py-3 text-white"
      @click="contact"
    >
      <Link class="size-4" aria-hidden="true" />
      联系TA
    </button>
    <button
      type="button"
      class="flex w-full items-center justify-center gap-1.5 rounded border border-line bg-white py-3 text-ink"
      @click="toggleMatches"
    >
      <FolderOpen v-if="matchesOpen" class="size-4" aria-hidden="true" />
      <Search v-else class="size-4" aria-hidden="true" />
      {{ matchesOpen ? '收起匹配' : '智能匹配' }}
    </button>

    <div v-if="matchesOpen" class="space-y-3">
      <p v-if="matchesLoading" class="text-center text-sm text-muted">匹配中...</p>
      <p v-else-if="matchesError" class="text-center text-sm text-danger">{{ matchesError }}</p>
      <p v-else-if="matches && matches.length === 0" class="text-center text-sm text-muted">
        未找到匹配消息
      </p>
      <template v-else-if="matches">
        <router-link
          v-for="r in matches"
          :key="r.item.id"
          :to="`/item/${r.item.id}?type=${matchCategory}`"
          class="block"
        >
          <MatchCard :result="r" />
        </router-link>
      </template>
    </div>
  </div>
  <p v-else-if="error" class="text-sm text-danger">{{ error }}</p>
  <p v-else class="text-sm text-muted">加载中...</p>
</template>
