<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useChatStore } from '../stores/chat'
import { ElMessage } from 'element-plus'

const router = useRouter()
const chatStore = useChatStore()

const DEFAULT_USER_ID = 1

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

onMounted(async () => {
  try {
    await chatStore.loadUser(DEFAULT_USER_ID)
    if (chatStore.currentUser) {
      Object.assign(userForm.value, chatStore.currentUser)
    }
  } catch {}
})

async function handleSave() {
  if (!userForm.value.nickname) {
    ElMessage.warning('请输入昵称')
    return
  }
  await chatStore.saveUser(userForm.value)
  ElMessage.success('保存成功')
}
</script>

<template>
  <div class="profile-container">
    <header class="page-header">
      <button class="btn-back" @click="router.push('/chat')">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
          <path d="M19 12H5M12 19l-7-7 7-7" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        <span>返回</span>
      </button>
      <h1>个人信息</h1>
    </header>

    <div class="profile-card">
      <div class="card-header">
        <div class="card-title">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
            <path d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2M12 11a4 4 0 100-8 4 4 0 000 8z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          <h2>用户资料</h2>
        </div>
        <button class="btn-save" @click="handleSave">保存</button>
      </div>

      <div class="card-body">
        <div class="form-row">
          <div class="form-group">
            <label>昵称 <span class="required">*</span></label>
            <input v-model="userForm.nickname" placeholder="请输入昵称" class="form-input" />
          </div>
          <div class="form-group">
            <label>年龄</label>
            <input v-model.number="userForm.age" type="number" min="1" max="150" placeholder="请输入年龄" class="form-input" />
          </div>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label>性别</label>
            <select v-model="userForm.gender" class="form-input">
              <option value="">请选择</option>
              <option value="男">男</option>
              <option value="女">女</option>
              <option value="其他">其他</option>
            </select>
          </div>
          <div class="form-group">
            <label>职业</label>
            <input v-model="userForm.occupation" placeholder="请输入职业" class="form-input" />
          </div>
        </div>

        <div class="form-group">
          <label>兴趣爱好</label>
          <textarea v-model="userForm.interests" placeholder="请输入兴趣爱好，例如：阅读、编程、音乐、运动..." class="form-textarea" rows="3"></textarea>
        </div>

        <div class="form-group">
          <label>性格特点</label>
          <textarea v-model="userForm.personality" placeholder="请输入性格特点，例如：内向、喜欢思考、注重细节..." class="form-textarea" rows="3"></textarea>
        </div>

        <div class="form-group">
          <label>其他信息</label>
          <textarea v-model="userForm.extraInfo" placeholder="其他想让 AI 了解的信息，例如：正在学习 Python、有 3 年工作经验..." class="form-textarea" rows="3"></textarea>
        </div>
      </div>
    </div>

    <div class="tips-card">
      <div class="tips-icon">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
          <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
          <line x1="12" y1="16" x2="12" y2="12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          <line x1="12" y1="8" x2="12.01" y2="8" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </div>
      <div class="tips-content">
        <h4>提示</h4>
        <p>完善个人信息后，AI 助手会根据你的背景、兴趣和需求提供更个性化的回答。</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.profile-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 24px;
  min-height: 100vh;
  background: var(--bg-primary);
}

.page-header {
  display: flex;
  align-items: center;
  margin-bottom: 32px;
  gap: 16px;
}

.btn-back {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  background: var(--bg-tertiary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
  color: var(--text-secondary);
  font-size: 13px;
  font-family: var(--font-sans);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.btn-back:hover {
  background: var(--bg-hover);
  color: var(--text-primary);
}

.page-header h1 {
  font-size: 20px;
  font-weight: 600;
  background: var(--accent-gradient);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.profile-card {
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  margin-bottom: 20px;
  overflow: hidden;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid var(--border-color);
}

.card-title {
  display: flex;
  align-items: center;
  gap: 10px;
  color: var(--text-primary);
}

.card-title h2 {
  font-size: 15px;
  font-weight: 500;
}

.btn-save {
  padding: 6px 16px;
  background: var(--accent-gradient);
  border: none;
  border-radius: var(--radius-sm);
  color: #000;
  font-size: 13px;
  font-weight: 500;
  font-family: var(--font-sans);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.btn-save:hover {
  transform: translateY(-1px);
  box-shadow: var(--shadow-glow);
}

.card-body {
  padding: 20px;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-bottom: 16px;
}

.form-group {
  margin-bottom: 16px;
}

.form-row .form-group {
  margin-bottom: 0;
}

.form-group label {
  display: block;
  font-size: 13px;
  font-weight: 500;
  color: var(--text-secondary);
  margin-bottom: 8px;
}

.required {
  color: #ef4444;
}

.form-input,
.form-textarea {
  width: 100%;
  padding: 10px 14px;
  background: var(--bg-tertiary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
  color: var(--text-primary);
  font-size: 14px;
  font-family: var(--font-sans);
  transition: all var(--transition-fast);
  outline: none;
}

.form-input:focus,
.form-textarea:focus {
  border-color: var(--accent-primary);
  box-shadow: 0 0 0 2px rgba(0, 212, 255, 0.1);
}

.form-input::placeholder,
.form-textarea::placeholder {
  color: var(--text-tertiary);
}

.form-textarea {
  resize: vertical;
  min-height: 60px;
  line-height: 1.6;
}

select.form-input {
  cursor: pointer;
  appearance: none;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 24 24' fill='none' stroke='%23666' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3E%3Cpolyline points='6 9 12 15 18 9'%3E%3C/polyline%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 12px center;
  padding-right: 32px;
}

select.form-input option {
  background: var(--bg-secondary);
  color: var(--text-primary);
}

.tips-card {
  display: flex;
  gap: 12px;
  padding: 16px;
  background: rgba(0, 212, 255, 0.05);
  border: 1px solid rgba(0, 212, 255, 0.1);
  border-radius: var(--radius-md);
}

.tips-icon {
  color: var(--accent-primary);
  flex-shrink: 0;
  margin-top: 2px;
}

.tips-content h4 {
  font-size: 13px;
  font-weight: 500;
  color: var(--accent-primary);
  margin-bottom: 4px;
}

.tips-content p {
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.6;
}
</style>
