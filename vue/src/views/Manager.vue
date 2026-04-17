<template>
  <div class="manager-layout">
    <!-- 头部区域开始 -->
    <header class="header">
      <div class="header-left">
        <div class="logo-container">
          <img class="logo" src="@/assets/imgs/logo.png" alt="Logo">
          <span class="logo-text">增强型脚手架</span>
        </div>
      </div>
      <div class="header-center">
        <p class="header-title">增强型脚手架</p>
      </div>
      <div class="header-right">
        <!-- 主题切换按钮 -->
        <el-button 
          class="theme-toggle-btn" 
          :icon="themeStore.darkMode ? Moon : Sunny" 
          circle 
          size="default"
          @click="toggleTheme"
          :title="themeStore.darkMode ? '切换到亮色主题' : '切换到暗色主题'"
        />
        
        <el-dropdown>
          <div class="user-info">
            <img v-if="userStore.user?.avatar" class="user-avatar" :src="userStore.user.avatar" alt="Avatar">
            <img v-else class="user-avatar" src="https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png" alt="Default Avatar">
            <span class="user-name">{{ userStore.user?.name }}</span>
            <el-icon class="arrow-down"><ArrowDown /></el-icon>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="router.push('/manager/person')">
                <el-icon><User /></el-icon> 个人信息
              </el-dropdown-item>
              <el-dropdown-item @click="router.push('/manager/updatePassword')">
                <el-icon><EditPen /></el-icon> 修改密码
              </el-dropdown-item>
              <el-dropdown-item @click="logout" divided>
                <el-icon><SwitchButton /></el-icon> 退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </header>
    <!--  头部区域结束 -->

    <!--  主体区域开始 -->
    <div class="main-container">
      <!--  菜单区域开始 -->
      <aside class="sidebar">
        <el-menu 
          router 
          :default-openeds="menuStore.openedMenus" 
          :default-active="router.currentRoute.value.path" 
          class="side-menu"
          :collapse="isCollapse"
        >
          <template v-for="menu in menuStore.menus" :key="menu.id">
            <el-menu-item v-if="!menu.children || menu.children.length === 0" :index="menu.path">
              <el-icon v-if="menu.icon">
                <component :is="getIconComponent(menu.icon)" />
              </el-icon>
              <span>{{ menu.name }}</span>
            </el-menu-item>
            <el-sub-menu v-else :index="menu.id.toString()">
              <template #title>
                <el-icon v-if="menu.icon">
                  <component :is="getIconComponent(menu.icon)" />
                </el-icon>
                <span>{{ menu.name }}</span>
              </template>
              <template v-for="child in menu.children" :key="child.id">
                <el-menu-item :index="child.path">
                  <el-icon v-if="child.icon">
                    <component :is="getIconComponent(child.icon)" />
                  </el-icon>
                  <span>{{ child.name }}</span>
                </el-menu-item>
              </template>
            </el-sub-menu>
          </template>
        </el-menu>
      </aside>
      <!--  菜单区域结束 -->

      <!--  内容区域开始 -->
      <main class="main-content">
        <div class="content-wrapper">
          <RouterView @updateUser="updateUser"/>
        </div>
      </main>
      <!--  内容区域结束 -->

    </div>
    <!--  主体区域结束 -->

  </div>
</template>

<script setup>
import router from "@/router/index.js";
import { onMounted, computed } from "vue";
import { useUserStore } from '@/stores/user'
import { useThemeStore } from '@/stores/theme'
import { useMenuStore } from '@/stores/menu'
import { House, User, EditPen, SwitchButton, ArrowDown, Monitor, Setting, Document, Moon, Sunny } from '@element-plus/icons-vue'

// 使用 Pinia stores
const userStore = useUserStore()
const themeStore = useThemeStore()
const menuStore = useMenuStore()

// 图标映射
const iconMap = {
  'house': House,
  'user': User,
  'monitor': Monitor,
  'setting': Setting,
  'document': Document
}

// ✅ 使用 computed 确保响应式
const data = {
  user: computed(() => userStore.user),
  menus: computed(() => menuStore.menus),
  openedMenus: computed(() => menuStore.openedMenus),
  darkMode: computed(() => themeStore.darkMode)
}

const logout = () => {
  userStore.clearUser()
  menuStore.clearMenus()
  location.href = '/login'
}

const updateUser = () => {
  // 重新从 localStorage 加载用户信息
  const userData = JSON.parse(localStorage.getItem('code_user') || '{}')
  userStore.setUser(userData)
}

// 获取图标组件
const getIconComponent = (iconName) => {
  if (iconName) {
    const lowerIconName = iconName.toLowerCase()
    return iconMap[lowerIconName] || null
  }
  return null
}

// 切换主题
const toggleTheme = () => {
  themeStore.toggleTheme()
}

// 加载菜单
const loadMenu = async () => {
  const role = userStore.userRole || 'USER'
  await menuStore.loadMenus(role)
}

