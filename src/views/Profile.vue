<template>
  <div class="profile-page">
    <el-card class="profile-card">
      <!-- 个人信息部分 -->
      <div class="profile-section">
        <div class="info-header">
          <div class="user-info">
            <el-avatar
              :size="80"
              :style="{ backgroundColor: '#1677FF' }"
              class="avatar"
            >
              {{ userForm.username?.charAt(0)?.toUpperCase() }}
            </el-avatar>
            <div class="user-details">
              <h2>{{ userForm.username }}</h2>
              <p class="signature">{{ userForm.signature || '这个人很懒，什么都没写~' }}</p>
            </div>
          </div>
          <div class="action-buttons">
            <el-button type="primary" link @click="handleEdit">
              {{ isEditing ? '保存' : '编辑资料' }}
            </el-button>
            <el-button type="primary" link @click="showPasswordDialog = true">
              修改密码
            </el-button>
          </div>
        </div>

        <!-- 基本信息表单 -->
        <div class="info-form" :class="{ 'editing': isEditing }">
          <div class="form-item">
            <label>用户名</label>
            <el-input 
              v-if="isEditing" 
              v-model="userForm.username"
              :rules="[
                { required: true, message: '请输入用户名' },
                { min: 3, message: '用户名至少3个字符' }
              ]"
            />
            <span v-else>{{ userForm.username }}</span>
          </div>

          <div class="form-item">
            <label>签名</label>
            <el-input 
              v-if="isEditing" 
              v-model="userForm.signature"
              placeholder="写点什么介绍自己吧..."
            />
            <span v-else>{{ userForm.signature || '这个人很懒，什么都没写~' }}</span>
          </div>

          <div class="form-item">
            <label>邮箱</label>
            <el-input 
              v-if="isEditing" 
              v-model="userForm.email"
              :rules="[
                { required: true, message: '请输入邮箱' },
                { type: 'email', message: '请输入正确的邮箱格式' }
              ]"
            />
            <span v-else>{{ userForm.email }}</span>
          </div>

          <div class="form-item">
            <label>手机号</label>
            <el-input 
              v-if="isEditing" 
              v-model="userForm.phone"
              :rules="[
                { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式' }
              ]"
            />
            <span v-else>{{ userForm.phone || '未设置' }}</span>
          </div>

          <div class="form-item">
            <label>QQ</label>
            <el-input 
              v-if="isEditing" 
              v-model="userForm.qq"
              :rules="[
                { pattern: /^\d{5,11}$/, message: '请输入正确的QQ号格式' }
              ]"
            />
            <span v-else>{{ userForm.qq || '未设置' }}</span>
          </div>
        </div>
      </div>

      <!-- 收藏列表部分 -->
      <div class="collections-section">
        <div class="section-header">
          <h3>我的收藏</h3>
          <span class="count">共 {{ collections.length }} 本</span>
        </div>

        <div class="collections-grid">
          <el-empty 
            v-if="!collections || collections.length === 0" 
            description="暂无收藏" 
          />
          
          <template v-else>
            <el-card
              v-for="book in collections"
              :key="book.id"
              class="book-card"
              shadow="hover"
            >
              <div class="book-cover">
                <el-image
                  :src="getBookCover(book.coverUrl)"
                  fit="cover"
                  :preview-src-list="[getBookCover(book.coverUrl)]"
                >
                  <template #error>
                    <div class="image-placeholder">
                      <el-icon><Picture /></el-icon>
                    </div>
                  </template>
                </el-image>
              </div>
              <div class="book-info">
                <h4 class="book-title">{{ book.name }}</h4>
                <p class="book-author">{{ book.author }}</p>
                <div class="book-tags" v-if="book.tags && book.tags.length">
                  <el-tag
                    v-for="tag in book.tags"
                    :key="tag"
                    size="small"
                    class="tag"
                  >
                    {{ tag }}
                  </el-tag>
                </div>
              </div>
            </el-card>
          </template>
        </div>
      </div>

      <!-- 添加健康提醒部分 -->
      <div v-if="hasLowScoreTests" class="health-alert">
        <el-alert
          type="warning"
          :closable="false"
          show-icon
        >
          <template #title>
            健康提醒
          </template>
          <template #default>
            <p>您最近的心理测试结果显示您的心理状态可能需要关注。</p>
            <div class="alert-content">
              <p>建议您：</p>
              <ul>
                <li>咨询专业的心理健康专家</li>
                <li>拨打心理咨询热线：400-161-9995</li>
                <li>到专业医疗机构进行心理健康检查</li>
              </ul>
            </div>
          </template>
        </el-alert>
      </div>
    </el-card>

    <!-- 修改密码对话框 -->
    <el-dialog
      v-model="showPasswordDialog"
      title="修改密码"
      width="400px"
      @close="handleCloseDialog"
    >
      <el-form
        ref="passwordFormRef"
        :model="passwordForm"
        :rules="passwordRules"
        label-width="100px"
      >
        <template v-if="!passwordVerified">
          <el-form-item label="当前密码" prop="currentPassword">
            <el-input
              v-model="passwordForm.currentPassword"
              type="password"
              show-password
              placeholder="请输入当前密码"
            />
          </el-form-item>
          <div class="dialog-footer">
            <el-button @click="showPasswordDialog = false">取消</el-button>
            <el-button 
              type="primary" 
              @click="verifyCurrentPassword"
              :loading="loading"
            >
              验证
            </el-button>
          </div>
        </template>

        <template v-else>
          <div class="verified-info">
            <el-alert
              type="success"
              :closable="false"
              show-icon
            >
              当前密码验证成功，请设置新密码
            </el-alert>
          </div>
          
          <el-form-item label="新密码" prop="newPassword">
            <el-input
              v-model="passwordForm.newPassword"
              type="password"
              show-password
              placeholder="请输入新密码"
            />
          </el-form-item>
          
          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input
              v-model="passwordForm.confirmPassword"
              type="password"
              show-password
              placeholder="请确认新密码"
            />
          </el-form-item>
          
          <div class="dialog-footer">
            <el-button @click="showPasswordDialog = false">取消</el-button>
            <el-button 
              type="primary" 
              @click="handleChangePassword"
              :loading="loading"
            >
              确认修改
            </el-button>
          </div>
        </template>
      </el-form>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Picture } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import { useCollectionsStore } from '@/stores/collections'
