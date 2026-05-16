<script setup lang="ts">
import { ref, onMounted, nextTick, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useChatStore } from '../stores/chat'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../api'

const router = useRouter()
const chatStore = useChatStore()

const isDark = ref(true)
const messageInput = ref('')
const messageListRef = ref<HTMLElement | null>(null)
const userInputVisible = ref(false)
const userForm = ref({
  id: undefined as number | undefined,
  nickname: '',
  age: undefined as number | undefined,
  gender: '',
  occupation: '',
  interests: '',
  personality: '',
  extraInfo: ''
})

const enabledModels = ref<any[]>([])
const selectedModelId = ref<number | null>(null)
const DEFAULT_USER_ID = 1

onMounted(async () => {
  // 初始化主题
  const savedTheme = localStorage.getItem('theme')
  isDark.value = savedTheme ? savedTheme === 'dark' : true
  document.documentElement.setAttribute('data-theme', isDark.value ? 'dark' : 'light')

  try {
    await chatStore.loadUser(DEFAULT_USER_ID)
    if (chatStore.currentUser) {
      Object.assign(userForm.value, chatStore.currentUser)
    }
    await chatStore.loadSessions(DEFAULT_USER_ID)
    if (chatStore.sessions.length > 0) {
      await chatStore.switchSession(chatStore.sessions[0])
    }
  } catch (e) {
    userInputVisible.value = true
  }
  await loadEnabledModels()
})

watch(() => chatStore.messages.length, () => {
  nextTick(() => { scrollToBottom() })
})

async function loadEnabledModels() {
  try {
    const res = await api.get('/ai-model-config/enabled')
    enabledModels.value = res.data
    const defaultModel = enabledModels.value.find((m: any) => m.isDefault)
    if (defaultModel) {
      selectedModelId.value = defaultModel.id
    } else if (enabledModels.value.length > 0) {
      selectedModelId.value = enabledModels.value[0].id
    }
  } catch (error) {
    console.error('Failed to load models:', error)
  }
}

function scrollToBottom() {
  if (messageListRef.value) {
    messageListRef.value.scrollTop = messageListRef.value.scrollHeight
  }
}

async function handleSendMessage() {
  const message = messageInput.value.trim()
  if (!message || chatStore.streaming) return
  if (!chatStore.currentSession) {
    await chatStore.createSession(DEFAULT_USER_ID, message.substring(0, 20))
  }
  messageInput.value = ''
  await chatStore.sendMessageStream(DEFAULT_USER_ID, message, selectedModelId.value)
}

async function handleNewSession() {
  await chatStore.createSession(DEFAULT_USER_ID)
}

