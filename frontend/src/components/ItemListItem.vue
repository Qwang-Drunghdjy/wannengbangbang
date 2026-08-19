<script setup lang="ts">
import { Backpack, Search } from 'lucide-vue-next'
import type { PublishItem } from '@/api/types'
import { relativeTime } from '@/utils/time'

defineProps<{ item: PublishItem }>()
</script>

<template>
  <router-link
    :to="`/item/${item.id}?type=${item.category ?? 'claim'}`"
    class="flex items-center gap-3 px-3 py-3"
  >
    <Search v-if="item.category === 'seek'" class="size-6" aria-hidden="true" />
    <Backpack v-else class="size-6" aria-hidden="true" />
    <span class="flex-1 truncate text-sm font-medium text-ink">{{ item.title }}</span>
    <span
      class="shrink-0 rounded px-1.5 py-0.5 text-xs"
      :class="
        item.category === 'seek' ? 'bg-orange-100 text-orange-600' : 'bg-green-100 text-green-600'
      "
      >{{ item.category === 'seek' ? '寻物' : '拾物' }}</span
    >
    <span class="text-xs text-muted">{{ item.location }}</span>
    <span class="text-xs text-muted">{{ relativeTime(item.createTime) }}</span>
  </router-link>
</template>
