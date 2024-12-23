import axios from 'axios'
import { handleApiError } from '../utils/errorHandler'
import type { 
  LoginRequest, 
  RegisterRequest,
  AuthResponse,
  ApiResponse,
  User
} from '../types'

const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 5000
})

// 认证相关接口
export const authAPI = {
  // 登录
  login: (data: LoginRequest) => 
    api.post<ApiResponse<AuthResponse>>('/auth/login', data),
    
  // 注册  
  register: (data: RegisterRequest) => 
    api.post<ApiResponse<AuthResponse>>('/auth/register', data),
    
  // 登出
  logout: () => 
    api.post<ApiResponse<null>>('/auth/logout'),
    
  // 重置密码
  resetPassword: (data: { username: string; newPassword: string }) =>
    api.post<ApiResponse<null>>('/auth/reset-password', data)
}

// 请求拦截器 - 添加token
api.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})
// 响应拦截器 - 统一错误处理
api.interceptors.response.use(
  response => response.data,
  error => {
    handleApiError(error)
    return Promise.reject(error)
  }
)

// 用户相关接口
export const userAPI = {
  // 获取用户信息
  getProfile: () => 
    api.get<ApiResponse<User>>('/user/profile'),
    
  // 更新基本信息
  updateProfile: (data: Partial<User>) => 
    api.put<ApiResponse<User>>('/user/profile', data),
    
  // 修改密码
  updatePassword: (data: { 
    currentPassword: string;
    newPassword: string;
  }) => 
    api.put<ApiResponse<null>>('/user/password', data),
    
  // 验证密码
  verifyPassword: (password: string) =>
    api.post<ApiResponse<boolean>>('/user/verify-password', { password })
}

export default api 