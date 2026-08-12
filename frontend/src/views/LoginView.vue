<script setup lang="ts">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const router = useRouter()
const route = useRoute()

const phone = ref('')
const password = ref('')
const error = ref('')
const loading = ref(false)

async function onSubmit() {
  error.value = ''
  if (!phone.value || !password.value) {
    error.value = '请输入手机号和密码'
    return
  }
  loading.value = true
  try {
    await auth.login({ phone: phone.value, password: password.value })
    const redirect = (route.query.redirect as string) || '/'
    await router.replace(redirect)
  } catch (e) {
    error.value = e instanceof Error ? e.message : '登录失败'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="flex min-h-[calc(100vh-3rem)] flex-col justify-center">
    <h1 class="text-center text-2xl font-bold">万能帮帮</h1>
    <p class="mt-1 text-center text-sm text-muted">登录后即可发布信息</p>
    <form class="mt-8 space-y-4" @submit.prevent="onSubmit">
      <input
        v-model="phone"
        type="tel"
        placeholder="手机号"
        class="h-12 w-full rounded border border-line bg-white px-3 outline-none focus:border-primary"
      />
      <input
        v-model="password"
        type="password"
        placeholder="密码"
        class="h-12 w-full rounded border border-line bg-white px-3 outline-none focus:border-primary"
      />
      <p v-if="error" class="text-sm text-danger">{{ error }}</p>
      <button
        type="submit"
        :disabled="loading"
        class="h-12 w-full rounded bg-primary text-white disabled:opacity-60"
      >
        {{ loading ? '登录中...' : '登录' }}
      </button>
    </form>
    <p class="mt-4 text-center text-sm">
      <router-link to="/register" class="text-primary">还没有账号？去注册</router-link>
    </p>
  </div>
</template>
