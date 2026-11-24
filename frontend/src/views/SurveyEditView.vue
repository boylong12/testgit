<template>
  <div v-if="survey">
    <span v-if="error" style="color:#dc2626">{{ error }}</span>
    <label>标题</label>
    <input v-model="survey.title" :disabled="survey.locked" />
    <label style="margin-top:0.75rem">说明</label>
    <textarea v-model="survey.description" :disabled="survey.locked" rows="3" />
    <label style="display:flex;gap:0.5rem;align-items:center;margin-top:0.75rem">
      <input type="checkbox" v-model="survey.allowMultiple" :disabled="survey.locked" />
      允许同一访客多次提交
    </label>

    <h3>题目</h3>
    <div v-for="(q, idx) in survey.questions" :key="idx" class="card">
      <select v-model="q.type" :disabled="survey.locked">
        <option value="SINGLE">单选</option>
        <option value="MULTIPLE">多选</option>
        <option value="TEXT">单行文本</option>
        <option value="TEXTAREA">多行文本</option>
      </select>
      <input v-model="q.title" placeholder="题干" :disabled="survey.locked" />
      <label><input type="checkbox" v-model="q.required" :disabled="survey.locked" /> 必填</label>
      <div v-if="q.type === 'SINGLE' || q.type === 'MULTIPLE'">
        <div v-for="(o, oi) in q.options" :key="oi" style="display:flex;gap:0.5rem;margin-top:0.25rem">
          <input v-model="o.label" placeholder="选项" :disabled="survey.locked" />
        </div>
        <button v-if="!survey.locked" style="margin-top:0.5rem" @click="q.options.push({ label: '新选项', sortOrder: q.options.length })">添加选项</button>
      </div>
      <button v-if="!survey.locked" style="margin-top:0.5rem;color:#dc2626" @click="survey.questions.splice(idx, 1)">删除题目</button>
    </div>
    <button v-if="!survey.locked" @click="addQuestion">添加题目</button>

    <div style="display:flex;gap:0.5rem;margin-top:1.5rem;flex-wrap:wrap">
      <button class="primary" @click="save" :disabled="survey.locked">保存</button>
      <button v-if="survey.status === 'DRAFT'" class="primary" @click="publish">发布</button>
      <button v-if="survey.status === 'PUBLISHED'" @click="closeSurvey">关闭</button>
      <router-link :to="`/surveys/${survey.id}#stats`">统计</router-link>
      <a :href="`/api/admin/surveys/${survey.id}/export.csv`" target="_blank">导出 CSV</a>
    </div>

    <div v-if="stats" id="stats" class="card" style="margin-top:2rem">
      <h3>统计（回收 {{ stats.responseCount }} 份）</h3>
      <div v-for="q in stats.questions" :key="q.questionId" style="margin-top:1rem">
        <strong>{{ q.title }}</strong>
        <ul v-if="q.type !== 'TEXT' && q.type !== 'TEXTAREA'">
          <li v-for="(cnt, opt) in q.counts" :key="opt">{{ opt }}：{{ cnt }}</li>
        </ul>
        <ul v-else>
          <li v-for="(t, i) in q.texts.slice(0, 20)" :key="i">{{ t }}</li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { api, type Question, type SurveyDetail } from '../api'

const props = defineProps<{ id: string }>()
const survey = ref<SurveyDetail | null>(null)
const stats = ref<{ responseCount: number; questions: any[] } | null>(null)
const error = ref('')

function addQuestion() {
  if (!survey.value) return
  survey.value.questions.push({
    type: 'SINGLE',
    title: '新问题',
    required: true,
    sortOrder: survey.value.questions.length,
    options: [{ label: '选项A', sortOrder: 0 }, { label: '选项B', sortOrder: 1 }],
  })
}

async function load() {
  const { data } = await api.get<SurveyDetail>(`/admin/surveys/${props.id}`)
  survey.value = data
  const st = await api.get(`/admin/surveys/${props.id}/stats`)
  stats.value = st.data
}

async function save() {
  if (!survey.value) return
  error.value = ''
  try {
    const payload = {
      title: survey.value.title,
      description: survey.value.description,
      allowMultiple: survey.value.allowMultiple,
      questions: survey.value.questions.map((q, i) => ({
        ...q,
        sortOrder: i,
        options: (q.options || []).map((o, j) => ({ ...o, sortOrder: j })),
      })),
    }
    const { data } = await api.put(`/admin/surveys/${props.id}`, payload)
    survey.value = data
  } catch (e: any) {
    error.value = e?.response?.data?.message || '保存失败'
  }
}

async function publish() {
  await save()
  const { data } = await api.post(`/admin/surveys/${props.id}/publish`)
  survey.value = data
  await load()
}

async function closeSurvey() {
  const { data } = await api.post(`/admin/surveys/${props.id}/close`)
  survey.value = data
}

onMounted(load)
</script>