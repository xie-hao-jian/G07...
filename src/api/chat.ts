import type { ChatMessage } from '@/types'

const API_KEY = 'sk-Tyw5d2JRl453b38xFf4d5545Fc5741D89e09853bE4A7C5B1'
const API_URL = 'https://aiproxy.hzh.sealos.run/v1/chat/completions'

// 系统角色设定
const SYSTEM_PROMPT = `你是一个专业的心理咨询助手。你需要:
1. 以专业、友善和同理心的态度倾听用户的问题
2. 提供科学、实用的心理建议和解决方案
3. 在交谈中保持积极正向的态度
4. 注意保护用户的隐私信息
5. 如果发现用户有严重的心理问题,建议其寻求专业的心理医生帮助
6. 使用温和、关怀的语气与用户交流`

export async function sendChatMessage(messages: ChatMessage[]) {
  try {
    const response = await fetch(API_URL, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${API_KEY}`
      },
      body: JSON.stringify({
        model: 'Doubao-lite-4k',
        messages: [
          // 添加系统角色设定
          { role: 'system', content: SYSTEM_PROMPT },
          // 转换用户消息
          ...messages.map(msg => ({
            role: msg.isUser ? 'user' : 'assistant',
            content: msg.content
          }))
        ],
        max_tokens: 2048,
        temperature: 0.7,
      })
    })

    if (!response.ok) {
      throw new Error(`API请求失败: ${response.status}`)
    }

    const data = await response.json()
    return data.choices[0].message.content
  } catch (error) {
    console.error('发送消息失败:', error)
    throw error
  }
} 