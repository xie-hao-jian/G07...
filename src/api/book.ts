import api from './index'
import type { Book, ApiResponse, PaginationResponse } from '../types'

// 图片基础URL配置
export const BASE_IMAGE_URL = 'http://localhost:8080/images/'

// 图片URL处理函数
export const processImageUrl = (coverUrl: string) => {
  if (!coverUrl) {
    console.warn('收到空的 coverUrl')
    return ''
  }
  
  // 如果已经是完整URL，直接返回
  if (coverUrl.startsWith('http')) {
    return coverUrl
  }
  
  // 获取 token
  const token = localStorage.getItem('token')
  
  // 构建带认证的URL
  const cleanUrl = coverUrl.replace(/^\/+/, '')
  const url = new URL(`${BASE_IMAGE_URL}${cleanUrl}`)
  
  // 添加 token 参数
  if (token) {
    url.searchParams.append('token', token)
  }
  
  return url.toString()
}

// 图书相关 API
export const bookAPI = {
  // 获取今日推荐
  getTodayRecommend: () => 
    api.get<ApiResponse<Book>>('/books/today-recommend'),
  
  // 获取历史推荐列表(分页)
  getHistoryRecommends: (page: number = 1) => 
    api.get<ApiResponse<PaginationResponse<Book>>>('/books/history-recommends', {
      params: { page }
    }),
  
  // 搜索图书
  searchBooks: (keyword: string) =>
    api.get<ApiResponse<Book[]>>('/books/search', {
      params: { keyword }
    })
} 