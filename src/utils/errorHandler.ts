import { ElMessage } from 'element-plus'
import type { ApiResponse } from '../types'

export const handleApiError = (error: any) => {
  if (error.response) {
    const response = error.response.data as ApiResponse<any>
    ElMessage.error(response.message || '请求失败')
  } else if (error.request) {
    ElMessage.error('网络请求失败')
  } else {
    ElMessage.error('发生错误')
  }
  console.error('API错误:', error)
} 