onMounted(() => {
  if (userStore.isLoggedIn) {
    // 路由守卫已经验证过 token，这里直接加载菜单
    loadMenu()
    // 初始化主题
    themeStore.initTheme()
  } else {
    // 如果没有用户信息，重定向到登录页
    router.push('/login')
  }
})

</script>

<style scoped>
.manager-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: var(--main-bg-color);
  transition: background-color var(--transition), color var(--transition);
}

.header {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: var(--header-bg-color);
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  position: sticky;
  top: 0;
  z-index: 100;
  transition: background-color var(--transition), color var(--transition);
}

.header-left {
  width: 240px;
  display: flex;
  align-items: center;
  padding-left: 20px;
}

.logo-container {
  display: flex;
  align-items: center;
}

.logo {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  margin-right: 10px;
}

.logo-text {
  font-size: 18px;
  font-weight: bold;
  color: var(--text-color);
  transition: color var(--transition);
}

.header-center {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.header-title {
  font-size: 18px;
  font-weight: 500;
  color: var(--text-color);
  margin: 0;
  transition: color var(--transition);
}

.header-right {
  width: fit-content;
  padding-right: 20px;
  display: flex;
  align-items: center;
  gap: 15px; /* 添加间距 */
}

.theme-toggle-btn {
  width: 44px;
  height: 44px;
  border: none;
  background-color: var(--header-bg-color);
  color: var(--text-color);
  transition: all var(--transition);
  display: flex;
  align-items: center;
  justify-content: center;
}

.theme-toggle-btn:hover {
  background-color: var(--main-bg-color);
  color: var(--el-color-primary);
  transform: scale(1.1);
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 5px;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.user-info:hover {
  background-color: var(--main-bg-color);
}

.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  margin-right: 8px;
}

.user-name {
  margin-right: 8px;
  color: var(--text-color);
  transition: color var(--transition);
}

.arrow-down {
  transition: transform 0.3s;
}

.main-container {
  display: flex;
  flex: 1;
  overflow: hidden;
}

.sidebar {
  width: 240px;
  transition: width 0.3s;
  background-color: var(--sidebar-bg-color);
  box-shadow: 2px 0 6px rgba(0, 21, 41, 0.1);
  height: calc(100vh - 60px);
  position: relative;
  overflow-y: auto;
  transition: background-color var(--transition), color var(--transition);
}

.side-menu {
  border: none;
  background-color: var(--sidebar-bg-color);
  transition: background-color var(--transition);
}

.side-menu:not(.el-menu--collapse) {
  width: 240px;
  min-height: calc(100vh - 60px);
}

.main-content {
  flex: 1;
  padding: 20px;
  background-color: var(--main-bg-color);
  overflow-y: auto;
  transition: background-color var(--transition), color var(--transition);
}

.content-wrapper {
  width: 100%;
  height: 100%;
  background-color: var(--card-bg-color);
  border-radius: 8px;
  padding: 20px;
  box-shadow: var(--box-shadow);
  color: var(--text-color);
  transition: background-color var(--transition), color var(--transition), box-shadow var(--transition);
}

/* Element Plus 菜单样式定制 */
:deep(.el-menu) {
  border: none;
  background-color: var(--sidebar-bg-color) !important;
  transition: background-color var(--transition);
}

:deep(.el-sub-menu__title) {
  background-color: var(--sidebar-bg-color);
  color: var(--text-color);
  transition: background-color var(--transition), color var(--transition);
}

:deep(.el-sub-menu__title:hover) {
  background-color: var(--main-bg-color);
  color: var(--text-color);
}

:deep(.el-menu-item) {
  height: 50px;
  line-height: 50px;
  color: var(--text-color);
  background-color: transparent;
  transition: background-color var(--transition), color var(--transition);
}

:deep(.el-menu .is-active) {
  background-color: #e6f7ff;
  color: #1890ff;
  border-right: 3px solid #1890ff;
}

:deep([data-theme="dark"] .el-menu .is-active) {
  background-color: #3a3a3a;
  color: #409eff;
}

:deep(.el-menu-item:not(.is-active):hover) {
  background-color: var(--main-bg-color);
  color: var(--text-color);
}

:deep(.el-dropdown) {
  cursor: pointer;
}

:deep(.el-tooltip__trigger) {
  outline: none;
}

:deep(.el-menu--inline .el-menu-item) {
  padding-left: 48px !important;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .header-left {
    width: auto;
    padding-left: 10px;
  }

  .sidebar {
    width: 64px;
  }

  .side-menu:not(.el-menu--collapse) {
    width: 64px;
  }

  .logo-text, .user-name, .header-title {
    display: none;
  }

  .logo {
    margin-right: 0;
  }

  .main-content {
    padding: 10px;
  }

  .content-wrapper {
    padding: 10px;
  }
}
</style>