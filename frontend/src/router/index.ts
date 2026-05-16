import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      redirect: '/chat'
    },
    {
      path: '/chat',
      name: 'Chat',
      component: () => import('../views/ChatView.vue')
    },
    {
      path: '/profile',
      name: 'Profile',
      component: () => import('../views/UserProfile.vue')
    },
    {
      path: '/ai-config',
      name: 'AiConfig',
      component: () => import('../views/AiConfigView.vue')
    },
    {
      path: '/ai-model-config',
      name: 'AiModelConfig',
      component: () => import('../views/AiModelConfigView.vue')
    }
  ]
})

export default router
