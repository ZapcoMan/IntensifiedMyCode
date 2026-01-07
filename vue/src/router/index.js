import { createRouter, createWebHistory } from 'vue-router'

// 创建一个路由实例
const router = createRouter({
  // 使用HTML5的history API来维护应用的历史记录
  history: createWebHistory(import.meta.env.BASE_URL),
  // 定义应用的路由配置
  routes: [
    // 根路径重定向到管理主页
    { path: '/', redirect:'/login'},
    // 管理员相关路由配置
    { path: '/manager', component: () =>import('../views/Manager.vue'),
      children:[
        // 主页路由
        {path: 'index', meta: {name:'主页'}, component: () =>import('../views/Index.vue'),},
        // 管理员信息路由
        {path: 'admin', meta: {name:'管理员信息'}, component: () =>import('../views/Admin.vue')},
        // 用户（学生）信息路由
        {path: 'user', meta: {name:'用户(学生)信息'}, component: () =>import('../views/User.vue')},
        // 个人信息路由
        {path: 'person', meta: {name:'个人信息'}, component: () =>import('../views/Person.vue'),},
        // 修改密码路由
        {path: 'updatePassword', meta: {name:'修改密码'}, component: () =>import('../views/UpdatePassword.vue'),},
        // src/router/index.js 或 routes.js


      ]
    },
    // 登录路由配置
    { path: '/login', component: () =>import('../views/Login.vue')},
    // 注册路由配置
    // { path: '/register', component: () =>import('../views/Register.vue')},
    // 404页面路由配置
    { path: '/notfound', component: () =>import('../views/404.vue'),},
    // 捕获所有未匹配到的路径并重定向到404页面
    { path: '/:pathMatch(.*)', redirect: '/notFound' },

  ],
})

// 添加路由守卫，防止已登录用户访问登录页面并验证token有效性
router.beforeEach(async (to, from, next) => {
  // 如果用户访问登录页面或其子路径
  if (to.path.startsWith('/login')) {
    // 检查本地存储中是否有用户信息
    const user = localStorage.getItem('code_user')
    if (user) {
      // 如果已登录，重定向到首页
      next('/manager/index')
    } else {
      // 如果未登录，允许访问登录页面
      next()
    }
  } else {
    // 检查是否已登录，如果已登录则验证token是否有效
    const user = localStorage.getItem('code_user')
    if (user) {
      try {
        // 验证token是否仍然有效
        const res = await import('@/api/auth.js')
        const response = await res.validateToken()
        if (response.code === 20000) {
          // Token有效，允许访问
          next()
        } else {
          // Token无效，清除本地存储并重定向到登录页面
          localStorage.removeItem('code_user')
          localStorage.removeItem('token')
          next('/login')
        }
      } catch (error) {
        // 验证失败，可能是网络问题或token已过期
        localStorage.removeItem('code_user')
        localStorage.removeItem('token')
        next('/login')
      }
    } else {
      // 未登录，直接通过（登录页面会由上面的逻辑处理）
      next()
    }
  }
})

// 导出路由实例供其他模块使用
export default router

