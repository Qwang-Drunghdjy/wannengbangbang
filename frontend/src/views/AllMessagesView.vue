<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { fetchFindItems, fetchLostItems, type PageQuery } from '@/api/items'
import type { PageResult, PublishCategory, PublishItem } from '@/api/types'
import ItemListItem from '@/components/ItemListItem.vue'
import { useAuthStore } from '@/stores/auth'

const PAGE_SIZE = 10

type Tab = 'seek' | 'claim'

const authStore = useAuthStore()
const route = useRoute()
const router = useRouter()

const tab = ref<Tab>('seek')
/** 默认关闭；带 ?mine=1（如「我的发布」入口）时自动开启 */
const onlyMine = ref(route.query.mine === '1')
const items = ref<PublishItem[]>([])
const page = ref(0)
const hasMore = ref(false)
const loading = ref(false)
const loadingMore = ref(false)
const error = ref('')

const isSeek = computed(() => tab.value === 'seek')
const mineDisabled = computed(() => !authStore.isLoggedIn)
const emptyText = computed(() => {
  const typeText = isSeek.value ? '寻物' : '拾物'
  return onlyMine.value ? `您还没有发布${typeText}消息` : `暂无${typeText}消息`
})

/** 拉取当前 tab 的一页数据（append 为 true 时追加到已有列表） */
async function fetchList(p: number, append: boolean) {
  const params: PageQuery = {
    page: p,
    size: PAGE_SIZE,
    sort: 'createTime,desc',
    mine: onlyMine.value || undefined,
  }
  const res: PageResult<PublishItem> = isSeek.value
    ? await fetchFindItems(params)
    : await fetchLostItems(params)
  const tagged: PublishItem[] = res.content.map((it) => ({
    ...it,
    category: (isSeek.value ? 'seek' : 'claim') as PublishCategory,
  }))
  if (append) {
    const existing = new Set(items.value.map((i) => i.id))
    items.value = [...items.value, ...tagged.filter((i) => !existing.has(i.id))]
  } else {
    items.value = tagged
  }
  hasMore.value = items.value.length < res.page.totalElements
}

/** 重置并重新加载第一页（切 tab / 切换开关时调用） */
async function reload() {
  loading.value = true
  error.value = ''
  page.value = 0
  try {
    await fetchList(0, false)
  } catch (e) {
    error.value = e instanceof Error ? e.message : '加载失败'
  } finally {
    loading.value = false
  }
}

/** 加载下一页并追加 */
async function loadMore() {
  if (loadingMore.value || !hasMore.value) return
  loadingMore.value = true
  try {
    const next = page.value + 1
    await fetchList(next, true)
    page.value = next
  } catch (e) {
    error.value = e instanceof Error ? e.message : '加载失败'
  } finally {
    loadingMore.value = false
  }
}

function switchTab(t: Tab) {
  if (tab.value === t) return
  tab.value = t
  reload()
}

function toggleMine() {
  if (mineDisabled.value) {
    window.alert('请先登录')
    return
  }
  onlyMine.value = !onlyMine.value
  // 同步 URL query（mine=1 时开启），保持刷新 / 返回后状态一致
  router.replace({
    query: onlyMine.value ? { ...route.query, mine: '1' } : { ...route.query, mine: undefined },
  })
  reload()
}

onMounted(reload)
</script>

<template>
  <div>
    <!-- 标签页 + 仅查看我的开关 -->
    <div class="flex items-center justify-between border-b border-line">
      <div class="flex">
        <button
          type="button"
          class="border-b-2 px-4 py-2.5 text-sm"
          :class="
            isSeek ? 'border-primary font-semibold text-primary' : 'border-transparent text-muted'
          "
          @click="switchTab('seek')"
        >
          寻物
        </button>
        <button
          type="button"
          class="border-b-2 px-4 py-2.5 text-sm"
          :class="
            !isSeek ? 'border-primary font-semibold text-primary' : 'border-transparent text-muted'
          "
          @click="switchTab('claim')"
        >
          拾物
        </button>
      </div>
      <button
        type="button"
        class="flex items-center gap-1.5 py-2.5 pr-1 text-sm"
        :class="mineDisabled ? 'opacity-50' : ''"
        @click="toggleMine"
      >
        <span
          class="inline-block h-4 w-7 rounded-full transition-colors"
          :class="onlyMine ? 'bg-primary' : 'bg-slate-300'"
        >
          <span
            class="block h-3.5 w-3.5 translate-x-0.5 rounded-full bg-white transition-transform"
            :class="onlyMine ? 'translate-x-3' : 'translate-x-0.5'"
          />
        </span>
        <span>仅查看我的</span>
      </button>
    </div>

    <!-- 列表 -->
    <p v-if="loading" class="mt-3 text-sm text-muted">加载中...</p>
    <p v-else-if="error" class="mt-3 text-sm text-danger">{{ error }}</p>
    <p v-else-if="items.length === 0" class="mt-3 text-sm text-muted">{{ emptyText }}</p>
    <ul v-else class="mt-2 divide-y divide-line rounded-lg bg-white">
      <li v-for="item in items" :key="item.id">
        <ItemListItem :item="item" />
      </li>
    </ul>

    <!-- 加载更多 -->
    <button
      v-if="hasMore && !loading"
      type="button"
      class="mt-4 w-full rounded border border-line py-2.5 text-sm text-muted"
      :disabled="loadingMore"
      @click="loadMore"
    >
      {{ loadingMore ? '加载中...' : '加载更多' }}
    </button>
  </div>
</template>
