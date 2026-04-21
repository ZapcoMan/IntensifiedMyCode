import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
  // 状态
  const user = ref(JSON.parse(localStorage.getItem('code_user') || '{}'))
  const token = ref(localStorage.getItem('token') || '')
  const refreshToken = ref(localStorage.getItem('refreshToken') || '')
  
  // 计算属性
  const isLoggedIn = computed(function() { return !!token.value && !!user.value.id })
  const userRole = computed(function() { return user.value.role || '' })
  const userName = computed(function() { return user.value.name || user.value.username || '' })
  
  // 方法
  function setUser(userData) {
    user.value = userData
    localStorage.setItem('code_user', JSON.stringify(userData))
  }
  
  function setToken(tokenValue) {
    token.value = tokenValue
    localStorage.setItem('token', tokenValue)
  }
  
  function setRefreshToken(refreshTokenValue) {
    refreshToken.value = refreshTokenValue
    localStorage.setItem('refreshToken', refreshTokenValue)
  }
  
  function clearUser() {
    user.value = {}
    token.value = ''
    refreshToken.value = ''
    localStorage.removeItem('code_user')
    localStorage.removeItem('token')
    localStorage.removeItem('refreshToken')
  }
  
  function updateUserField(field, value) {
    if (user.value) {
      user.value[field] = value
      localStorage.setItem('code_user', JSON.stringify(user.value))
    }
  }
  
  return {
    user,
    token,
    refreshToken,
    isLoggedIn,
    userRole,
    userName,
    setUser,
    setToken,
    setRefreshToken,
    clearUser,
    updateUserField
  }
})
