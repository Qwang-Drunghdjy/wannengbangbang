<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { createFindItem, createLostItem } from '@/api/items'
import type { PublishCategory } from '@/api/types'
import UploadArea from '@/components/UploadArea.vue'

const route = useRoute()
const router = useRouter()

const LOCATIONS = ['图书馆', '食堂', '教学楼', '操场', '宿舍', '商场', '社区', '其他']

// 规范 3.2.1：?type=seek | claim；无 type 时先选择类型
const urlType = (route.query.type as string) ?? ''
const category = computed<PublishCategory | ''>(() =>
  urlType === 'claim' || urlType === 'seek' ? (urlType as PublishCategory) : '',
)
const picked = ref<PublishCategory | ''>('')
const effectiveType = computed<PublishCategory | ''>(() => category.value || picked.value)
const isClaim = computed(() => effectiveType.value === 'claim')

const title = ref('')
const description = ref('')
const location = ref('')
const contact = ref('')
const imageUrl = ref<string | null>(null)
const error = ref('')
const submitting = ref(false)

async function onSubmit() {
  error.value = ''
  if (!effectiveType.value) return
  if (!title.value.trim()) {
    error.value = '请输入物品名称'
    return
  }
  if (!location.value) {
    error.value = '请选择地点'
    return
  }
  if (isClaim.value && !imageUrl.value) {
    error.value = '拾物必须上传图片'
    return
  }
  submitting.value = true
  try {
    const payload = {
      title: title.value.trim(),
      description: description.value || undefined,
      location: location.value,
      contact: contact.value || undefined,
      imageUrl: imageUrl.value ?? undefined,
    }
    if (isClaim.value) {
      // 拾物招领 → POST /lost-items，成功回首页
      await createLostItem(payload)
      window.alert('发布成功')
      await router.replace('/')
    } else {
      // 寻物启事 → POST /find-items，成功跳匹配结果页
      const saved = await createFindItem(payload)
      await router.replace(`/match-result?findItemId=${saved.id}`)
    }
  } catch (e) {
    error.value = e instanceof Error ? e.message : '发布失败'
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div>
    <!-- 无 type 参数：先选发布类型（规范 3.2.1） -->
    <div v-if="!effectiveType" class="space-y-3">
      <button
        type="button"
        class="flex w-full flex-col items-center gap-1 rounded-lg border-2 border-line bg-white py-5"
        @click="picked = 'seek'"
      >
        <span class="text-3xl">🔍</span>
        <span class="font-semibold">寻物启事</span>
        <span class="text-xs text-muted">我丢了东西，找它</span>
      </button>
      <button
        type="button"
        class="flex w-full flex-col items-center gap-1 rounded-lg border-2 border-line bg-white py-5"
        @click="picked = 'claim'"
      >
        <span class="text-3xl">📦</span>
        <span class="font-semibold">拾物招领</span>
        <span class="text-xs text-muted">我捡到东西，还给它</span>
      </button>
    </div>

    <form v-else class="space-y-4" @submit.prevent="onSubmit">
      <UploadArea :required="isClaim" @change="(v) => (imageUrl = v)" />

      <input
        v-model="title"
        type="text"
        placeholder="请输入物品名称"
        class="h-12 w-full rounded border border-line bg-white px-3 outline-none focus:border-primary"
      />
      <textarea
        v-model="description"
        rows="4"
        placeholder="请描述物品的颜色、品牌、特征..."
        class="w-full rounded border border-line bg-white px-3 py-2 outline-none focus:border-primary"
      />
      <select
        v-model="location"
        class="h-12 w-full rounded border border-line bg-white px-3 outline-none focus:border-primary"
      >
        <option value="" disabled>{{ isClaim ? '请选择拾到地点' : '请选择丢失地点' }}</option>
        <option v-for="loc in LOCATIONS" :key="loc" :value="loc">{{ loc }}</option>
      </select>
      <input
        v-model="contact"
        type="text"
        placeholder="手机号/微信号（选填，默认手机号）"
        class="h-12 w-full rounded border border-line bg-white px-3 outline-none focus:border-primary"
      />

      <p v-if="error" class="text-sm text-danger">{{ error }}</p>
      <button
        type="submit"
        :disabled="submitting"
        class="h-12 w-full rounded bg-primary text-white disabled:opacity-60"
      >
        {{ submitting ? '提交中...' : '发布' }}
      </button>
    </form>
  </div>
</template>
