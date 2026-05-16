import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
api.interceptors.request.use(
  (config) => {
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
api.interceptors.response.use(
  (response) => {
    return response.data
  },
  (error) => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 用户相关 API
export const userApi = {
  saveProfile: (data: any) => api.post('/user/profile', data),
  getProfile: (id: number) => api.get(`/user/profile/${id}`),
  listProfiles: () => api.get('/user/profiles'),
  deleteProfile: (id: number) => api.delete(`/user/profile/${id}`)
}

// 会话相关 API
export const sessionApi = {
  create: (userId: number, title?: string) => api.post('/session', { userId, title }),
  list: (userId: number) => api.get(`/session/list/${userId}`),
  get: (id: number) => api.get(`/session/${id}`),
  update: (id: number, title: string) => api.put(`/session/${id}`, { title }),
  delete: (id: number) => api.delete(`/session/${id}`)
}

// 对话相关 API
export const chatApi = {
  sendMessage: (sessionId: number, userId: number, message: string) =>
    api.post('/chat/send', { sessionId, userId, message }),
  getMessages: (sessionId: number) => api.get(`/chat/messages/${sessionId}`)
}

// SSE 流式对话
export const createChatStream = (sessionId: number, userId: number, message: string) => {
  const url = `/api/chat/stream?sessionId=${sessionId}&userId=${userId}&message=${encodeURIComponent(message)}`
  return new EventSource(url)
}

export default api
