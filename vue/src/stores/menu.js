import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getMenuByRole } from '@/api/menu.js'

export const useMenuStore = defineStore('menu', () => {
  // 状态
  const menus = ref([])
  const openedMenus = ref([])
  
  // 方法
  async function loadMenus(role) {
    try {
      const res = await getMenuByRole(role || 'USER')
      if (res.code === 20000) {
        menus.value = res.data
        // 初始化展开的菜单
        openedMenus.value = menus.value.map(menu => menu.id.toString())
        return true
      } else {
        console.error('获取菜单失败:', res.message)
        return false
      }
    } catch (error) {
      console.error('获取菜单时发生错误:', error)
      return false
    }
  }
  
  function clearMenus() {
    menus.value = []
    openedMenus.value = []
  }
  
  return {
    menus,
    openedMenus,
    loadMenus,
    clearMenus
  }
})
