<script setup lang="ts">
import MessageItem from '@/components/MessageItem.vue'
import { useMessagesStore } from '@/stores/messages'

const store = useMessagesStore()
</script>

<template>
  <div>
    <header class="flex items-center justify-between border-b border-line bg-white px-4 py-3">
      <span class="text-base font-semibold">我的消息</span>
      <button class="text-sm text-primary" @click="store.markAllRead">全部已读</button>
    </header>
    <div class="divide-y divide-line">
      <MessageItem v-for="m in store.messages" :key="m.id" :message="m" />
    </div>
    <p v-if="store.messages.length === 0" class="py-10 text-center text-sm text-muted">暂无消息</p>
  </div>
</template>
