<script setup lang="ts">
import { ref } from 'vue'

const props = withDefaults(defineProps<{ required?: boolean }>(), { required: false })

const emit = defineEmits<{ change: [value: string | null] }>()

const fileUrl = ref<string | null>(null)
const fileName = ref<string | null>(null)

/** 选择文件：生成本地预览 URL 并上报（真实上传需后端 upload 接口，脚手架阶段占位） */
function onFileChange(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return
  fileName.value = file.name
  fileUrl.value = URL.createObjectURL(file)
  emit('change', fileUrl.value)
  window.alert(`已上传：${file.name}`)
}

function removeFile() {
  fileUrl.value = null
  fileName.value = null
  emit('change', null)
}
</script>

<template>
  <div>
    <label
      class="flex h-[120px] cursor-pointer flex-col items-center justify-center gap-1 rounded-lg border-2 border-dashed border-line bg-white text-muted transition-colors"
      :class="{ 'border-primary text-primary': fileUrl }"
    >
      <template v-if="!fileUrl">
        <span class="text-2xl">📷</span>
        <span class="text-sm">点击上传图片</span>
        <span v-if="props.required" class="text-xs text-danger">拾物必填</span>
      </template>
      <template v-else>
        <img :src="fileUrl" alt="预览" class="h-20 w-20 rounded object-cover" />
        <span class="max-w-full truncate text-xs">{{ fileName }}</span>
      </template>
      <input type="file" accept="image/*" class="hidden" @change="onFileChange" />
    </label>
    <button v-if="fileUrl" type="button" class="mt-2 text-sm text-danger" @click="removeFile">
      删除图片
    </button>
  </div>
</template>
