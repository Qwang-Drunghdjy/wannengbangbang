<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { fetchFindItems, fetchLostItems } from '@/api/items'
import type { PublishItem } from '@/api/types'
import { Handshake, MapPin, Package, Search, User } from 'lucide-vue-next'
import { features } from '@/config/features'
import CabinetDialog from '@/components/CabinetDialog.vue'
import ItemListItem from '@/components/ItemListItem.vue'

const items = ref<PublishItem[]>([])
const loading = ref(false)
const error = ref('')
const cabinetOpen = ref(false)

onMounted(async () => {
  loading.value = true
  try {
    // 并行拉取拾物（claim）与寻物（seek）各 6 条，合并后按发布时间倒序取前 6
    const [lostPage, findPage] = await Promise.all([
      fetchLostItems({ page: 0, size: 6, sort: 'createTime,desc' }),
      fetchFindItems({ page: 0, size: 6, sort: 'createTime,desc' }),
    ])
    const merged: PublishItem[] = [
      ...lostPage.content.map((it) => ({ ...it, category: 'claim' as const })),
      ...findPage.content.map((it) => ({ ...it, category: 'seek' as const })),
    ]
    merged.sort(
      (a, b) => new Date(b.createTime ?? 0).getTime() - new Date(a.createTime ?? 0).getTime(),
    )
    items.value = merged.slice(0, 6)
  } catch (e) {
    error.value = e instanceof Error ? e.message : '加载失败'
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="p-4">
    <div class="flex items-center justify-between">
      <h1 class="text-xl font-bold">万能帮帮</h1>
      <span v-if="features.showSchoolBadge" class="flex items-center gap-0.5 text-sm text-muted">
        <MapPin class="size-4" aria-hidden="true" />
        南京审计大学
      </span>
    </div>
    <p class="mt-1 text-center text-sm text-muted">让失物有处寻 · 求助有回应</p>

    <div class="mt-4 grid grid-cols-2 gap-3">
      <router-link
        to="/publish?type=seek"
        class="flex flex-col items-center gap-1 rounded-lg bg-white py-4 text-sm text-ink"
      >
        <Search class="size-6" aria-hidden="true" />
        <span>发布寻物</span>
      </router-link>
      <router-link
        to="/publish?type=claim"
        class="flex flex-col items-center gap-1 rounded-lg bg-white py-4 text-sm text-ink"
      >
        <Package class="size-6" aria-hidden="true" />
        <span>发布拾物</span>
      </router-link>
      <button
        type="button"
        class="flex flex-col items-center gap-1 rounded-lg bg-white py-4 text-sm text-ink"
        @click="cabinetOpen = true"
      >
        <Handshake class="size-6" aria-hidden="true" />
        <span>帮帮柜</span>
      </button>
      <router-link
        to="/profile"
        class="flex flex-col items-center gap-1 rounded-lg bg-white py-4 text-sm text-ink"
      >
        <User class="size-6" aria-hidden="true" />
        <span>我的</span>
      </router-link>
    </div>

    <div class="mt-5 flex items-center justify-between">
      <h2 class="text-lg font-bold">最新消息</h2>
      <router-link to="/all-messages" class="text-sm text-primary">全部消息 →</router-link>
    </div>
    <p v-if="loading" class="mt-3 text-sm text-muted">加载中...</p>
    <p v-else-if="error" class="mt-3 text-sm text-danger">{{ error }}</p>
    <p v-else-if="items.length === 0" class="mt-3 text-sm text-muted">暂无消息</p>
    <ul v-else class="mt-2 divide-y divide-line rounded-lg bg-white">
      <li v-for="item in items" :key="item.id">
        <ItemListItem :item="item" />
      </li>
    </ul>
  </div>

  <CabinetDialog :open="cabinetOpen" @close="cabinetOpen = false" />
</template>
