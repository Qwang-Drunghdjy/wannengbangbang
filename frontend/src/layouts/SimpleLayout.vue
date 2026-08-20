<script setup lang="ts">
import { useRoute } from 'vue-router'
import TopBar from '@/components/TopBar.vue'
import { useTopBarRight } from '@/composables/useTopBarRight'

const route = useRoute()
const { topBarRight } = useTopBarRight()
</script>

<template>
  <div class="min-h-full">
    <TopBar :title="(route.meta.title as string) ?? ''">
      <template #right>
        <component :is="topBarRight" />
      </template>
    </TopBar>
    <main class="p-4">
      <router-view />
    </main>
  </div>
</template>
