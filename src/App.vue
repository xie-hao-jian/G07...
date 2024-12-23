<template>
  <div id="app">
    <router-view></router-view>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useAuthStore } from './stores/auth'
import { useCollectionsStore } from './stores/collections'

const authStore = useAuthStore()
const collectionsStore = useCollectionsStore()

onMounted(() => {
  // 初始化主题
  if (authStore.isDarkMode) {
    document.documentElement.classList.add('dark')
  }
  
  // 初始化收藏列表
  collectionsStore.initialize()
})
</script>

<style>
html, body {
  margin: 0;
  padding: 0;
  height: 100%;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, 'Open Sans', 'Helvetica Neue', sans-serif;
}

#app {
  height: 100vh;
  min-height: 100vh;
}

/* Element Plus 主题覆盖 */
:root {
  --el-color-primary: #409eff;
}

/* 暗色主题 */
.dark {
  --el-color-primary: #60a5fa;
  
  body {
    background-color: #1a1a1a;
    color: #e5e7eb;
  }
}
</style>
