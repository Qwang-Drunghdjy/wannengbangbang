<script setup lang="ts">
import { Camera } from 'lucide-vue-next'
import { onMounted, ref } from 'vue'

const props = withDefaults(defineProps<{ required?: boolean; initialSrc?: string }>(), {
  required: false,
  initialSrc: '',
})

/** 上传结果：previewUrl 用于本地预览；base64 为压缩后的纯 base64（供 AI 生成描述） */
export interface UploadedImage {
  previewUrl: string
  base64: string
}

const emit = defineEmits<{ change: [value: UploadedImage | null] }>()

const fileUrl = ref<string | null>(null)
const fileName = ref<string | null>(null)
const processing = ref(false)

/** 编辑模式：mounted 时若提供 initialSrc，则展示已有图（不触发 change；fileName 保持空） */
onMounted(() => {
  if (props.initialSrc) {
    fileUrl.value = props.initialSrc
  }
})

/** 解码图片（Image + objectURL 方案，兼容性最好） */
function loadImage(src: string): Promise<HTMLImageElement> {
  return new Promise((resolve, reject) => {
    const img = new Image()
    img.onload = () => resolve(img)
    img.onerror = () => reject(new Error('图片解码失败'))
    img.src = src
  })
}

/** 压缩图片为 JPEG（宽边 ≤ 1024、质量 0.8），返回剥离 data URL 前缀的纯 base64 */
async function compressToBase64(file: File): Promise<string> {
  const objectUrl = URL.createObjectURL(file)
  try {
    const img = await loadImage(objectUrl)
    const maxSide = 1024
    const scale = Math.min(1, maxSide / Math.max(img.naturalWidth, img.naturalHeight))
    const canvas = document.createElement('canvas')
    canvas.width = Math.max(1, Math.round(img.naturalWidth * scale))
    canvas.height = Math.max(1, Math.round(img.naturalHeight * scale))
    const ctx = canvas.getContext('2d')
    if (!ctx) throw new Error('图片处理失败')
    ctx.drawImage(img, 0, 0, canvas.width, canvas.height)
    return canvas.toDataURL('image/jpeg', 0.8).split(',')[1] ?? ''
  } finally {
    URL.revokeObjectURL(objectUrl)
  }
}

/** 选择文件：压缩为 base64 并上报（previewUrl 本地预览 + base64 供 AI 生成描述） */
async function onFileChange(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file || processing.value) return
  processing.value = true
  try {
    const base64 = await compressToBase64(file)
    const previewUrl = URL.createObjectURL(file)
    fileUrl.value = previewUrl
    fileName.value = file.name
    emit('change', { previewUrl, base64 })
    window.alert(`已上传：${file.name}`)
  } catch (e) {
    window.alert(e instanceof Error ? e.message : '图片处理失败，请更换图片')
  } finally {
    processing.value = false
  }
}

function removeFile() {
  if (fileUrl.value) URL.revokeObjectURL(fileUrl.value)
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
        <Camera class="size-6" aria-hidden="true" />
        <span class="text-sm">{{ processing ? '处理中...' : '点击上传图片' }}</span>
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
