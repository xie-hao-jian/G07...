import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('../views/Login.vue'),
      meta: { requiresAuth: false }
    },
    {
      path: '/',
      component: () => import('../layouts/MainLayout.vue'),
      meta: { requiresAuth: true },
      children: [
        {
          path: '',
          name: 'Dashboard',
          component: () => import('../views/Dashboard.vue'),
          props: true
        },
        {
          path: 'psychological-test',
          name: 'PsychologicalTest',
          component: () => import('../views/PsychologicalTest.vue')
        },
        {
          path: 'ai-assistant',
          name: 'AIAssistant',
          component: () => import('../views/AIAssistant.vue')
        },
        {
          path: 'test-records',
          name: 'TestRecords',
          component: () => import('../views/TestRecords.vue')
        },
        {
          path: 'profile',
          name: 'Profile',
          component: () => import('../views/Profile.vue')
        }
      ]
    }
  ]
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()
  const token = localStorage.getItem('token')
  const user = JSON.parse(localStorage.getItem('user') || 'null')
  const isAuthenticated = !!token && !!user
  
  // 更新 store 中的认证状态
  if (isAuthenticated && !authStore.user) {
    authStore.$patch({
      token,
      user
    })
  }
  
  // 处理需要认证的路由
  if (to.meta.requiresAuth && !isAuthenticated) {
    next({ name: 'Login' })
    return
  }
  
  // 处理登录页
  if (to.name === 'Login' && isAuthenticated) {
    next({ name: 'Dashboard' })
    return
  }
  
  // 其他情况正常放行
  next()
})

export default router 