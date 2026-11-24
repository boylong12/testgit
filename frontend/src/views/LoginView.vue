<template>
  <div class="card" style="max-width:400px;margin:4rem auto">
    <h2>管理员登录</h2>
    <label>用户名</label>
    <input v-model="username" autocomplete="username" />
    <label style="margin-top:0.75rem">密码</label>
    <input v-model="password" type="password" autocomplete="current-password" />
    <p v-if="error" style="color:#dc2626">{{ error }}</p>
    <button class="primary" style="margin-top:1rem;width:100%" @click="login">登录</button>
    <p style="font-size:0.85rem;color:#64748b;margin-top:1rem">默认账号 admin / admin123</p>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { api, setToken } from '../api'

const router = useRouter()
const username = ref('admin')
const password = ref('')
const error = ref('')

async function login() {
  error.value = ''
  try {
    const { data } = await api.post('/auth/login', { username: username.value, password: password.value })
    setToken(data.token)
    router.push('/')
  } catch {
    error.value = '登录失败，请检查用户名和密码'
  }
}
</script>