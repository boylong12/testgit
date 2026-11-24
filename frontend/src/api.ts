import axios from 'axios'

const TOKEN_KEY = 'survey_admin_token'

export const api = axios.create({ baseURL: '/api' })

api.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_KEY)
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

export function setToken(token: string | null) {
  if (token) localStorage.setItem(TOKEN_KEY, token)
  else localStorage.removeItem(TOKEN_KEY)
}

export function getVisitorKey(): string {
  const key = 'survey_visitor_key'
  let v = localStorage.getItem(key)
  if (!v) {
    v = crypto.randomUUID()
    localStorage.setItem(key, v)
  }
  return v
}

export type QuestionType = 'SINGLE' | 'MULTIPLE' | 'TEXT' | 'TEXTAREA'

export interface SurveySummary {
  id: number
  title: string
  status: string
  publicToken: string | null
  allowMultiple: boolean
}

export interface Question {
  id?: number
  type: QuestionType
  title: string
  required: boolean
  sortOrder: number
  options: { id?: number; label: string; sortOrder: number }[]
}

export interface SurveyDetail {
  id: number
  title: string
  description: string
  status: string
  publicToken: string | null
  allowMultiple: boolean
  locked: boolean
  questions: Question[]
}