async function handleDeleteSession(sessionId: number) {
  try {
    await ElMessageBox.confirm('确定删除这个会话吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await chatStore.deleteSession(sessionId)
    ElMessage.success('删除成功')
  } catch {}
}

async function handleSaveUser() {
  if (!userForm.value.nickname) {
    ElMessage.warning('请输入昵称')
    return
  }
  await chatStore.saveUser(userForm.value)
  userInputVisible.value = false
  ElMessage.success('保存成功')
  await chatStore.loadSessions(DEFAULT_USER_ID)
}

function handleSwitchSession(session: any) {
  chatStore.switchSession(session)
}

function formatTime(dateStr: string) {
  const date = new Date(dateStr)
  return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

function handleKeydown(e: KeyboardEvent) {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    handleSendMessage()
  }
}

function toggleTheme() {
  isDark.value = !isDark.value
  document.documentElement.setAttribute('data-theme', isDark.value ? 'dark' : 'light')
  localStorage.setItem('theme', isDark.value ? 'dark' : 'light')
}
</script>

<template>
  <div class="chat-container">
    <!-- 左侧会话栏 -->
    <aside class="session-sidebar">
      <div class="sidebar-header">
        <div class="logo-area">
          <div class="logo-icon">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
              <path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </div>
          <span class="logo-text">AI Studio</span>
        </div>
        <button class="btn-new-chat" @click="handleNewSession" title="新建会话">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
            <path d="M12 5v14M5 12h14" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          </svg>
        </button>
      </div>

      <div class="session-list">
        <div
          v-for="session in chatStore.sessions"
          :key="session.id"
          class="session-item"
          :class="{ active: chatStore.currentSession?.id === session.id }"
          @click="handleSwitchSession(session)"
        >
          <div class="session-icon">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
              <path d="M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </div>
          <div class="session-info">
            <div class="session-title">{{ session.title }}</div>
            <div class="session-time">{{ formatTime(session.updatedAt) }}</div>
          </div>
          <button class="btn-delete" @click.stop="handleDeleteSession(session.id)" title="删除">
            <svg width="12" height="12" viewBox="0 0 24 24" fill="none">
              <path d="M18 6L6 18M6 6l12 12" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
          </button>
        </div>

        <div v-if="chatStore.sessions.length === 0" class="empty-sessions">
          <svg width="32" height="32" viewBox="0 0 24 24" fill="none" opacity="0.3">
            <path d="M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          <span>暂无会话</span>
        </div>
      </div>

      <div class="sidebar-footer">
        <button class="nav-btn" @click="router.push('/profile')">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
            <path d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2M12 11a4 4 0 100-8 4 4 0 000 8z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          <span>个人信息</span>
        </button>
        <button class="nav-btn" @click="router.push('/ai-config')">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
            <path d="M12 15a3 3 0 100-6 3 3 0 000 6z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M19.4 15a1.65 1.65 0 00.33 1.82l.06.06a2 2 0 010 2.83 2 2 0 01-2.83 0l-.06-.06a1.65 1.65 0 00-1.82-.33 1.65 1.65 0 00-1 1.51V21a2 2 0 01-2 2 2 2 0 01-2-2v-.09A1.65 1.65 0 009 19.4a1.65 1.65 0 00-1.82.33l-.06.06a2 2 0 01-2.83 0 2 2 0 010-2.83l.06-.06A1.65 1.65 0 004.68 15a1.65 1.65 0 00-1.51-1H3a2 2 0 01-2-2 2 2 0 012-2h.09A1.65 1.65 0 004.6 9a1.65 1.65 0 00-.33-1.82l-.06-.06a2 2 0 010-2.83 2 2 0 012.83 0l.06.06A1.65 1.65 0 009 4.68a1.65 1.65 0 001-1.51V3a2 2 0 012-2 2 2 0 012 2v.09a1.65 1.65 0 001 1.51 1.65 1.65 0 001.82-.33l.06-.06a2 2 0 012.83 0 2 2 0 010 2.83l-.06.06A1.65 1.65 0 0019.4 9a1.65 1.65 0 001.51 1H21a2 2 0 012 2 2 2 0 01-2 2h-.09a1.65 1.65 0 00-1.51 1z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          <span>AI 人设</span>
        </button>
        <button class="nav-btn" @click="router.push('/ai-model-config')">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
            <path d="M22 12h-4l-3 9L9 3l-3 9H2" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          <span>模型配置</span>
        </button>
        <div class="nav-divider"></div>
        <button class="nav-btn" @click="toggleTheme">
          <svg v-if="isDark" width="16" height="16" viewBox="0 0 24 24" fill="none">
            <circle cx="12" cy="12" r="5" stroke="currentColor" stroke-width="2"/>
            <line x1="12" y1="1" x2="12" y2="3" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            <line x1="12" y1="21" x2="12" y2="23" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            <line x1="4.22" y1="4.22" x2="5.64" y2="5.64" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            <line x1="18.36" y1="18.36" x2="19.78" y2="19.78" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            <line x1="1" y1="12" x2="3" y2="12" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            <line x1="21" y1="12" x2="23" y2="12" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            <line x1="4.22" y1="19.78" x2="5.64" y2="18.36" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            <line x1="18.36" y1="5.64" x2="19.78" y2="4.22" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          </svg>
          <svg v-else width="16" height="16" viewBox="0 0 24 24" fill="none">
            <path d="M21 12.79A9 9 0 1111.21 3 7 7 0 0021 12.79z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          <span>{{ isDark ? '亮色模式' : '暗色模式' }}</span>
        </button>
      </div>
    </aside>

    <!-- 右侧对话区 -->
    <main class="chat-main">
      <header class="chat-header">
        <div class="header-left">
          <h1>{{ chatStore.currentSession?.title || 'AI 助手' }}</h1>
          <div class="header-badge" v-if="chatStore.streaming">
            <span class="pulse-dot"></span>
            <span>思考中...</span>
          </div>
        </div>
        <div class="header-right">
          <div class="model-selector">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
              <path d="M22 12h-4l-3 9L9 3l-3 9H2" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <select v-model="selectedModelId" class="model-select">
              <option v-for="model in enabledModels" :key="model.id" :value="model.id">
                {{ model.modelName }}
              </option>
            </select>
          </div>
        </div>
      </header>

      <div class="message-list" ref="messageListRef">
        <!-- 空状态 -->
        <div v-if="chatStore.messages.length === 0" class="empty-state">
          <div class="empty-icon">
            <svg width="48" height="48" viewBox="0 0 24 24" fill="none">
              <path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </div>
          <h2>开始新的对话</h2>
          <p>输入你的问题，AI 将为你提供个性化帮助</p>
        </div>

        <!-- 消息列表 -->
        <div
          v-for="msg in chatStore.messages"
          :key="msg.id"
          class="message-item"
          :class="msg.role"
        >
          <div class="message-avatar">
            <div v-if="msg.role === 'user'" class="avatar avatar-user">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
                <path d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2M12 11a4 4 0 100-8 4 4 0 000 8z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </div>
            <div v-else class="avatar avatar-ai">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
                <path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </div>
          </div>
          <div class="message-content">
            <div class="message-text" v-html="msg.content.replace(/\n/g, '<br>')" />
            <div class="message-time">{{ formatTime(msg.createdAt) }}</div>
          </div>
        </div>
      </div>

      <div class="input-area">
        <div class="input-wrapper">
          <textarea
            v-model="messageInput"
            placeholder="输入消息... (Enter 发送，Shift+Enter 换行)"
            :disabled="chatStore.streaming"
            @keydown="handleKeydown"
            rows="1"
            class="message-input"
          />
          <button
            class="btn-send"
            :disabled="!messageInput.trim() || chatStore.streaming"
            @click="handleSendMessage"
          >
            <svg v-if="!chatStore.streaming" width="18" height="18" viewBox="0 0 24 24" fill="none">
              <path d="M22 2L11 13M22 2l-7 20-4-9-9-4 20-7z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <div v-else class="spinner"></div>
          </button>
        </div>
        <div class="input-hint">
          <span>Shift + Enter 换行</span>
        </div>
      </div>
    </main>

    <!-- 用户信息弹窗 -->
    <el-dialog v-model="userInputVisible" title="完善个人信息" width="500px" :closable="false" :close-on-click-modal="false">
      <el-form :model="userForm" label-width="80px">
        <el-form-item label="昵称" required>
          <el-input v-model="userForm.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="年龄">
          <el-input-number v-model="userForm.age" :min="1" :max="150" />
        </el-form-item>
        <el-form-item label="性别">
          <el-select v-model="userForm.gender" placeholder="请选择">
            <el-option label="男" value="男" />
            <el-option label="女" value="女" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="职业">
          <el-input v-model="userForm.occupation" placeholder="请输入职业" />
        </el-form-item>
        <el-form-item label="兴趣爱好">
          <el-input v-model="userForm.interests" type="textarea" :rows="2" placeholder="请输入兴趣爱好" />
        </el-form-item>
        <el-form-item label="性格特点">
          <el-input v-model="userForm.personality" type="textarea" :rows="2" placeholder="请输入性格特点" />
        </el-form-item>
        <el-form-item label="其他信息">
          <el-input v-model="userForm.extraInfo" type="textarea" :rows="2" placeholder="其他想让 AI 了解的信息" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="handleSaveUser">保存并开始</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.chat-container {
  display: flex;
  height: 100vh;
  overflow: hidden;
}

/* ===== 侧边栏 ===== */
.session-sidebar {
  width: 280px;
  background: var(--bg-secondary);
  border-right: 1px solid var(--border-color);
  display: flex;
  flex-direction: column;
  backdrop-filter: blur(20px);
}

.sidebar-header {
  padding: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid var(--border-color);
}

.logo-area {
  display: flex;
  align-items: center;
  gap: 10px;
}

.logo-icon {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--accent-gradient);
  border-radius: var(--radius-sm);
  color: #000;
}

