import { defineStore } from 'pinia'
import { authAPI } from '@/api'


export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('token') || null,
    user: JSON.parse(localStorage.getItem('user') || 'null'),
    theme: localStorage.getItem('theme') || 'light'
  }),
  
  getters: {
    isAuthenticated: (state) => !!state.token && !!state.user,
    isDarkMode: (state) => state.theme === 'dark'
  },
  
  actions: {
    async login(username, password) {
      try {
        const response = await authAPI.login({ username, password })
        
        if (response.code === 200) {
          const { token, user } = response.data
          this.token = token
          this.user = user
          
          // 保存到本地存储
          localStorage.setItem('token', token)
          localStorage.setItem('user', JSON.stringify(user))
          
          return { success: true }
        }
        return { 
          success: false, 
          message: response.message || '登录失败'
        }
      } catch (error) {
        return { 
          success: false, 
          message: error.response?.data?.message || '登录失败，请稍后重试'
        }
      }
    },
    
    async register(username, password) {
      try {
        const response = await authAPI.register({ username, password })
        
        if (response.code === 200) {
          const { token, user } = response.data
          this.token = token
          this.user = user
          
          localStorage.setItem('token', token)
          localStorage.setItem('user', JSON.stringify(user))
          
          return { success: true }
        }
        return {
          success: false,
          message: response.message || '注册失败'
        }
      } catch (error) {
        return {
          success: false,
          message: error.response?.data?.message || '注册失败，请稍后重试'
        }
      }
    },
    
    async logout() {
      try {
        // 先清除本地状态
        this.token = null
        this.user = null
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        
        // 然后调用登出接口
        await authAPI.logout()
      } catch (error) {
        console.error('登出失败:', error)
      } finally {
        // 确保导航到登录页
        window.location.href = '/login'
      }
    },
    
    async resetPassword(username, newPassword) {
      try {
        const response = await authAPI.resetPassword({ 
          username, 
          newPassword 
        })
        return response.code === 200
      } catch (error) {
        return false
      }
    },
    
    toggleTheme() {
      this.theme = this.theme === 'light' ? 'dark' : 'light'
      localStorage.setItem('theme', this.theme)
      document.documentElement.classList.toggle('dark')
    },
    
    initializeAuth() {
      const token = localStorage.getItem('token')
      const user = JSON.parse(localStorage.getItem('user') || 'null')
      if (token && user) {
        this.token = token
        this.user = user
      }
    }
  }
}) 