<script setup lang="ts">
import { useRoute } from 'vue-router'
import TopBar from '@/components/TopBar.vue'

const route = useRoute()
</script>

<template>
  <div class="min-h-full">
    <TopBar :title="(route.meta.title as string) ?? ''" />
    <main class="p-4">
      <router-view />
    </main>
  </div>
</template>