.logo-text {
  font-size: 16px;
  font-weight: 600;
  background: var(--accent-gradient);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.btn-new-chat {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-tertiary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
  color: var(--text-secondary);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.btn-new-chat:hover {
  background: var(--bg-hover);
  color: var(--accent-primary);
  border-color: var(--accent-primary);
}

.session-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.session-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px;
  border-radius: var(--radius-md);
  cursor: pointer;
  margin-bottom: 2px;
  position: relative;
  transition: all var(--transition-fast);
}

.session-item:hover {
  background: var(--bg-hover);
}

.session-item.active {
  background: rgba(0, 212, 255, 0.08);
  border: 1px solid rgba(0, 212, 255, 0.15);
}

.session-icon {
  color: var(--text-tertiary);
  flex-shrink: 0;
}

.session-item.active .session-icon {
  color: var(--accent-primary);
}

.session-info {
  flex: 1;
  min-width: 0;
}

.session-title {
  font-size: 13px;
  color: var(--text-primary);
  margin-bottom: 2px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.session-time {
  font-size: 11px;
  color: var(--text-tertiary);
}

.btn-delete {
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  border: none;
  border-radius: var(--radius-sm);
  color: var(--text-tertiary);
  cursor: pointer;
  opacity: 0;
  transition: all var(--transition-fast);
}

.session-item:hover .btn-delete {
  opacity: 1;
}

.btn-delete:hover {
  background: rgba(239, 68, 68, 0.1);
  color: #ef4444;
}

.empty-sessions {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 120px;
  color: var(--text-tertiary);
  font-size: 13px;
  gap: 8px;
}

.sidebar-footer {
  padding: 12px;
  border-top: 1px solid var(--border-color);
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.nav-divider {
  height: 1px;
  background: var(--border-color);
  margin: 4px 0;
}

.nav-btn {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  background: transparent;
  border: none;
  border-radius: var(--radius-sm);
  color: var(--text-secondary);
  font-size: 13px;
  font-family: var(--font-sans);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.nav-btn:hover {
  background: var(--bg-hover);
  color: var(--text-primary);
}

/* ===== 主对话区 ===== */
.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.chat-header {
  padding: 16px 24px;
  background: var(--bg-secondary);
  border-bottom: 1px solid var(--border-color);
  display: flex;
  justify-content: space-between;
  align-items: center;
  backdrop-filter: blur(20px);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-left h1 {
  font-size: 16px;
  font-weight: 500;
  color: var(--text-primary);
}

.header-badge {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 10px;
  background: rgba(0, 212, 255, 0.1);
  border: 1px solid rgba(0, 212, 255, 0.2);
  border-radius: 20px;
  font-size: 12px;
  color: var(--accent-primary);
}

.pulse-dot {
  width: 6px;
  height: 6px;
  background: var(--accent-primary);
  border-radius: 50%;
  animation: pulse 1.5s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.5; transform: scale(0.8); }
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.model-selector {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 12px;
  background: var(--bg-tertiary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
  color: var(--text-secondary);
}

.model-select {
  background: transparent;
  border: none;
  color: var(--text-primary);
  font-size: 13px;
  font-family: var(--font-sans);
  cursor: pointer;
  outline: none;
}

.model-select option {
  background: var(--bg-secondary);
  color: var(--text-primary);
}

/* ===== 消息列表 ===== */
.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
}

.empty-state {
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: var(--text-tertiary);
}

.empty-icon {
  width: 80px;
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-tertiary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-xl);
  margin-bottom: 20px;
  color: var(--text-tertiary);
}

.empty-state h2 {
  font-size: 18px;
  font-weight: 500;
  color: var(--text-secondary);
  margin-bottom: 8px;
}

.empty-state p {
  font-size: 14px;
  color: var(--text-tertiary);
}

.message-item {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
  max-width: 800px;
  margin-left: auto;
  margin-right: auto;
}

.message-item.user {
  flex-direction: row-reverse;
}

.message-avatar {
  flex-shrink: 0;
}

.avatar {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-sm);
}

.avatar-user {
  background: var(--bg-tertiary);
  border: 1px solid var(--border-color);
  color: var(--text-secondary);
}

.avatar-ai {
  background: var(--accent-gradient);
  color: #000;
}

.message-content {
  max-width: 70%;
  min-width: 0;
}

.message-item.user .message-content {
  text-align: right;
}

.message-text {
  padding: 12px 16px;
  border-radius: var(--radius-lg);
  font-size: 14px;
  line-height: 1.7;
  word-break: break-word;
}

.message-item.assistant .message-text {
  background: var(--bg-tertiary);
  border: 1px solid var(--border-color);
  color: var(--text-primary);
  border-top-left-radius: 4px;
}

.message-item.user .message-text {
  background: var(--accent-gradient);
  color: #000;
  border-top-right-radius: 4px;
}

.message-time {
  font-size: 11px;
  color: var(--text-tertiary);
  margin-top: 6px;
}

/* ===== 输入区 ===== */
.input-area {
  padding: 16px 24px 12px;
  background: var(--bg-secondary);
  border-top: 1px solid var(--border-color);
}

.input-wrapper {
  display: flex;
  align-items: flex-end;
  gap: 8px;
  padding: 8px 8px 8px 16px;
  background: var(--bg-tertiary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  transition: border-color var(--transition-fast);
}

.input-wrapper:focus-within {
  border-color: var(--accent-primary);
  box-shadow: 0 0 0 2px rgba(0, 212, 255, 0.1);
}

.message-input {
  flex: 1;
  background: transparent;
  border: none;
  color: var(--text-primary);
  font-size: 14px;
  font-family: var(--font-sans);
  line-height: 1.6;
  resize: none;
  outline: none;
  max-height: 120px;
}

.message-input::placeholder {
  color: var(--text-tertiary);
}

.btn-send {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--accent-gradient);
  border: none;
  border-radius: var(--radius-sm);
  color: #000;
  cursor: pointer;
  transition: all var(--transition-fast);
  flex-shrink: 0;
}

.btn-send:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.btn-send:not(:disabled):hover {
  transform: scale(1.05);
  box-shadow: var(--shadow-glow);
}

.spinner {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(0, 0, 0, 0.3);
  border-top-color: #000;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.input-hint {
  text-align: center;
  padding-top: 8px;
  font-size: 11px;
  color: var(--text-tertiary);
}
</style>
