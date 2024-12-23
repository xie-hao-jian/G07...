import { defineStore } from 'pinia'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: null,
    token: null
  }),
  actions: {
    async login(credentials) {
      // 登录逻辑
    },
    async logout() {
      // 登出逻辑
    }
  },
  persist: true // 持久化存储
}) 