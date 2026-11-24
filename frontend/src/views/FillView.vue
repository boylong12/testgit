<template>
  <div v-if="survey" class="card">
    <h2>{{ survey.title }}</h2>
    <p>{{ survey.description }}</p>
    <form @submit.prevent="submit">
      <div v-for="q in survey.questions" :key="q.id" style="margin-bottom:1.25rem">
        <label>{{ q.title }}<span v-if="q.required" style="color:#dc2626"> *</span></label>

        <div v-if="q.type === 'SINGLE'">
          <label v-for="o in q.options" :key="o.id" style="display:block;font-weight:400">
            <input type="radio" :name="'q' + q.id" :value="o.label" v-model="answers[String(q.id)]" />
            {{ o.label }}
          </label>
        </div>

        <div v-else-if="q.type === 'MULTIPLE'">
          <label v-for="o in q.options" :key="o.id" style="display:block;font-weight:400">
            <input type="checkbox" :value="o.label" @change="toggleMulti(q.id!, o.label, ($event.target as HTMLInputElement).checked)" :checked="multiChecked(q.id!, o.label)" />
            {{ o.label }}
          </label>
        </div>

        <input v-else-if="q.type === 'TEXT'" v-model="answers[String(q.id)]" />
        <textarea v-else v-model="answers[String(q.id)]" rows="3" />
      </div>
      <p v-if="error" style="color:#dc2626">{{ error }}</p>
      <p v-if="done" style="color:#16a34a">提交成功，感谢参与！</p>
      <button v-if="!done" class="primary" type="submit">提交</button>
    </form>
  </div>
  <p v-else-if="error">{{ error }}</p>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import axios from 'axios'
import { getVisitorKey } from '../api'

const props = defineProps<{ token: string }>()
const survey = ref<{ title: string; description: string; questions: any[] } | null>(null)
const answers = reactive<Record<string, any>>({})
const multi = reactive<Record<string, string[]>>({})
const error = ref('')
const done = ref(false)

function multiChecked(qid: number, label: string) {
  return (multi[String(qid)] || []).includes(label)
}

function toggleMulti(qid: number, label: string, checked: boolean) {
  const key = String(qid)
  if (!multi[key]) multi[key] = []
  if (checked) multi[key].push(label)
  else multi[key] = multi[key].filter((x) => x !== label)
  answers[key] = [...multi[key]]
}

async function load() {
  try {
    const { data } = await axios.get(`/api/public/surveys/${props.token}`)
    survey.value = data
  } catch {
    error.value = '问卷不存在或未开放'
  }
}

async function submit() {
  error.value = ''
  try {
    await axios.post(`/api/public/surveys/${props.token}/responses`, {
      answers,
      visitorKey: getVisitorKey(),
    })
    done.value = true
  } catch (e: any) {
    error.value = e?.response?.data?.message || '提交失败'
  }
}

onMounted(load)
</script>