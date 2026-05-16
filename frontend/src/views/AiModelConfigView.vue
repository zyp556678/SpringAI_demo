<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../api'

const router = useRouter()

interface AiModelConfig {
  id?: number
  modelName: string
  provider: string
  apiKey: string
  baseUrl: string
  modelId: string
  temperature: number
  maxTokens: number
  isDefault: boolean
  isEnabled: boolean
}

const modelConfigs = ref<AiModelConfig[]>([])
const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)

const form = ref<AiModelConfig>({
  modelName: '',
  provider: 'openai',
  apiKey: '',
  baseUrl: '',
  modelId: '',
  temperature: 0.7,
  maxTokens: 2000,
  isDefault: false,
  isEnabled: true
})

const providerOptions = [
  { label: 'OpenAI 兼容 (Deepseek/OpenAI/智谱/通义/Moonshot)', value: 'openai' },
  { label: 'Anthropic (Claude)', value: 'anthropic' }
]

onMounted(() => { loadModelConfigs() })

async function loadModelConfigs() {
  loading.value = true
  try {
    const res = await api.get('/ai-model-config')
    modelConfigs.value = res.data
  } catch (error) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

function handleAdd() {
  isEdit.value = false
  form.value = {
    modelName: '',
    provider: 'openai',
    apiKey: '',
    baseUrl: 'https://api.deepseek.com',
    modelId: 'deepseek-chat',
    temperature: 0.7,
    maxTokens: 2000,
    isDefault: false,
    isEnabled: true
  }
  dialogVisible.value = true
}

function handleEdit(row: AiModelConfig) {
  isEdit.value = true
  form.value = { ...row }
  dialogVisible.value = true
}

async function handleSave() {
  if (!form.value.modelName || !form.value.modelId) {
    ElMessage.warning('请填写模型名称和模型 ID')
    return
  }
  try {
    if (isEdit.value) {
      await api.put(`/ai-model-config/${form.value.id}`, form.value)
      ElMessage.success('更新成功')
    } else {
      await api.post('/ai-model-config', form.value)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    await loadModelConfigs()
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

async function handleDelete(id: number) {
  try {
    await ElMessageBox.confirm('确定删除此模型配置？', '确认')
    await api.delete(`/ai-model-config/${id}`)
    ElMessage.success('删除成功')
    await loadModelConfigs()
  } catch {}
}

async function handleSetDefault(id: number) {
  try {
    await api.post(`/ai-model-config/${id}/set-default`)
    ElMessage.success('已设为默认模型')
    await loadModelConfigs()
  } catch (error) {
    ElMessage.error('设置失败')
  }
}

async function handleTest(id: number) {
  try {
    const res: any = await api.post(`/ai-model-config/${id}/test`)
    if (res.code === 200) {
      ElMessage.success('连接测试通过')
    } else {
      ElMessage.error(res.message || '连接测试失败')
    }
  } catch (error) {
    ElMessage.error('连接测试失败')
  }
}

function handleProviderChange() {
  if (form.value.provider === 'anthropic') {
    form.value.baseUrl = 'https://api.anthropic.com'
  }
}
</script>

<template>
  <div class="model-config-container">
    <header class="page-header">
      <button class="btn-back" @click="router.push('/chat')">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
          <path d="M19 12H5M12 19l-7-7 7-7" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        <span>返回</span>
      </button>
      <h1>模型配置</h1>
      <button class="btn-add" @click="handleAdd">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
          <path d="M12 5v14M5 12h14" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
        </svg>
        <span>添加模型</span>
      </button>
    </header>

    <div class="model-grid" v-loading="loading">
      <div
        v-for="model in modelConfigs"
        :key="model.id"
        class="model-card"
        :class="{ default: model.isDefault, disabled: !model.isEnabled }"
      >
        <div class="card-header">
          <div class="card-title">
            <h3>{{ model.modelName }}</h3>
            <div class="card-badges">
              <span v-if="model.isDefault" class="badge badge-default">默认</span>
              <span v-if="model.isEnabled" class="badge badge-enabled">启用</span>
              <span v-else class="badge badge-disabled">禁用</span>
            </div>
          </div>
          <span class="provider-tag" :class="model.provider">{{ model.provider }}</span>
        </div>

        <div class="card-body">
          <div class="card-info">
            <div class="info-item">
              <span class="info-label">模型 ID</span>
              <span class="info-value mono">{{ model.modelId }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">Base URL</span>
              <span class="info-value mono url">{{ model.baseUrl }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">参数</span>
              <span class="info-value">T={{ model.temperature }}, Max={{ model.maxTokens }}</span>
            </div>
          </div>
        </div>

        <div class="card-actions">
          <button class="btn-card" @click="handleEdit(model)" title="编辑">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
              <path d="M11 4H4a2 2 0 00-2 2v14a2 2 0 002 2h14a2 2 0 002-2v-7" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M18.5 2.5a2.121 2.121 0 013 3L12 15l-4 1 1-4 9.5-9.5z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <span>编辑</span>
          </button>
          <button class="btn-card" @click="handleTest(model.id!)" title="测试">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
              <path d="M22 11.08V12a10 10 0 11-5.93-9.14" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <polyline points="22 4 12 14.01 9 11.01" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <span>测试</span>
          </button>
          <button class="btn-card" @click="handleSetDefault(model.id!)" :disabled="model.isDefault" title="默认">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
              <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <span>默认</span>
          </button>
          <button class="btn-card btn-danger" @click="handleDelete(model.id!)" title="删除">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
              <polyline points="3 6 5 6 21 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M19 6v14a2 2 0 01-2 2H7a2 2 0 01-2-2V6m3 0V4a2 2 0 012-2h4a2 2 0 012 2v2" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <span>删除</span>
          </button>
        </div>
      </div>

      <div v-if="modelConfigs.length === 0 && !loading" class="empty-state">
        <svg width="48" height="48" viewBox="0 0 24 24" fill="none">
          <path d="M22 12h-4l-3 9L9 3l-3 9H2" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        <h3>暂无模型配置</h3>
        <p>点击"添加模型"开始配置 AI 模型</p>
      </div>
    </div>

    <!-- Add/Edit Dialog -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑模型' : '添加模型'" width="600px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="模型名称" required>
          <el-input v-model="form.modelName" placeholder="例如: Deepseek Chat" />
          <div class="form-tip">在聊天列表中显示的名称</div>
        </el-form-item>

        <el-form-item label="提供商" required>
          <el-select v-model="form.provider" @change="handleProviderChange" style="width: 100%">
            <el-option v-for="opt in providerOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>
        </el-form-item>

        <el-form-item label="API Key" required>
          <el-input v-model="form.apiKey" type="password" show-password placeholder="sk-..." />
        </el-form-item>

        <el-form-item label="Base URL" required>
          <el-input v-model="form.baseUrl" placeholder="https://api.deepseek.com" />
        </el-form-item>

        <el-form-item label="模型 ID" required>
          <el-input v-model="form.modelId" placeholder="deepseek-chat" />
          <div class="form-tip">实际模型标识符，如 deepseek-chat, gpt-4, claude-3-opus</div>
        </el-form-item>

        <el-form-item label="Temperature">
          <el-slider v-model="form.temperature" :min="0" :max="2" :step="0.1" show-input />
        </el-form-item>

        <el-form-item label="Max Tokens">
          <el-input-number v-model="form.maxTokens" :min="100" :max="8000" :step="100" />
        </el-form-item>

        <el-form-item label="启用">
          <el-switch v-model="form.isEnabled" />
        </el-form-item>

        <el-form-item label="设为默认">
          <el-switch v-model="form.isDefault" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.model-config-container {
  max-width: 1200px;
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
  flex: 1;
  font-size: 20px;
  font-weight: 600;
  background: var(--accent-gradient);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.btn-add {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
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

.btn-add:hover {
  transform: translateY(-1px);
  box-shadow: var(--shadow-glow);
}

/* 模型卡片网格 */
.model-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(360px, 1fr));
  gap: 16px;
}

.model-card {
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  padding: 20px;
  transition: all var(--transition-normal);
}

.model-card:hover {
  border-color: var(--border-hover);
  box-shadow: var(--shadow-md);
}

.model-card.default {
  border-color: rgba(0, 212, 255, 0.3);
  box-shadow: 0 0 20px rgba(0, 212, 255, 0.05);
}

.model-card.disabled {
  opacity: 0.6;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.card-title h3 {
  font-size: 16px;
  font-weight: 500;
  color: var(--text-primary);
  margin-bottom: 6px;
}

.card-badges {
  display: flex;
  gap: 6px;
}

.badge {
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 500;
}

.badge-default {
  background: rgba(0, 212, 255, 0.15);
  color: var(--accent-primary);
  border: 1px solid rgba(0, 212, 255, 0.2);
}

.badge-enabled {
  background: rgba(34, 197, 94, 0.15);
  color: #22c55e;
  border: 1px solid rgba(34, 197, 94, 0.2);
}

.badge-disabled {
  background: rgba(107, 114, 128, 0.15);
  color: #6b7280;
  border: 1px solid rgba(107, 114, 128, 0.2);
}

.provider-tag {
  padding: 4px 10px;
  border-radius: var(--radius-sm);
  font-size: 11px;
  font-weight: 500;
  text-transform: uppercase;
}

.provider-tag.openai {
  background: rgba(34, 197, 94, 0.1);
  color: #22c55e;
  border: 1px solid rgba(34, 197, 94, 0.2);
}

.provider-tag.anthropic {
  background: rgba(168, 85, 247, 0.1);
  color: #a855f7;
  border: 1px solid rgba(168, 85, 247, 0.2);
}

.card-body {
  margin-bottom: 16px;
}

.card-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.info-item {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.info-label {
  font-size: 12px;
  color: var(--text-tertiary);
  min-width: 60px;
}

.info-value {
  font-size: 13px;
  color: var(--text-secondary);
}

.info-value.mono {
  font-family: var(--font-mono);
  font-size: 12px;
}

.info-value.url {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 250px;
}

.card-actions {
  display: flex;
  gap: 8px;
  padding-top: 16px;
  border-top: 1px solid var(--border-color);
}

.btn-card {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  padding: 8px;
  background: var(--bg-tertiary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
  color: var(--text-secondary);
  font-size: 12px;
  font-family: var(--font-sans);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.btn-card:hover {
  background: var(--bg-hover);
  color: var(--text-primary);
  border-color: var(--border-hover);
}

.btn-card:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.btn-card.btn-danger:hover {
  background: rgba(239, 68, 68, 0.1);
  color: #ef4444;
  border-color: rgba(239, 68, 68, 0.3);
}

.empty-state {
  grid-column: 1 / -1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px;
  color: var(--text-tertiary);
}

.empty-state svg {
  margin-bottom: 16px;
  opacity: 0.3;
}

.empty-state h3 {
  font-size: 16px;
  font-weight: 500;
  color: var(--text-secondary);
  margin-bottom: 8px;
}

.empty-state p {
  font-size: 14px;
}

.form-tip {
  font-size: 12px;
  color: var(--text-tertiary);
  margin-top: 4px;
}
</style>
