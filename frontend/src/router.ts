import { createRouter, createWebHistory } from 'vue-router'
import LoginView from './views/LoginView.vue'
import SurveyListView from './views/SurveyListView.vue'
import SurveyEditView from './views/SurveyEditView.vue'
import FillView from './views/FillView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', component: LoginView },
    { path: '/', component: SurveyListView },
    { path: '/surveys/:id', component: SurveyEditView, props: true },
    { path: '/s/:token', component: FillView, props: true },
  ],
})

router.beforeEach((to) => {
  if (to.path.startsWith('/s/')) return true
  if (to.path === '/login') return true
  const token = localStorage.getItem('survey_admin_token')
  if (!token) return '/login'
  return true
})

export default router