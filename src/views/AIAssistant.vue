<template>
  <div class="ai-assistant">
    <el-card class="chat-card">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-avatar :size="32" :src="aiAvatar" />
            <h2>AI心理助手</h2>
            <el-tag type="success" size="small">在线</el-tag>
          </div>
          <div class="header-right">
            <el-button 
              type="danger" 
              plain
              size="small"
              @click="handleClearChat"
              class="clear-btn"
            >
              <el-icon><Delete /></el-icon>
              清空对话
            </el-button>
          </div>
        </div>
      </template>

      <div class="chat-main">
        <div class="chat-container" ref="chatContainer">
          <!-- 空聊天提示 -->
          <div v-if="messages.length === 0" class="empty-chat">
            <el-empty description="开始和AI助手聊天吧">
              <template #image>
                <el-icon :size="60"><ChatRound /></el-icon>
              </template>
            </el-empty>
            <div class="quick-starts">
              <h3>您可以这样问：</h3>
              <div class="suggestions">
                <el-button
                  v-for="suggestion in suggestions"
                  :key="suggestion"
                  @click="handleQuickStart(suggestion)"
                >
                  {{ suggestion }}
                </el-button>
              </div>
            </div>
          </div>

          <!-- 消息列表 -->
          <div v-else class="message-list">
            <div
              v-for="(message, index) in messages"
              :key="index"
              class="message-item"
              :class="{ 'message-ai': !message.isUser, 'message-user': message.isUser }"
            >
              <el-avatar
                :size="36"
                :src="message.isUser ? userAvatar : aiAvatar"
              />
              <div class="message-content">
                <div class="message-bubble" :class="{ 'thinking': message.thinking }">
                  <template v-if="message.thinking">
                    <span class="typing-indicator">
                      <i></i><i></i><i></i>
                    </span>
                  </template>
                  <template v-else>
                    {{ message.content }}
                  </template>
                </div>
                <span class="message-time">{{ message.time }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 输入区域 -->
        <div class="chat-input-wrapper">
          <div class="chat-input">
            <el-input
              v-model="inputMessage"
              type="textarea"
              :rows="3"
              placeholder="输入您想说的话..."
              :disabled="loading"
              @keyup.enter.exact="handleSendMessage"
            />
            <div class="input-actions">
              <el-button
                type="primary"
                :loading="loading"
                @click="handleSendMessage"
              >
                发送
                <template #icon>
                  <el-icon><Position /></el-icon>
                </template>
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, nextTick, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ChatRound, Position, Delete } from '@element-plus/icons-vue'
import { sendChatMessage } from '@/api/chat'

// 模拟数据
const aiAvatar = 'https://api.dicebear.com/7.x/bottts/svg?seed=ai-assistant'  // AI风格头像
const userAvatar = 'https://api.dicebear.com/7.x/avataaars/svg?seed=user'     // 用户风格头像
const suggestions = [
  '最近感觉压力很大，该怎么办？',
  '如何改善睡眠质量？',
  '怎样提高自信心？'
]

const chatContainer = ref(null)
const inputMessage = ref('')
const loading = ref(false)
const messages = ref([])

// 发送消息
const handleSendMessage = async () => {
  const message = inputMessage.value.trim()
  if (!message) return
  
  // 添加用户消息
  messages.value.push({
    content: message,
    isUser: true,
    time: new Date().toLocaleTimeString()
  })
  
  // 清空输入框
  inputMessage.value = ''
  
  // 滚动到底部
  await nextTick()
  scrollToBottom()
  
  // 添加AI思考状态
  messages.value.push({
    thinking: true,
    isUser: false,
    time: new Date().toLocaleTimeString()
  })
  
  // 调用API获取响应
  loading.value = true
  try {
    const response = await sendChatMessage(messages.value.filter(msg => !msg.thinking))
    
    // 移除思考状态
    messages.value.pop()
    
    // 添加AI回复
    messages.value.push({
      content: response,
      isUser: false,
      time: new Date().toLocaleTimeString()
    })
  } catch (error) {
    ElMessage.error('发送消息失败')
  } finally {
    loading.value = false
    await nextTick()
    scrollToBottom()
  }
}

// 快速开始对话
const handleQuickStart = (suggestion) => {
  inputMessage.value = suggestion
  handleSendMessage()
}

// 清空对话
const handleClearChat = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要清空所有对话记录吗？',
      '清空确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    messages.value = []
  } catch (error) {
    // 用户取消操作
  }
}

// 滚动到底部
const scrollToBottom = () => {
  const container = chatContainer.value
  if (container) {
    container.scrollTop = container.scrollHeight
  }
}

onMounted(() => {
  // 初始化欢迎消息
  messages.value.push({
    content: '你好！我是你的AI心理助手，有什么想和我聊的吗？',
    isUser: false,
    time: new Date().toLocaleTimeString()
  })
})
</script>

<style lang="scss" scoped>
.ai-assistant {
  @apply h-[calc(100vh-120px)] p-6;
}

.chat-card {
  @apply h-full flex flex-col;
  
  :deep(.el-card__body) {
    @apply flex-1 overflow-hidden;
  }
  
  .card-header {
    @apply flex items-center justify-between;
    
    .header-left {
      @apply flex items-center gap-3;
      
      h2 {
        @apply text-xl font-semibold m-0;
      }
    }
    
    .header-right {
      .clear-btn {
        @apply hover:bg-red-50;
      }
    }
  }
}

.chat-main {
  @apply flex flex-col h-full;
}

.chat-container {
  @apply flex-1 overflow-y-auto p-4 min-h-0;
  
  .empty-chat {
    @apply flex flex-col items-center justify-center h-full space-y-8;
    
    .quick-starts {
      @apply text-center space-y-4;
      
      .suggestions {
        @apply flex flex-wrap gap-2 justify-center;
      }
    }
  }
  
  .message-list {
    @apply space-y-6 h-full;
  }
  
  .message-item {
    @apply flex gap-3;
    
    &.message-user {
      @apply flex-row-reverse;
      
      .message-bubble {
        @apply bg-blue-500 text-white;
      }
      
      .message-time {
        @apply text-right;
      }
    }
  }
  
  .message-content {
    @apply flex-1 max-w-[70%];
    
    .message-bubble {
      @apply bg-gray-100 rounded-lg p-3 break-words;
      
      &.thinking {
        @apply min-w-[60px];
      }
    }
    
    .message-time {
      @apply text-xs text-gray-500 mt-1 block;
    }
  }
}

.chat-input-wrapper {
  @apply mt-4 border-t bg-white;
}

.chat-input {
  @apply p-4 space-y-2;
  
  .input-actions {
    @apply flex justify-end;
  }
}

// 打字动画
.typing-indicator {
  @apply flex gap-1 justify-center;
  
  i {
    @apply w-2 h-2 bg-gray-400 rounded-full;
    animation: typing 1s infinite;
    
    &:nth-child(2) { animation-delay: 0.2s; }
    &:nth-child(3) { animation-delay: 0.4s; }
  }
}

@keyframes typing {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-5px); }
}

// 暗色主题适配
:deep(.dark) {
  .message-bubble {
    &:not(.message-user .message-bubble) {
      @apply bg-gray-800 text-gray-200;
    }
  }
  
  .typing-indicator i {
    @apply bg-gray-500;
  }
}
</style> 