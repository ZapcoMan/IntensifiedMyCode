import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useThemeStore = defineStore('theme', () => {
  // 状态
  const darkMode = ref(localStorage.getItem('theme') === 'dark')
  
  // 方法
  function toggleTheme() {
    darkMode.value = !darkMode.value
    const theme = darkMode.value ? 'dark' : 'light'
    document.documentElement.setAttribute('data-theme', theme)
    localStorage.setItem('theme', theme)
  }
  
  function setTheme(theme) {
    darkMode.value = theme === 'dark'
    document.documentElement.setAttribute('data-theme', theme)
    localStorage.setItem('theme', theme)
  }
  
  function initTheme() {
    const savedTheme = localStorage.getItem('theme') || 'light'
    setTheme(savedTheme)
  }
  
  return {
    darkMode,
    toggleTheme,
    setTheme,
    initTheme
  }
})
