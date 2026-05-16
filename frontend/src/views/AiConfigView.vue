<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const router = useRouter()

const personality = ref({
  role: '',
  personality: '',
  speaking_style: '',
  greeting: ''
})

const promptTemplate = ref('')
const loading = ref(false)

onMounted(async () => {
  await loadAllConfigs()
})

async function loadAllConfigs() {
  loading.value = true
  try {
    const res = await axios.get('/api/ai-config')
    const data = res.data.data

    if (data.personality) {
      for (const item of data.personality) {
        const key = item.configKey.replace('ai.', '')
        if (key in personality.value) {
          (personality.value as any)[key] = item.configValue
        }
      }
    }

    if (data.prompt) {
      const templateItem = data.prompt.find((item: any) => item.configKey === 'ai.system_prompt_template')
      if (templateItem) {
        promptTemplate.value = templateItem.configValue
      }
    }
  } catch (error) {
    ElMessage.error('加载配置失败')
  } finally {
    loading.value = false
  }
}

async function savePersonality() {
  try {
    const configs = {
      'ai.role': personality.value.role,
      'ai.personality': personality.value.personality,
      'ai.speaking_style': personality.value.speaking_style,
      'ai.greeting': personality.value.greeting
    }
    await axios.put('/api/ai-config', configs)
    ElMessage.success('人设配置保存成功')
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

async function savePromptTemplate() {
  try {
    await axios.put('/api/ai-config/ai.system_prompt_template', {
      value: promptTemplate.value
    })
    ElMessage.success('提示词模板保存成功')
  } catch (error) {
    ElMessage.error('保存失败')
  }
}
</script>

<template>
  <div class="config-container" v-loading="loading">
    <header class="page-header">
      <button class="btn-back" @click="router.push('/chat')">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
          <path d="M19 12H5M12 19l-7-7 7-7" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        <span>返回</span>
      </button>
      <h1>AI 人设配置</h1>
    </header>

    <!-- AI 人设配置 -->
    <div class="config-card">
      <div class="card-header">
        <div class="card-title">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
            <path d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2M12 11a4 4 0 100-8 4 4 0 000 8z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          <h2>角色设定</h2>
        </div>
        <button class="btn-save" @click="savePersonality">保存</button>
      </div>

      <div class="card-body">
        <div class="form-group">
          <label>AI 角色</label>
          <input v-model="personality.role" placeholder="例如：专业顾问、朋友、老师、心理咨询师" class="form-input" />
          <span class="form-tip">设定 AI 扮演的角色身份</span>
        </div>

        <div class="form-group">
          <label>性格特点</label>
          <textarea v-model="personality.personality" placeholder="例如：友好、专业、耐心、幽默、严谨" class="form-textarea" rows="3"></textarea>
          <span class="form-tip">描述 AI 的性格特征</span>
        </div>

        <div class="form-group">
          <label>说话风格</label>
          <textarea v-model="personality.speaking_style" placeholder="例如：简洁明了、通俗易懂、善用比喻、严谨专业" class="form-textarea" rows="2"></textarea>
          <span class="form-tip">描述 AI 的语言表达方式</span>
        </div>

        <div class="form-group">
          <label>开场白</label>
          <textarea v-model="personality.greeting" placeholder="AI 在新对话开始时的问候语" class="form-textarea" rows="3"></textarea>
          <span class="form-tip">新会话开始时 AI 的第一句话</span>
        </div>
      </div>
    </div>

    <!-- 系统提示词配置 -->
    <div class="config-card">
      <div class="card-header">
        <div class="card-title">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
            <path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <polyline points="14 2 14 8 20 8" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <line x1="16" y1="13" x2="8" y2="13" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <line x1="16" y1="17" x2="8" y2="17" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <polyline points="10 9 9 9 8 9" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          <h2>系统提示词模板</h2>
        </div>
        <button class="btn-save" @click="savePromptTemplate">保存</button>
      </div>

      <div class="card-body">
        <div class="form-group">
          <label>提示词模板</label>
          <textarea v-model="promptTemplate" placeholder="输入系统提示词模板" class="form-textarea" rows="8"></textarea>
        </div>

        <div class="variables-panel">
          <h4>可用变量</h4>
          <div class="variables-grid">
            <div class="variable-item">
              <code>{role}</code>
              <span>AI 角色</span>
            </div>
            <div class="variable-item">
              <code>{personality}</code>
              <span>性格特点</span>
            </div>
            <div class="variable-item">
              <code>{speaking_style}</code>
              <span>说话风格</span>
            </div>
            <div class="variable-item">
              <code>{user_info}</code>
              <span>用户个人信息</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.config-container {
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

.config-card {
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

.form-group {
  margin-bottom: 20px;
}

.form-group:last-child {
  margin-bottom: 0;
}

.form-group label {
  display: block;
  font-size: 13px;
  font-weight: 500;
  color: var(--text-secondary);
  margin-bottom: 8px;
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

.form-tip {
  display: block;
  font-size: 12px;
  color: var(--text-tertiary);
  margin-top: 6px;
}

.variables-panel {
  margin-top: 20px;
  padding: 16px;
  background: var(--bg-tertiary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
}

.variables-panel h4 {
  font-size: 13px;
  font-weight: 500;
  color: var(--text-secondary);
  margin-bottom: 12px;
}

.variables-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
}

.variable-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.variable-item code {
  padding: 3px 8px;
  background: rgba(0, 212, 255, 0.1);
  border: 1px solid rgba(0, 212, 255, 0.2);
  border-radius: 4px;
  font-family: var(--font-mono);
  font-size: 12px;
  color: var(--accent-primary);
}

.variable-item span {
  font-size: 12px;
  color: var(--text-tertiary);
}
</style>
