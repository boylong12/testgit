<template>
  <div class="page">
    <header style="display:flex;justify-content:space-between;align-items:center;margin-bottom:1rem">
      <h1 style="margin:0;font-size:1.4rem">调研问卷系统</h1>
      <router-link v-if="showAdmin" to="/">管理首页</router-link>
    </header>
    <router-view />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const showAdmin = computed(() => !route.path.startsWith('/s/') && route.path !== '/login')
</script>