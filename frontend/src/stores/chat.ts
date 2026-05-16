import { defineStore } from 'pinia'
import { ref } from 'vue'
import { sessionApi, chatApi, userApi } from '../api'

export interface UserProfile {
  id?: number
  nickname: string
  age?: number
  gender?: string
  occupation?: string
  interests?: string
  personality?: string
  extraInfo?: string
}

export interface ChatSession {
  id: number
  userId: number
  title: string
  createdAt: string
  updatedAt: string
}

export interface ChatMessage {
  id: number
  sessionId: number
  role: 'user' | 'assistant' | 'system'
  content: string
  createdAt: string
}

export const useChatStore = defineStore('chat', () => {
  // 当前用户
  const currentUser = ref<UserProfile | null>(null)
  // 当前会话
  const currentSession = ref<ChatSession | null>(null)
  // 会话列表
  const sessions = ref<ChatSession[]>([])
  // 消息列表
  const messages = ref<ChatMessage[]>([])
  // 加载状态
  const loading = ref(false)
  // 流式输出中
  const streaming = ref(false)

  // 加载用户信息
  async function loadUser(userId: number) {
    const res = await userApi.getProfile(userId)
    currentUser.value = res.data
  }

  // 保存用户信息
  async function saveUser(user: UserProfile) {
    const res = await userApi.saveProfile(user)
    currentUser.value = res.data
    return res.data
  }

  // 加载会话列表
  async function loadSessions(userId: number) {
    const res = await sessionApi.list(userId)
    sessions.value = res.data
  }

  // 创建新会话
  async function createSession(userId: number, title?: string) {
    const res = await sessionApi.create(userId, title)
    const session = res.data
    sessions.value.unshift(session)
    currentSession.value = session
    messages.value = []
    return session
  }

  // 切换会话
  async function switchSession(session: ChatSession) {
    currentSession.value = session
    await loadMessages(session.id)
  }

  // 删除会话
  async function deleteSession(sessionId: number) {
    await sessionApi.delete(sessionId)
    sessions.value = sessions.value.filter(s => s.id !== sessionId)
    if (currentSession.value?.id === sessionId) {
      currentSession.value = sessions.value[0] || null
      if (currentSession.value) {
        await loadMessages(currentSession.value.id)
      } else {
        messages.value = []
      }
    }
  }

  // 加载消息
  async function loadMessages(sessionId: number) {
    const res = await chatApi.getMessages(sessionId)
    messages.value = res.data
  }

  // 发送消息（流式）
  async function sendMessageStream(userId: number, message: string, modelConfigId?: number | null) {
    if (!currentSession.value) return

    // 添加用户消息
    const userMsg: ChatMessage = {
      id: Date.now(),
      sessionId: currentSession.value.id,
      role: 'user',
      content: message,
      createdAt: new Date().toISOString()
    }
    messages.value.push(userMsg)

    // 添加助手消息占位
    const assistantMsg: ChatMessage = {
      id: Date.now() + 1,
      sessionId: currentSession.value.id,
      role: 'assistant',
      content: '',
      createdAt: new Date().toISOString()
    }
    messages.value.push(assistantMsg)

    streaming.value = true

    return new Promise<void>((resolve) => {
      let url = `/api/chat/stream?sessionId=${currentSession.value!.id}&userId=${userId}&message=${encodeURIComponent(message)}`
      if (modelConfigId) {
        url += `&modelConfigId=${modelConfigId}`
      }
      const eventSource = new EventSource(url)

      eventSource.onmessage = (event) => {
        const content = event.data
        assistantMsg.content += content
      }

      eventSource.onerror = () => {
        eventSource.close()
        streaming.value = false
        resolve()
      }

      eventSource.addEventListener('complete', () => {
        eventSource.close()
        streaming.value = false
        resolve()
      })
    })
  }

  return {
    currentUser,
    currentSession,
    sessions,
    messages,
    loading,
    streaming,
    loadUser,
    saveUser,
    loadSessions,
    createSession,
    switchSession,
    deleteSession,
    loadMessages,
    sendMessageStream
  }
})
