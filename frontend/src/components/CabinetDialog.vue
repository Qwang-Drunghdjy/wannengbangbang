<script setup lang="ts">
defineProps<{ open: boolean }>()

const emit = defineEmits<{ close: [] }>()
</script>

<template>
  <Teleport to="body">
    <div
      v-if="open"
      class="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-6"
      @click.self="emit('close')"
    >
      <div class="w-full max-w-sm rounded-lg bg-white p-5">
        <h2 class="text-center text-lg font-semibold">📦 万能帮帮柜</h2>
        <p class="mt-3 text-sm text-muted">24小时自助存取 · 扫码即可开柜 · 全程录像追溯</p>
        <ol class="mt-3 list-decimal space-y-1 pl-5 text-sm text-ink">
          <li>小程序发布/匹配成功 → 获得取件码</li>
          <li>到帮帮柜扫码/输码 → 柜门弹开</li>
          <li>存入或取出物品 → 关闭柜门</li>
        </ol>
        <p class="mt-3 text-xs text-muted">部署位置：图书馆、食堂、宿舍楼下、商场、社区</p>
        <button
          class="mt-4 w-full rounded bg-primary py-2.5 text-sm text-white"
          @click="emit('close')"
        >
          我知道了
        </button>
      </div>
    </div>
  </Teleport>
</template>
