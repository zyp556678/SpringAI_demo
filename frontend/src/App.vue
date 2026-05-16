<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { RouterView } from 'vue-router'

const isDark = ref(true)

onMounted(() => {
  const saved = localStorage.getItem('theme')
  if (saved) {
    isDark.value = saved === 'dark'
  } else {
    isDark.value = window.matchMedia('(prefers-color-scheme: dark)').matches
  }
  applyTheme()
})

watch(isDark, () => {
  applyTheme()
  localStorage.setItem('theme', isDark.value ? 'dark' : 'light')
})

function applyTheme() {
  document.documentElement.setAttribute('data-theme', isDark.value ? 'dark' : 'light')
}

function toggleTheme() {
  isDark.value = !isDark.value
}

// 提供给子组件
defineExpose({ isDark, toggleTheme })
</script>

<template>
  <div class="app-container">
    <RouterView />
  </div>
</template>

<style>
@import url('https://fonts.googleapis.com/css2?family=Noto+Sans+SC:wght@300;400;500;700&family=JetBrains+Mono:wght@400;500&display=swap');

/* ===== 暗黑主题 ===== */
[data-theme="dark"] {
  --bg-primary: #0a0a0a;
  --bg-secondary: #111111;
  --bg-tertiary: #1a1a1a;
  --bg-elevated: #222222;
  --bg-hover: #2a2a2a;

  --accent-primary: #00d4ff;
  --accent-secondary: #a855f7;
  --accent-gradient: linear-gradient(135deg, #00d4ff, #a855f7);

  --text-primary: #f0f0f0;
  --text-secondary: #a0a0a0;
  --text-tertiary: #666666;

  --border-color: rgba(255, 255, 255, 0.06);
  --border-hover: rgba(255, 255, 255, 0.12);

  --glass-bg: rgba(255, 255, 255, 0.03);
  --glass-border: rgba(255, 255, 255, 0.08);

  --shadow-sm: 0 2px 8px rgba(0, 0, 0, 0.3);
  --shadow-md: 0 4px 16px rgba(0, 0, 0, 0.4);
  --shadow-glow: 0 0 20px rgba(0, 212, 255, 0.15);

  --input-bg: #1a1a1a;
  --card-bg: #111111;
  --sidebar-bg: #111111;
  --header-bg: #111111;
}

/* ===== 亮色主题 ===== */
[data-theme="light"] {
  --bg-primary: #f8f9fa;
  --bg-secondary: #ffffff;
  --bg-tertiary: #f1f3f5;
  --bg-elevated: #ffffff;
  --bg-hover: #e9ecef;

  --accent-primary: #0891b2;
  --accent-secondary: #7c3aed;
  --accent-gradient: linear-gradient(135deg, #0891b2, #7c3aed);

  --text-primary: #1a1a2e;
  --text-secondary: #495057;
  --text-tertiary: #adb5bd;

  --border-color: rgba(0, 0, 0, 0.08);
  --border-hover: rgba(0, 0, 0, 0.15);

  --glass-bg: rgba(255, 255, 255, 0.8);
  --glass-border: rgba(0, 0, 0, 0.08);

  --shadow-sm: 0 2px 8px rgba(0, 0, 0, 0.06);
  --shadow-md: 0 4px 16px rgba(0, 0, 0, 0.08);
  --shadow-glow: 0 0 20px rgba(8, 145, 178, 0.1);

  --input-bg: #f1f3f5;
  --card-bg: #ffffff;
  --sidebar-bg: #ffffff;
  --header-bg: #ffffff;
}

:root {
  --radius-sm: 8px;
  --radius-md: 12px;
  --radius-lg: 16px;
  --radius-xl: 24px;

  --font-sans: 'Noto Sans SC', -apple-system, BlinkMacSystemFont, sans-serif;
  --font-mono: 'JetBrains Mono', 'Fira Code', monospace;

  --transition-fast: 0.15s ease;
  --transition-normal: 0.25s ease;
  --transition-slow: 0.4s ease;
}

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body {
  height: 100%;
  font-family: var(--font-sans);
  background-color: var(--bg-primary);
  color: var(--text-primary);
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  transition: background-color var(--transition-normal), color var(--transition-normal);
}

#app {
  height: 100%;
}

.app-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

[data-theme="dark"] .app-container {
  background:
    radial-gradient(ellipse at 20% 50%, rgba(0, 212, 255, 0.03) 0%, transparent 50%),
    radial-gradient(ellipse at 80% 20%, rgba(168, 85, 247, 0.03) 0%, transparent 50%),
    var(--bg-primary);
}

[data-theme="light"] .app-container {
  background: var(--bg-primary);
}

/* 滚动条 */
::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

::-webkit-scrollbar-track {
  background: transparent;
}

::-webkit-scrollbar-thumb {
  background: var(--bg-hover);
  border-radius: 3px;
}

::-webkit-scrollbar-thumb:hover {
  background: var(--text-tertiary);
}

/* Element Plus 覆盖 */
.el-button--primary {
  --el-button-bg-color: var(--accent-primary);
  --el-button-border-color: var(--accent-primary);
  --el-button-hover-bg-color: var(--accent-primary);
  --el-button-hover-border-color: var(--accent-primary);
  --el-button-active-bg-color: var(--accent-primary);
  --el-button-active-border-color: var(--accent-primary);
}

.el-input__wrapper,
.el-textarea__inner {
  background-color: var(--input-bg) !important;
  border-color: var(--border-color) !important;
  box-shadow: none !important;
  color: var(--text-primary) !important;
}

.el-input__wrapper:hover,
.el-textarea__inner:hover {
  border-color: var(--border-hover) !important;
}

.el-input__wrapper.is-focus,
.el-textarea__inner:focus {
  border-color: var(--accent-primary) !important;
  box-shadow: 0 0 0 2px rgba(0, 212, 255, 0.1) !important;
}

.el-input__inner {
  color: var(--text-primary) !important;
}

.el-input__inner::placeholder {
  color: var(--text-tertiary) !important;
}

.el-dialog {
  background-color: var(--bg-secondary) !important;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg) !important;
}

