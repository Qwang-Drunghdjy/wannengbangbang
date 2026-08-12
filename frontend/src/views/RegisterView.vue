<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const router = useRouter()

const phone = ref('')
const password = ref('')
const nickname = ref('')
const error = ref('')
const loading = ref(false)

async function onSubmit() {
  error.value = ''
  if (!phone.value || !password.value || !nickname.value) {
    error.value = '请填写完整信息'
    return
  }
  loading.value = true
  try {
    await auth.register({ phone: phone.value, password: password.value, nickname: nickname.value })
    window.alert('注册成功，请登录')
    await router.replace('/login')
  } catch (e) {
    error.value = e instanceof Error ? e.message : '注册失败'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="flex min-h-[calc(100vh-3rem)] flex-col justify-center">
    <h1 class="text-center text-2xl font-bold">注册</h1>
    <p class="mt-1 text-center text-sm text-muted">创建账号，开始失物互助</p>
    <form class="mt-8 space-y-4" @submit.prevent="onSubmit">
      <input
        v-model="phone"
        type="tel"
        placeholder="手机号"
        class="h-12 w-full rounded border border-line bg-white px-3 outline-none focus:border-primary"
      />
      <input
        v-model="nickname"
        type="text"
        placeholder="昵称"
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
        {{ loading ? '注册中...' : '注册' }}
      </button>
    </form>
    <p class="mt-4 text-center text-sm">
      <router-link to="/login" class="text-primary">已有账号？去登录</router-link>
    </p>
  </div>
</template>
