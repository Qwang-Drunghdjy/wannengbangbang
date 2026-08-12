<script setup lang="ts">
import BottomNav from '@/components/BottomNav.vue'
</script>

<template>
  <div class="flex min-h-full flex-col pb-16">
    <main class="flex-1">
      <router-view />
    </main>
    <BottomNav />
  </div>
</template>
