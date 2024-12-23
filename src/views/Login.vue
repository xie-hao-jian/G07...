<template>
  <div class="login-container" :class="{ 'dark': isDarkMode }">
    <div class="background-animation">
      <div class="circle circle-1"></div>
      <div class="circle circle-2"></div>
      <div class="circle circle-3"></div>
    </div>

    <div class="login-box">
      <div class="login-header">
        <h1>心理咨询平台</h1>
        <p class="subtitle">关注心理健康，提升生活品质</p>
      </div>

      <el-tabs v-model="activeTab" class="login-tabs">
        <el-tab-pane label="登录" name="login">
          <el-form
            ref="loginForm"
            :model="loginData"
            :rules="loginRules"
            @submit.prevent="handleLogin"
          >
            <el-form-item prop="username">
              <el-input
                v-model="loginData.username"
                placeholder="用户名"
                prefix-icon="User"
                size="large"
              />
            </el-form-item>
            
            <el-form-item prop="password">
              <el-input
                v-model="loginData.password"
                type="password"
                placeholder="密码"
                prefix-icon="Lock"
                show-password
                size="large"
              />
            </el-form-item>
            
            <div class="remember-forgot">
              <el-checkbox v-model="rememberMe">记住我</el-checkbox>
              <el-button link type="primary" @click="forgotPassword">
                忘记密码？
              </el-button>
            </div>

            <el-form-item>
              <el-button
                type="primary"
                native-type="submit"
                :loading="loading"
                class="submit-button"
                size="large"
              >
                登录
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="注册" name="register">
          <el-form
            ref="formRef"
            :model="registerData"
            :rules="registerRules"
            @submit.prevent="handleRegister"
          >
            <el-form-item prop="username">
              <el-input
                v-model="registerData.username"
                placeholder="用户名"
                prefix-icon="User"
                size="large"
              />
            </el-form-item>
            
            <el-form-item prop="password">
              <el-input
                v-model="registerData.password"
                type="password"
                placeholder="密码"
                prefix-icon="Lock"
                show-password
                size="large"
              />
            </el-form-item>
            
            <el-form-item prop="confirmPassword">
              <el-input
                v-model="registerData.confirmPassword"
                type="password"
                placeholder="确认密码"
                prefix-icon="Lock"
                show-password
                size="large"
              />
            </el-form-item>

            <el-form-item>
              <el-button
                type="primary"
                native-type="submit"
                :loading="loading"
                class="submit-button"
                size="large"
              >
                注册
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 添加重置密码对话框 -->
    <el-dialog
      v-model="resetPasswordVisible"
      title="重置密码"
      width="400px"
      center
    >
      <el-form
        ref="resetFormRef"
        :model="resetData"
        :rules="resetRules"
        @submit.prevent="handleResetPassword"
      >
        <el-form-item prop="username">
          <el-input
            v-model="resetData.username"
            placeholder="用户名"
            prefix-icon="User"
            size="large"
          />
        </el-form-item>
        
        <el-form-item prop="newPassword">
          <el-input
            v-model="resetData.newPassword"
            type="password"
            placeholder="新密码"
            prefix-icon="Lock"
            show-password
            size="large"
          />
        </el-form-item>
        
        <el-form-item prop="confirmNewPassword">
          <el-input
            v-model="resetData.confirmNewPassword"
            type="password"
            placeholder="确认新密码"
            prefix-icon="Lock"
            show-password
            size="large"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="resetPasswordVisible = false">取消</el-button>
          <el-button type="primary" @click="handleResetPassword" :loading="loading">
            确认重置
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { ElMessage } from 'element-plus'

const router = useRouter()
const authStore = useAuthStore()
const isDarkMode = computed(() => authStore.isDarkMode)

const activeTab = ref('login')
const loading = ref(false)
const rememberMe = ref(false)
const resetFormRef = ref(null)
const formRef = ref(null)

// 登录相关
const loginData = ref({
  username: '',
  password: ''
})

const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, message: '用户名至少3个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6个字符', trigger: 'blur' }
  ]
}

// 注册相关
const registerData = ref({
  username: '',
  password: '',
  confirmPassword: ''
})

const validatePass = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== registerData.value.password) {
    callback(new Error('两次输入密码不一致!'))
  } else {
    callback()
  }
}

const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, message: '用户名长度不能小于3个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { 
      validator: (rule, value, callback) => {
        if (!value) {
          callback(new Error('请输入密码'))
        } else if (value.length < 6) {
          callback(new Error('密码长度不能小于6个字符'))
        } else {
          callback()
        }
      },
      trigger: ['blur', 'change']
    }
  ],
  confirmPassword: [
    { required: true, validator: validatePass, trigger: 'blur' }
  ]
}

// 重置密码相关
const resetPasswordVisible = ref(false)
const resetData = ref({
  username: '',
  newPassword: '',
  confirmNewPassword: ''
})

const validateNewPass = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入新密码'))
  } else if (value !== resetData.value.newPassword) {
    callback(new Error('两次输入密码不一致!'))
  } else {
    callback()
  }
}

const resetRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, message: '用户名至少3个字符', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码至少6个字符', trigger: 'blur' }
  ],
  confirmNewPassword: [
    { required: true, validator: validateNewPass, trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  loading.value = true
  try {
    const result = await authStore.login(
      loginData.value.username,
      loginData.value.password
    )
    
    if (result.success) {
      ElMessage.success('登录成功')
      router.push('/')
    } else {
      ElMessage.error(result.message)
    }
  } catch (error) {
    ElMessage.error('登录失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const handleRegister = async () => {
  loading.value = true
  try {
    await formRef.value.validate()
    
    const result = await authStore.register(
      registerData.value.username,
      registerData.value.password
    )
    
    if (result.success) {
      ElMessage.success('注册成功')
      activeTab.value = 'login'
      registerData.value = {
        username: '',
        password: '',
        confirmPassword: ''
      }
    } else {
      ElMessage.error(result.message)
    }
  } catch (error) {
    if (error.message) {
      ElMessage.error(error.message)
    } else {
      ElMessage.error('注册失败，请稍后重试')
    }
  } finally {
    loading.value = false
  }
}

// 修改忘记密码处理函数
const forgotPassword = () => {
  resetPasswordVisible.value = true
}

// 处理重置密码
const handleResetPassword = async () => {
  loading.value = true
  try {
    await resetFormRef.value.validate()
    
    const success = await authStore.resetPassword(
      resetData.value.username,
      resetData.value.newPassword
    )
    
    if (success) {
      ElMessage.success('密码重置成功')
      resetPasswordVisible.value = false
      resetData.value = {
        username: '',
        newPassword: '',
        confirmNewPassword: ''
      }
    }
  } catch (error) {
    console.error('密码重置失败:', error)
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.login-container {
  @apply min-h-screen flex items-center justify-center relative overflow-hidden;
  background: linear-gradient(45deg, #4158D0 0%, #C850C0 46%, #FFCC70 100%);
  
  &.dark {
    background: linear-gradient(45deg, #0f172a 0%, #312e81 46%, #581c87 100%);
  }
}

.background-animation {
  @apply absolute inset-0 overflow-hidden;
  
  .circle {
    @apply absolute rounded-full;
    background: radial-gradient(circle, rgba(255,255,255,0.3) 0%, rgba(255,255,255,0) 70%);
    filter: blur(20px);
    animation: float 15s ease-in-out infinite;
    
    &.circle-1 {
      width: 500px;
      height: 500px;
      left: -10%;
      top: -10%;
      animation-delay: 0s;
    }
    
    &.circle-2 {
      width: 600px;
      height: 600px;
      right: -20%;
      top: 30%;
      animation-delay: -5s;
    }
    
    &.circle-3 {
      width: 400px;
      height: 400px;
      left: 30%;
      bottom: -10%;
      animation-delay: -10s;
    }
  }
}

@keyframes float {
  0%, 100% {
    transform: translate(0, 0) scale(1);
  }
  25% {
    transform: translate(5%, 5%) scale(1.1);
  }
  50% {
    transform: translate(0, 10%) scale(1);
  }
  75% {
    transform: translate(-5%, 5%) scale(0.9);
  }
}

.login-box {
  @apply bg-white/80 backdrop-blur-xl rounded-3xl shadow-2xl w-[480px] p-10 relative z-10;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  
  .dark & {
    @apply bg-gray-900/80 text-gray-200;
    border-color: rgba(255, 255, 255, 0.1);
  }
}

.login-header {
  @apply text-center mb-8;
  
  h1 {
    @apply text-3xl font-bold mb-3 bg-clip-text text-transparent;
    background-image: linear-gradient(to right, #4158D0, #C850C0);
    
    .dark & {
      background-image: linear-gradient(to right, #60a5fa, #c084fc);
    }
  }
  
  .subtitle {
    @apply text-gray-600 text-lg;
    
    .dark & {
      @apply text-gray-400;
    }
  }
}

.login-tabs {
  @apply mb-8;
  
  :deep(.el-tabs__nav) {
    @apply w-full;
    
    .el-tabs__item {
      @apply flex-1 text-center text-lg font-medium;
      
      &.is-active {
        @apply text-blue-600;
        
        .dark & {
          @apply text-blue-400;
        }
      }
    }
  }
}

.el-form-item {
  :deep(.el-input__wrapper) {
    @apply rounded-lg shadow-sm border-0;
    background: rgba(255, 255, 255, 0.9);
    
    .dark & {
      background: rgba(30, 41, 59, 0.9);
    }
  }
}

.remember-forgot {
  @apply flex items-center justify-between mb-6;
  
  .el-checkbox {
    @apply text-gray-600;
    
    .dark & {
      @apply text-gray-400;
    }
  }
}

.submit-button {
  @apply w-full h-12 text-lg rounded-lg font-medium transition-all;
  background: linear-gradient(to right, #4158D0, #C850C0);
  
  &:hover {
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  }
  
  &:active {
    transform: translateY(0);
  }
  
  .dark & {
    background: linear-gradient(to right, #60a5fa, #c084fc);
  }
}

// 添加对话框样式
:deep(.el-dialog) {
  @apply rounded-2xl overflow-hidden;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  
  .dark & {
    background: rgba(30, 41, 59, 0.9);
    border-color: rgba(255, 255, 255, 0.1);
  }
  
  .el-dialog__header {
    @apply text-center mb-4;
    
    .el-dialog__title {
      @apply text-xl font-medium;
      
      .dark & {
        @apply text-gray-200;
      }
    }
  }
  
  .dialog-footer {
    @apply flex justify-end gap-4;
  }
}
</style> 