import { storeToRefs } from 'pinia'
import { userAPI } from '@/api'
import { useTestRecordsStore } from '@/stores/testRecords'
import { processImageUrl } from '@/api/book'

const authStore = useAuthStore()
const collectionsStore = useCollectionsStore()
const { collections } = storeToRefs(collectionsStore)

const defaultAvatar = '#1677FF'
const isEditing = ref(false)
const formRef = ref(null)
const loading = ref(false)

// 用户表单数据
const userForm = reactive({
  username: '',
  email: '',
  phone: '',
  qq: '',
  avatar: defaultAvatar,
  currentPassword: '',
  password: '',
  confirmPassword: '',
  signature: ''
})

// 密码修改相关
const showPasswordDialog = ref(false)
const passwordVerified = ref(false)
const passwordFormRef = ref(null)

const passwordForm = reactive({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 密码验证规则
const validateNewPassword = (rule, value, callback) => {
  if (value === passwordForm.currentPassword) {
    callback(new Error('新密不能与当前密码相同'))
  } else {
    callback()
  }
}

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const passwordRules = {
  currentPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能小于6个字符', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能小于6个字符', trigger: 'blur' },
    { validator: validateNewPassword, trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

// 验证当前密码
const verifyCurrentPassword = async () => {
  try {
    await passwordFormRef.value.validateField('currentPassword')
    loading.value = true
    
    const response = await userAPI.verifyPassword(passwordForm.currentPassword)
    
    if (response.code === 200) {
      if (response.data) { // 检查验证结果
        ElMessage.success('密码验证成功')
        passwordVerified.value = true
      } else {
        ElMessage.error('当前密码错误')
        passwordForm.currentPassword = '' // 清空密码输入
      }
    } else {
      ElMessage.error(response.message || '密码验证失败')
    }
  } catch (error) {
    console.error('密码验证失败:', error)
    ElMessage.error(error.response?.data?.message || '密码验证失败')
  } finally {
    loading.value = false
  }
}

// 修改密码
const handleChangePassword = async () => {
  if (!passwordVerified.value) {
    ElMessage.warning('请先验证当前密码')
    return
  }

  try {
    await passwordFormRef.value.validate()
    loading.value = true
    
    const response = await userAPI.updatePassword({
      currentPassword: passwordForm.currentPassword,
      newPassword: passwordForm.newPassword
    })
    
    if (response.code === 200) {
      ElMessage.success('密码修改成功')
      showPasswordDialog.value = false
      resetPasswordForm()
    } else {
      ElMessage.error(response.message || '密码修改失败')
    }
  } catch (error) {
    console.error('密码修改失败:', error)
    ElMessage.error(error.response?.data?.message || '密码修改失败')
  } finally {
    loading.value = false
  }
}

// 重置密码表单时重置验证状态
const resetPasswordForm = () => {
  passwordVerified.value = false
  passwordForm.currentPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
  if (passwordFormRef.value) {
    passwordFormRef.value.resetFields()
  }
}

// 关闭对话框时重置所有状态
const handleCloseDialog = () => {
  showPasswordDialog.value = false
  resetPasswordForm()
}

// 获取用户信息
const fetchUserProfile = async () => {
  try {
    loading.value = true
    const response = await userAPI.getProfile()
    
    if (response.code === 200) {
      const profile = response.data
      Object.assign(userForm, profile)
    } else {
      // 如果获取失败但不是 401 错误，显示错误消息但不跳转
      ElMessage.error(response.message || '获取用户信息失败')
    }
  } catch (error) {
    // 只在非 401 错误时显示错误消息
    if (error.response?.status !== 401) {
      ElMessage.error('获取用户信息失败')
    }
  } finally {
    loading.value = false
  }
}

// 添加错误处理
onMounted(async () => {
  try {
    await fetchUserProfile()
  } catch (error) {
    console.error('Failed to fetch user profile:', error)
  }
})

// 处理编辑/保存
const handleSave = async () => {
  try {
    loading.value = true
    
    // 表单验证
    if (!userForm.username || !userForm.email) {
      ElMessage.error('请填写必要信息')
      return
    }
    
    // 邮箱格���验证
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
    if (!emailRegex.test(userForm.email)) {
      ElMessage.error('请输入正确的邮箱格式')
      return
    }
    
    // 手机号格式验证（如果填写了的话）
    if (userForm.phone && !/^1[3-9]\d{9}$/.test(userForm.phone)) {
      ElMessage.error('请输入正确的手机号格式')
      return
    }
    
    // QQ号格式验证（如果填写了的话）
    if (userForm.qq && !/^\d{5,11}$/.test(userForm.qq)) {
      ElMessage.error('请输入正确的QQ号格式')
      return
    }
    
    // 构建更新数据
    const updateData = {
      username: userForm.username,
      email: userForm.email,
      phone: userForm.phone || null,
      qq: userForm.qq || null,
      signature: userForm.signature || null
    }
    
    const response = await userAPI.updateProfile(updateData)
    
    if (response.code === 200) {
      ElMessage.success('保存成功')
      isEditing.value = false
      // 更新本地用户信息
      Object.assign(userForm, response.data)
    } else {
      ElMessage.error(response.message || '保存失败')
    }
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error(error.response?.data?.message || '保存失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// handleEdit 简化为切换编辑状态
const handleEdit = () => {
  if (isEditing.value) {
    handleSave()
  } else {
    isEditing.value = true
  }
}

const testStore = useTestRecordsStore()

// 检查是否有低分测试
const hasLowScoreTests = computed(() => {
  const recentTests = testStore.records
    .filter(record => {
      const testDate = new Date(record.date)
      const oneMonthAgo = new Date()
      oneMonthAgo.setMonth(oneMonthAgo.getMonth() - 1)
      return testDate >= oneMonthAgo
    })
  
  return recentTests.some(test => test.score < 60)
})

// 确保组件挂载时加载收藏列表
onMounted(async () => {
  collectionsStore.initialize()
})

// 添加图片URL处理函数
const getBookCover = (coverUrl) => {
  return processImageUrl(coverUrl)
}
</script>

<style lang="scss" scoped>
.profile-page {
  @apply p-6;
}

.profile-card {
  @apply min-h-[700px];
}

.profile-section {
  @apply mb-8;
}

.info-header {
  @apply flex items-center justify-between mb-6;
  
  .user-info {
    @apply flex items-center gap-6;
    
    .avatar {
      @apply border-4 border-white shadow-lg;
    }
    
    .user-details {
      h2 {
        @apply text-xl font-semibold m-0 mb-2;
      }
      
      .signature {
        @apply text-gray-500 text-sm m-0;
      }
    }
  }
}

.edit-form {
  @apply max-w-2xl mt-6;
  
  .form-actions {
    @apply flex justify-end gap-2 mt-6;
  }
}

.collections-section {
  @apply border-t pt-6;
  
  .section-header {
    @apply flex items-center justify-between mb-6;
    
    h3 {
      @apply text-lg font-medium m-0;
    }
    
    .count {
      @apply text-gray-500 text-sm;
    }
  }
}

.collections-grid {
  @apply grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6;
  
  .book-card {
    @apply flex gap-4;
    
    .book-cover {
      @apply w-28 flex-shrink-0;
      
      .el-image {
        @apply w-full aspect-[3/4] rounded overflow-hidden;
      }
    }
    
    .book-info {
      @apply flex-1 flex flex-col;
      
      .book-title {
        @apply text-lg font-medium mb-1;
      }
      
      .book-author {
        @apply text-gray-600 text-sm mb-2;
      }
      
      .book-tags {
        @apply flex flex-wrap gap-1 mb-2;
        
        .tag {
          @apply text-xs;
        }
      }
    }
  }
}

.info-form {
  @apply mt-8 space-y-6;
  
  .form-item {
    @apply flex items-center;
    
    label {
      @apply w-24 text-gray-500 flex-shrink-0;
    }
    
    span {
      @apply text-gray-900 flex-1;
    }
    
    .el-input {
      @apply flex-1;
    }
  }
  
  &:not(.editing) {
    .form-item {
      @apply py-2;
    }
  }
  
  &.editing {
    .form-item {
      @apply border rounded-lg p-4;
    }
  }
}

.verified-info {
  @apply mb-6;
}

.dialog-footer {
  @apply flex justify-end gap-2 mt-6;
}

// 暗色主题适配
:deep(.dark) {
  .info-header {
    .user-info {
      .avatar {
        @apply border-gray-800;
      }
      
      .signature {
        @apply text-gray-400;
      }
    }
  }
  
  .collections-section {
    @apply border-gray-700;
  }
  
  .book-card {
    .book-author {
      @apply text-gray-400;
    }
  }
  
  .count {
    @apply text-gray-400;
  }
}

.health-alert {
  @apply mb-6;
  
  .alert-content {
    @apply mt-2;
    
    ul {
      @apply list-disc list-inside mt-2 space-y-1;
    }
  }
}
</style> 