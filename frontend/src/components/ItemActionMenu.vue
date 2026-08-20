<script setup lang="ts">
import { computed, onUnmounted, ref, type Component } from 'vue'
import { Copy, Edit, Trash2 } from 'lucide-vue-next'

/** 动作菜单面板是否打开（由父组件控制）；isOwner 是否发布者本人（非本人仅显示「复制链接」） */
const props = defineProps<{ open: boolean; isOwner: boolean }>()

const emit = defineEmits<{ close: []; edit: []; delete: []; copy: [] }>()

interface Action {
  label: string
  icon: Component
  danger?: boolean
  ariaLabel: string
  type: 'edit' | 'delete' | 'copy'
}

/** 动作按钮列表：仅本人可见「编辑 / 删除」；非本人仅「复制链接」 */
const actions = computed<Action[]>(() => {
  const base: Action[] = []
  if (props.isOwner) {
    base.push(
      { label: '编辑', icon: Edit, ariaLabel: '编辑', type: 'edit' },
      { label: '删除', icon: Trash2, ariaLabel: '删除', danger: true, type: 'delete' },
    )
  }
  base.push({ label: '复制链接', icon: Copy, ariaLabel: '复制链接', type: 'copy' })
  return base
})

/** 复制成功反馈：按钮临时变为「已复制」约 2s */
const copied = ref(false)
let copiedTimer: ReturnType<typeof setTimeout> | undefined

function onAction(a: Action) {
  if (a.type === 'copy') {
    emit('copy')
    copied.value = true
    if (copiedTimer) clearTimeout(copiedTimer)
    copiedTimer = setTimeout(() => (copied.value = false), 2000)
    return
  }
  if (a.type === 'edit') {
    emit('edit')
  } else {
    emit('delete')
  }
}

onUnmounted(() => {
  if (copiedTimer) clearTimeout(copiedTimer)
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
            @click="onAction(a)"
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
              a.type === 'copy' && copied ? '已复制' : a.label
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
