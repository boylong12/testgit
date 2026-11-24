<template>
  <div>
    <div style="display:flex;gap:0.5rem;margin-bottom:1rem">
      <input v-model="newTitle" placeholder="新问卷标题" style="flex:1" />
      <button class="primary" @click="create">创建问卷</button>
    </div>
    <div v-for="s in list" :key="s.id" class="card">
      <div style="display:flex;justify-content:space-between;align-items:center">
        <div>
          <strong>{{ s.title }}</strong>
          <span style="margin-left:0.5rem;color:#64748b;font-size:0.9rem">{{ s.status }}</span>
        </div>
        <router-link :to="`/surveys/${s.id}`">编辑</router-link>
      </div>
      <p v-if="s.publicToken" style="margin:0.5rem 0 0;font-size:0.9rem">
        填答链接：
        <a :href="fillUrl(s.publicToken)" target="_blank">{{ fillUrl(s.publicToken) }}</a>
      </p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { api, type SurveySummary } from '../api'

const list = ref<SurveySummary[]>([])
const newTitle = ref('')

function fillUrl(token: string) {
  const base = window.location.origin
  return `${base}/s/${token}`
}

async function load() {
  const { data } = await api.get<SurveySummary[]>('/admin/surveys')
  list.value = data
}

async function create() {
  if (!newTitle.value.trim()) return
  await api.post('/admin/surveys', { title: newTitle.value, description: '', allowMultiple: false })
  newTitle.value = ''
  await load()
}

onMounted(load)
</script>