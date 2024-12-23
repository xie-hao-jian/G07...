<template>
  <div class="dashboard">
    <!-- 每日推荐书籍卡片 -->
    <el-card class="book-card">
      <template #header>
        <div class="card-header">
          <h2>今日推荐书籍</h2>
          <el-tag type="success">{{ currentDate }}</el-tag>
        </div>
      </template>
      
      <div class="book-content" v-loading="loading">
        <template v-if="book">
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
            <h3 class="book-title">{{ book.name }}</h3>
            <p class="book-author">作者：{{ book.author }}</p>
            <p class="book-publish-date">出版日期：{{ formatDate(book.publishDate) }}</p>
            <p class="book-description">{{ book.description }}</p>
            
            <div class="book-actions">
              <el-button type="primary" @click="handleReadMore">
                了解更多
                <el-icon class="el-icon--right"><ArrowRight /></el-icon>
              </el-button>
              <el-button 
                type="primary" 
                @click="handleToggleCollection(book)"
                :class="{ 'is-collected': isBookCollected(book.id) }"
              >
                <el-icon><Star /></el-icon>
                {{ isBookCollected(book.id) ? '取消收藏' : '收藏' }}
              </el-button>
            </div>
          </div>
        </template>
        
        <el-empty v-else description="暂无推荐书籍" />
      </div>
    </el-card>

    <!-- 历史推荐列表 -->
    <el-card class="history-card">
      <template #header>
        <div class="card-header">
          <h2>历史推荐</h2>
          <div class="search-section">
            <el-autocomplete
              v-model="searchKeyword"
              :fetch-suggestions="handleSearchSuggestions"
              placeholder="搜索书籍..."
              class="search-input"
              :trigger-on-focus="false"
              @select="handleSelectBook"
              clearable
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
              
              <template #default="{ item }">
                <div class="suggestion-item">
                  <el-image
                    :src="getBookCover(item.coverUrl)"
                    class="suggestion-cover"
                    fit="cover"
                  >
                    <template #error>
                      <div class="image-placeholder">
                        <el-icon><Picture /></el-icon>
                      </div>
                    </template>
                  </el-image>
                  <div class="suggestion-info">
                    <div class="suggestion-name">{{ item.name }}</div>
                    <div class="suggestion-author">{{ item.author }}</div>
                  </div>
                </div>
              </template>
            </el-autocomplete>
          </div>
        </div>
      </template>
      
      <el-table 
        :data="historyBooks" 
        stripe
        v-loading="tableLoading"
      >
        <template #empty>
          <el-empty description="暂无数据" />
        </template>
        
        <el-table-column label="封面" width="80">
          <template #default="{ row }">
            <el-image
              :src="getBookCover(row.coverUrl)"
              fit="cover"
              style="width: 50px; height: 70px; border-radius: 4px;"
            >
              <template #error>
                <div class="image-placeholder">
                  <el-icon><Picture /></el-icon>
                </div>
              </template>
            </el-image>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="书名" />
        <el-table-column prop="author" label="作者" width="150" />
        <el-table-column label="出版日期" width="120">
          <template #default="{ row }">
            {{ formatDate(row.publishDate) }}
          </template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="180">
          <template #default="{ row }">
            <el-button 
              link 
              type="primary" 
              @click="handleViewBook(row)"
            >
              查看详情
            </el-button>
            <el-button
              link
              type="primary"
              @click="handleToggleCollection(row)"
            >
              {{ isBookCollected(row.id) ? '取消收藏' : '收藏' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页器 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          :page-size="3"
          :total="total"
          :pager-count="5"
          background
          layout="prev, pager, next"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 书籍详情对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="书籍详情"
      width="60%"
      :before-close="handleCloseDialog"
    >
      <div class="book-detail" v-if="selectedBook">
        <div class="detail-content">
          <div class="detail-cover">
            <el-image
              :src="getBookCover(selectedBook.coverUrl)"
              fit="cover"
              :preview-src-list="[getBookCover(selectedBook.coverUrl)]"
            >
              <template #error>
                <div class="image-placeholder">
                  <el-icon><Picture /></el-icon>
                </div>
              </template>
            </el-image>
          </div>
          <div class="detail-info">
            <h3>{{ selectedBook.name }}</h3>
            <p><strong>作者：</strong>{{ selectedBook.author }}</p>
            <p><strong>出版日期：</strong>{{ formatDate(selectedBook.publishDate) }}</p>
            <p class="description"><strong>简介：</strong>{{ selectedBook.description }}</p>
          </div>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">关闭</el-button>
          <el-button 
            type="primary" 
            @click="handleToggleCollection(selectedBook)"
          >
            {{ isBookCollected(selectedBook?.id) ? '取消收藏' : '收藏' }}
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Picture, ArrowRight, Star, Search } from '@element-plus/icons-vue'
import { useCollectionsStore } from '@/stores/collections'
import { bookAPI, processImageUrl } from '@/api/book'
import type { Book } from '@/types'

const loading = ref(false)
const tableLoading = ref(false)
const book = ref(null)
const historyBooks = ref([])
const dialogVisible = ref(false)
const selectedBook = ref(null)
const collectionsStore = useCollectionsStore()

// 分页相关
const currentPage = ref(1)
const total = ref(9)
const searchKeyword = ref('')
const searching = ref(false)

const currentDate = computed(() => {
  return new Date().toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
})

// 格式化日期
const formatDate = (date) => {
  if (!date) return '暂无'
  return new Date(date).toLocaleDateString('zh-CN')
}

const getTodayKey = () => {
  return new Date().toLocaleDateString('zh-CN')
}

// 获取今日推荐书籍
const fetchTodayBook = async () => {
  try {
    // 获取当前日期
    const today = getTodayKey()
    // 从 localStorage 获取已存储的推荐
    const savedRecommend = localStorage.getItem('todayRecommend')
    
    if (savedRecommend) {
      const { date, book: savedBook } = JSON.parse(savedRecommend)
      // 如果是同一天且有存储的书籍，直接使用
      if (date === today && savedBook) {
        book.value = savedBook
        return
      }
    }
    
    // 如果是新的一天或没有存储的书籍，则请求新的推荐
    loading.value = true
    const response = await bookAPI.getTodayRecommend()
    
    if (response.code === 200) {
      book.value = response.data
      // 存储今日推荐和日期
      localStorage.setItem('todayRecommend', JSON.stringify({
        date: today,
        book: response.data
      }))
    }
  } catch (error) {
    console.error('获取推荐书籍失败:', error)
    ElMessage.error('获取推荐书籍失败')
  } finally {
    loading.value = false
  }
}

// 获取历史推荐列表
const fetchHistoryBooks = async () => {
  tableLoading.value = true
  try {
    const response = await bookAPI.getHistoryRecommends(currentPage.value)
    console.log('API Response:', {
      code: response.code,
      total: response.data.total,
      records: response.data.records.map(book => ({
        id: book.id,
        name: book.name,
        coverUrl: book.coverUrl
      }))
    })
    if (response.code === 200) {
      historyBooks.value = response.data.records.map(book => ({
        ...book,
        coverUrl: book.coverUrl || ''
      }))
      total.value = response.data.total
      currentPage.value = response.data.current
    }
  } catch (error) {
    console.error('Error:', error)
    ElMessage.error('获取历史推荐失败')
  } finally {
    tableLoading.value = false
  }
}

// 处理搜索建议
const handleSearchSuggestions = async (query, callback) => {
  if (!query) {
    callback([])
    return
  }
  
  searching.value = true
  try {
    const response = await bookAPI.searchBooks(query)
    if (response.code === 200) {
      const suggestions = response.data.map(book => ({
        ...book,
        value: book.name // 用于显示在输入框中的值
      }))
      callback(suggestions)
    } else {
      callback([])
    }
  } catch (error) {
    console.error('搜索失败:', error)
    callback([])
  } finally {
    searching.value = false
  }
}

// 处理选择书籍
const handleSelectBook = (book) => {
  if (book) {
    handleViewBook(book)
    searchKeyword.value = '' // 清空搜索框
  }
}

// 处理页码变化
const handleCurrentChange = (page) => {
  currentPage.value = page
  fetchHistoryBooks()
}

// 查看书籍详情
const handleViewBook = (book) => {
  selectedBook.value = book
  dialogVisible.value = true
}

// 关闭详情对话框
const handleCloseDialog = () => {
  dialogVisible.value = false
  selectedBook.value = null
}

// 了解更多
const handleReadMore = () => {
  handleViewBook(book.value)
}

// 断书籍是否已收藏
const isBookCollected = (bookId) => {
  return collectionsStore.isCollected(bookId)
}

// 处理收藏/取消收藏
const handleToggleCollection = async (book) => {
  if (!book) return
  
  try {
    const isNowCollected = collectionsStore.toggleCollection(book)
    ElMessage.success(isNowCollected ? '收藏成功' : '已取消收藏')
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

// 添加图片URL处理函数
const getBookCover = (coverUrl) => {
  return processImageUrl(coverUrl)
}

onMounted(() => {
  fetchTodayBook()
  fetchHistoryBooks()
})

onUnmounted(() => {
  // 获取当前日期
  const today = getTodayKey()
  const savedRecommend = localStorage.getItem('todayRecommend')
  
  if (savedRecommend) {
    const { date } = JSON.parse(savedRecommend)
    // 如果存储的推荐不是今天的，则清除
    if (date !== today) {
      localStorage.removeItem('todayRecommend')
    }
  }
})
</script>

<style lang="scss" scoped>
.dashboard {
  padding: 24px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  
  h2 {
    font-size: 1.25rem;
    font-weight: 600;
    margin: 0;
  }
  
  .search-box {
    width: 300px;
  }
}

.book-content {
  display: flex;
  gap: 24px;
}

.book-cover {
  width: 200px;
  flex-shrink: 0;
  
  .el-image {
    width: 100%;
    height: 280px;
    border-radius: 8px;
    overflow: hidden;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  }
}

.book-info {
  flex: 1;
  
  .book-title {
    font-size: 1.5rem;
    font-weight: 700;
    margin-bottom: 12px;
    color: var(--el-color-primary);
  }
  
  .book-author,
  .book-publish-date {
    color: #606266;
    margin-bottom: 12px;
  }
  
  .book-description {
    color: #303133;
    margin-bottom: 24px;
    line-height: 1.6;
  }
}

.book-actions {
  display: flex;
  gap: 16px;
}

.history-card {
  margin-top: 24px;
}

.pagination-container {
  margin-top: 20px;
  padding: 10px;
  background-color: #fff;
  border-radius: 4px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);

  :deep(.el-pagination) {
    justify-content: center;
    padding: 0;
    
    .el-pagination__sizes {
      margin-right: 16px;
    }
    
    .el-pager li {
      background-color: transparent;
      
      &:not(.is-active):hover {
        color: var(--el-color-primary);
      }
      
      &.is-active {
        background-color: var(--el-color-primary);
        color: #fff;
      }
    }
    
    button:not(:disabled):hover {
      color: var(--el-color-primary);
    }
  }
}

.book-detail {
  .detail-content {
    display: flex;
    gap: 24px;
    
    .detail-cover {
      width: 180px;
      flex-shrink: 0;
      
      .el-image {
        width: 100%;
        height: 250px;
        border-radius: 8px;
        overflow: hidden;
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
      }
    }
    
    .detail-info {
      flex: 1;
      
      h3 {
        color: var(--el-color-primary);
        margin-top: 0;
      }
      
      .description {
        line-height: 1.6;
      }
    }
  }
}

/* 暗色主题适配 */
:deep(.dark) {
  .book-info {
    .book-author,
    .book-publish-date {
      color: #a3a6ad;
    }
    
    .book-description {
      color: #e5e7eb;
    }
  }
  
  .image-placeholder {
    background-color: #2c2c2c;
    
    .el-icon {
      color: #606266;
    }
  }
  
  .pagination-container {
    background-color: #1a1a1a;
    
    .el-pagination {
      .el-pager li:not(.is-active) {
        background-color: transparent;
        color: #e5e7eb;
        
        &:hover {
          color: var(--el-color-primary);
        }
      }
    }
  }
}

/* 更新搜索相关样式 */
.search-section {
  width: 100%;
  max-width: 500px;
  margin: 0 auto;
  
  .search-input {
    width: 100%;
  }
}

.suggestion-item {
  display: flex;
  align-items: center;
  padding: 8px;
  cursor: pointer;
  
  .suggestion-cover {
    width: 40px;
    height: 56px;
    border-radius: 4px;
    margin-right: 12px;
  }
  
  .suggestion-info {
    flex: 1;
    
    .suggestion-name {
      font-weight: 500;
      margin-bottom: 4px;
    }
    
    .suggestion-author {
      font-size: 12px;
      color: var(--el-text-color-secondary);
    }
  }
  
  &:hover {
    background-color: var(--el-fill-color-light);
  }
}

/* 暗色主题适配 */
:deep(.dark) {
  .suggestion-item {
    &:hover {
      background-color: var(--el-fill-color-darker);
    }
  }
}

.image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f7fa;
  color: #909399;
  font-size: 20px;
}
</style> 