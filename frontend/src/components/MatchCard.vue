<script setup lang="ts">
import { Link, Package, Star } from 'lucide-vue-next'
import type { MatchResult } from '@/api/types'
import { formatPercent, scoreColor } from '@/utils/format'
import { relativeTime } from '@/utils/time'

const props = defineProps<{ result: MatchResult; large?: boolean }>()

function contact() {
  const item = props.result.item
  // 演示阶段弹提示；正式展示 item.contact / item.user?.phone
  window.alert(`联系TA（演示）：${item.contact || item.user?.phone || '暂无联系方式'}`)
}
</script>

<template>
  <div
    class="rounded-lg bg-white p-3"
    :class="large ? 'border-2 border-primary shadow-sm' : 'border border-line'"
  >
    <div class="flex gap-3">
      <div
        class="flex h-20 w-20 shrink-0 items-center justify-center overflow-hidden rounded bg-slate-100"
      >
        <img
          v-if="result.item.imageUrl"
          :src="result.item.imageUrl"
          class="h-full w-full object-cover"
          alt=""
        />
        <Package v-else class="size-8" aria-hidden="true" />
      </div>
      <div class="min-w-0 flex-1">
        <p class="truncate font-semibold text-ink">{{ result.item.title }}</p>
        <p class="mt-1 flex items-center gap-0.5 text-sm" :class="scoreColor(result.score)">
          <Star class="size-4 fill-current" aria-hidden="true" />
          {{ formatPercent(result.score) }}%
        </p>
        <p class="mt-1 truncate text-xs text-muted">{{ result.item.location }}</p>
        <p class="mt-0.5 text-xs text-muted">{{ relativeTime(result.item.createTime) }}</p>
      </div>
    </div>
    <button
      v-if="large"
      class="mt-3 flex w-full items-center justify-center gap-1.5 rounded bg-primary py-2.5 text-sm text-white"
      @click="contact"
    >
      <Link class="size-4" aria-hidden="true" />
      联系TA
    </button>
  </div>
</template>
