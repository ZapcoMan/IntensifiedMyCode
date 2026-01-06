import { createApp } from 'vue'
// import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'

// 导入全局样式 - 只导入 global.css，避免循环依赖
import './assets/css/global.css'
// 移除 index.scss 的直接导入，因为可能与 global.css 产生循环依赖

// 导入 Element Plus
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'

// 初始化主题
function initializeTheme() {
  const savedTheme = localStorage.getItem('theme') || 'light'
  document.documentElement.setAttribute('data-theme', savedTheme)
}

// 在应用启动前初始化主题
initializeTheme()

const app = createApp(App)

// app.use(createPinia())
app.use(router)
app.use(ElementPlus)

app.mount('#app')