.el-dialog__title {
  color: var(--text-primary) !important;
}

.el-dialog__body {
  color: var(--text-secondary) !important;
}

.el-form-item__label {
  color: var(--text-secondary) !important;
}

.el-select__wrapper {
  background-color: var(--input-bg) !important;
  border-color: var(--border-color) !important;
  box-shadow: none !important;
}

.el-select__placeholder {
  color: var(--text-tertiary) !important;
}

.el-select-dropdown {
  background-color: var(--bg-secondary) !important;
  border-color: var(--border-color) !important;
}

.el-select-dropdown__item {
  color: var(--text-secondary) !important;
}

.el-select-dropdown__item.is-hovering {
  background-color: var(--bg-hover) !important;
}

.el-select-dropdown__item.is-selected {
  color: var(--accent-primary) !important;
}

.el-table {
  --el-table-bg-color: var(--bg-secondary);
  --el-table-tr-bg-color: var(--bg-secondary);
  --el-table-header-bg-color: var(--bg-tertiary);
  --el-table-row-hover-bg-color: var(--bg-hover);
  --el-table-border-color: var(--border-color);
  --el-table-text-color: var(--text-secondary);
  --el-table-header-text-color: var(--text-primary);
}

.el-tag {
  border-color: var(--border-color);
}

.el-empty__description p {
  color: var(--text-tertiary) !important;
}

.el-loading-mask {
  background-color: var(--bg-primary) !important;
  opacity: 0.8;
}

.el-slider__runway {
  background-color: var(--bg-hover);
}

.el-slider__bar {
  background-color: var(--accent-primary);
}

.el-slider__button {
  border-color: var(--accent-primary);
}

.el-switch.is-checked .el-switch__core {
  background-color: var(--accent-primary);
  border-color: var(--accent-primary);
}

.el-card {
  --el-card-bg-color: var(--bg-secondary);
  --el-card-border-color: var(--border-color);
}

.el-descriptions {
  --el-descriptions-item-bordered-label-background: var(--bg-tertiary);
}
</style>
