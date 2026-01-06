<template>
  <div>
    <!-- 头部区域开始 -->
    <div style="height: 60px; display: flex;">
      <div style="width: 240px; display: flex; align-items: center; padding-left: 20px; background-color: #3a456b">
        <img style="width: 40px; height: 40px; border-radius: 50%" src="@/assets/imgs/logo.png" alt="">
        <span style="font-size: 20px; font-weight: bold; color: #f1f1f1; margin-left: 5px">毕业设计</span>
      </div>
      <div style="flex: 1; display: flex; align-items: center; padding-left: 20px; border-bottom: 1px solid #ddd">
          <p style="font-size: larger"> 脚手架 </p>
      </div>
      <div style="width: fit-content; padding-right: 20px; display: flex; align-items: center; border-bottom: 1px solid #ddd">
        <el-dropdown>
          <div style="display: flex; align-items: center">
            <img v-if="data.user?.avatar" style="width: 40px; height: 40px; border-radius: 50%" :src="data.user?.avatar" />
            <img v-else style="width: 40px; height: 40px; border-radius: 50%" src="https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png" alt="" />
            <span style="margin-left: 10px">{{ data.user?.name }}</span>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="router.push('/manager/person')">个人信息</el-dropdown-item>
              <el-dropdown-item @click="router.push('/manager/updatePassword')">修改密码</el-dropdown-item>
              <el-dropdown-item @click="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>
    <!--  头部区域结束 -->

    <!--  下方区域开始 -->
    <div style="display: flex">
      <!--  菜单区域开始 -->
      <div style="width: 240px;">
        <el-menu router :default-openeds="data.openedMenus" :default-active="router.currentRoute.value.path" style="min-height: calc(100vh - 60px)">
          <template v-for="menu in data.menus" :key="menu.id">
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
                  <span>{{ child.name }}</span>
                </el-menu-item>
              </template>
            </el-sub-menu>
          </template>
        </el-menu>
      </div>
      <!--  菜单区域结束 -->

      <!--  数据渲染区域开始 -->
      <div style="flex: 1; width: 0; padding: 10px; background-color: #f2f4ff">
        <RouterView @updateUser="updateUser"/>
      </div>
      <!--  数据渲染区域结束 -->

    </div>
    <!--  下方区域结束 -->

  </div>
</template>

<script setup>

import router from "@/router/index.js";
import { reactive, onMounted } from "vue";
import { getMenuByRole } from '@/api/menu.js'
import { House, User } from '@element-plus/icons-vue'

// 图标映射
const iconMap = {
  'house': House,
  'user': User
}

const data = reactive({
  user: JSON.parse(localStorage.getItem('code_user') || "{}"),
  menus: [],
  openedMenus: []
})

const logout = () => {
  localStorage.removeItem('code_user')
  location.href = '/login'
}

/*if (!data.user?.id) {
  location.href = '/login'
}*/

const updateUser = () => {
  data.user = JSON.parse(localStorage.getItem("code_user") || '{}')
}

// 获取图标组件
const getIconComponent = (iconName) => {
  if (iconName) {
    const lowerIconName = iconName.toLowerCase()
    return iconMap[lowerIconName] || null
  }
  return null
}

// 加载菜单
const loadMenu = async () => {
  try {
    const role = data.user.role || 'USER'
    const res = await getMenuByRole(role)
    if (res.code === 20000) {
      data.menus = res.data  // 修改为使用 res.data 而不是 res.dataMap
      // 初始化展开的菜单
      data.openedMenus = data.menus.map(menu => menu.id.toString())
    } else {
      console.error('获取菜单失败:', res.message)
    }
  } catch (error) {
    console.error('获取菜单时发生错误:', error)
  }
}

onMounted(() => {
  loadMenu()
})

</script>

<style>
.el-menu {
  background-color: #3a456b;
  border: none;
}
.el-sub-menu__title {
  background-color: #3a456b;
  color: #ddd;
}
.el-menu-item {
  height: 50px;
  color: #ddd;
}
.el-menu .is-active {
  background-color: #537bee;
  color: #fff;
}
.el-sub-menu__title:hover {
  background-color: #3a456b;
}
.el-menu-item:not(.is-active):hover {
  background-color: #7a9fff;
  color: #333;
}
.el-dropdown {
  cursor: pointer;
}
.el-tooltip__trigger {
  outline: none;
}
.el-menu--inline .el-menu-item {
  padding-left: 48px !important;
}
</style>