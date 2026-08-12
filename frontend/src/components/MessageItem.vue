<script setup lang="ts">
import type { Message } from '@/api/types'
import { useMessagesStore } from '@/stores/messages'

defineProps<{ message: Message }>()

const store = useMessagesStore()
</script>

<template>
  <div class="flex gap-3 border-b border-line bg-white p-3" @click="store.markRead(message.id)">
    <span class="text-2xl">👤</span>
    <div class="min-w-0 flex-1">
      <p class="flex items-center gap-1.5">
        <span v-if="!message.isRead" class="h-2 w-2 shrink-0 rounded-full bg-danger" />
        <span class="truncate text-sm font-semibold text-ink">{{ message.title }}</span>
      </p>
      <p class="mt-0.5 truncate text-xs text-muted">{{ message.summary }}</p>
      <p class="mt-1 text-xs text-muted">{{ message.createdAt }}</p>
    </div>
  </div>
</template>
