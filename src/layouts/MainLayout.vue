<template>
  <el-container class="layout-container">
    <el-aside width="200px" class="aside">
      <div class="logo">
        <h1>心理咨询平台</h1>
      </div>
      <el-menu
        router
        :default-active="route.path"
      >
        <el-menu-item index="/">
          <el-icon><HomeFilled /></el-icon>
          <span>每日推荐</span>
        </el-menu-item>
        
        <el-menu-item index="/psychological-test">
          <el-icon><DocumentChecked /></el-icon>
          <span>心理测试</span>
        </el-menu-item>
        
        <el-menu-item index="/ai-assistant">
          <el-icon><ChatDotRound /></el-icon>
          <span>AI助手</span>
        </el-menu-item>
        
        <el-menu-item index="/test-records">
          <el-icon><List /></el-icon>
          <span>测试记录</span>
        </el-menu-item>
        
        <el-menu-item index="/profile">
          <el-icon><User /></el-icon>
          <span>个人中心</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    
    <el-container>
      <el-header class="header">
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-profile">
              <el-avatar 
                :size="32" 
                :style="{ backgroundColor: '#1677FF' }"
              >
                {{ authStore.user?.username?.charAt(0)?.toUpperCase() }}
              </el-avatar>
              <span class="username">{{ authStore.user?.username }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      
      <el-main>
        <router-view v-slot="{ Component }">
          <component :is="Component" />
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import {
  HomeFilled,
  DocumentChecked,
  ChatDotRound,
  List,
  ArrowDown,
  User
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const handleCommand = (command) => {
  if (command === 'logout') {
    authStore.logout()
  }
}
</script>

<style scoped>
.layout-container {
  min-height: 100vh;
}

.aside {
  background-color: #fff;
  border-right: 1px solid #e4e7ed;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid #e4e7ed;
}

.logo h1 {
  font-size: 1.125rem;
  font-weight: 600;
  background: linear-gradient(to right, #4158D0, #C850C0);
  -webkit-background-clip: text;
  color: transparent;
}

:deep(.el-menu) {
  border: none;
}

:deep(.el-menu-item) {
  display: flex;
  align-items: center;
  gap: 8px;
}

:deep(.el-menu-item .el-icon) {
  font-size: 1.125rem;
}

.header {
  background-color: #fff;
  border-bottom: 1px solid #e4e7ed;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding: 0 24px;
}

.user-profile {
  @apply flex items-center gap-2 cursor-pointer;
}

.username {
  @apply text-sm;
}

.el-main {
  background-color: #f5f7fa;
  padding: 0;
}

/* 暗色主题 */
:deep(.dark) {
  .aside {
    background-color: #1a1a1a;
    border-color: #4c4c4c;
  }
  
  .logo {
    border-color: #4c4c4c;
  }
  
  .logo h1 {
    background: linear-gradient(to right, #60a5fa, #c084fc);
    -webkit-background-clip: text;
  }
  
  .header {
    background-color: #1a1a1a;
    border-color: #4c4c4c;
  }
  
  .user-profile {
    color: #a3a6ad;
  }
  
  .user-profile:hover {
    color: #e5e7eb;
  }
  
  .el-main {
    background-color: #141414;
  }
}
</style> 