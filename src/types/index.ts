export interface User {
  id: string
  username: string
  email: string
  phone: string
  qq: string
  status: number
  createTime: string
  updateTime: string
}

export interface TestResult {
  id: string
  userId: string
  testType: string
  score: number
  createdAt: Date
  // ...其他测试结果相关类型
}

export interface TestItem {
  id: string | number
  name: string
  status: 'completed' | 'incomplete'
  createdAt: Date
  // ... 其他属性
}

export interface TestRecord {
  id: string | number
  name: string
  date: string
  grade: 'A' | 'B' | 'C'
  // ... 其他属性
}

export interface LoginRequest {
  username: string
  password: string
}

export interface RegisterRequest {
  username: string
  password: string
}

export interface AuthResponse {
  token: string
  user: User
}

export interface TestQuestion {
  id: string | number
  question: string
  description?: string
  options: Array<{
    label: string
    value: string
  }>
}

export interface TestTemplate {
  id: string
  title: string
  questions: TestQuestion[]
}

export interface TestSubmission {
  id: string
  testId: string
  answers: string[]
  userId: string
  status: 'completed' | 'incomplete'
  grade?: 'A' | 'B' | 'C'
  completedAt?: Date
}

export interface ChatMessage {
  content: string
  isUser: boolean
  time: string
  thinking?: boolean
}

export interface ChatHistory {
  messages: ChatMessage[]
}

export interface Book {
  id: number
  name: string
  author: string
  publishDate: string
  description: string
  coverUrl: string
  tags: string[]
}

export interface BookCollection {
  id: string
  userId: string
  bookId: string
  collectedAt: string
}

export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

export enum ErrorCode {
  SUCCESS = 200,
  UNAUTHORIZED = 401,
  FORBIDDEN = 403,
  NOT_FOUND = 404,
  INTERNAL_ERROR = 500,
  // ... 其他业务错误码
}

export interface BookCollection {
  id: string
  userId: string
  bookId: string
  book: Book
  collectedAt: string
}

export interface PaginationParams {
  page: number
  pageSize: number
  keyword?: string
}

export interface PaginationResponse<T> {
  total: number
  records: T[]
  size: number
  current: number
  pages: number
}
