<script setup lang="ts">
import { computed, type Component } from 'vue'
import { Copy, Edit, Trash2 } from 'lucide-vue-next'

/** 动作菜单面板是否打开（由父组件控制）；isOwner 是否发布者本人（非本人仅显示「复制链接」） */
const props = defineProps<{ open: boolean; isOwner: boolean }>()

const emit = defineEmits<{ close: [] }>()

interface Action {
  label: string
  icon: Component
  danger?: boolean
  ariaLabel: string
}

/** 动作按钮列表：仅本人可见「编辑 / 删除」；非本人仅「复制链接」 */
const actions = computed<Action[]>(() => {
  const base: Action[] = []
  if (props.isOwner) {
    base.push(
      { label: '编辑', icon: Edit, ariaLabel: '编辑' },
      { label: '删除', icon: Trash2, ariaLabel: '删除', danger: true },
    )
  }
  base.push({ label: '复制链接', icon: Copy, ariaLabel: '复制链接' })
  return base
})
</script>

<template>
  <Teleport to="body">
    <div
      v-if="open"
      class="fixed inset-0 z-50 flex items-end bg-black/40"
      @click.self="emit('close')"
    >
      <div class="w-full rounded-t-2xl bg-white pb-[calc(env(safe-area-inset-bottom,0px)+1rem)]">
        <!-- 动作按钮行：图标在上、文字在下；按 isOwner 过滤 -->
        <div class="flex items-center justify-around border-b border-line px-4 py-5">
          <button
            v-for="a in actions"
            :key="a.label"
            type="button"
            class="flex flex-col items-center gap-2 px-4 py-1"
            :aria-label="a.ariaLabel"
          >
            <span class="flex size-12 items-center justify-center rounded-full bg-bg">
              <component
                :is="a.icon"
                class="size-6"
                :class="a.danger ? 'text-danger' : 'text-ink'"
                aria-hidden="true"
              />
            </span>
            <span class="text-sm" :class="a.danger ? 'text-danger' : 'text-ink'">{{
              a.label
            }}</span>
          </button>
        </div>
        <!-- 取消按钮 -->
        <button
          type="button"
          class="mx-2 mt-3 w-[calc(100%-1rem)] rounded bg-bg py-3 text-sm text-ink"
          @click="emit('close')"
        >
          取消
        </button>
      </div>
    </div>
  </Teleport>
</template>
