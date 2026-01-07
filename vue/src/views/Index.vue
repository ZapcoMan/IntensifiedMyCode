<template>
  <div class="home-container">
    <div class="welcome-card">
      <div class="welcome-header">
        <h1>欢迎使用管理系统</h1>
        <p>高效、简洁、现代化的管理平台</p>
      </div>
    </div>

    <div class="stats-container">
      <el-row :gutter="20">
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-icon bg-blue">
              <el-icon><User /></el-icon>
            </div>
            <div class="stat-content">
              <h3>128</h3>
              <p>用户总数</p>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-icon bg-green">
              <el-icon><Document /></el-icon>
            </div>
            <div class="stat-content">
              <h3>256</h3>
              <p>文档数量</p>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-icon bg-orange">
              <el-icon><Setting /></el-icon>
            </div>
            <div class="stat-content">
              <h3>18</h3>
              <p>系统配置</p>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-icon bg-purple">
              <el-icon><Monitor /></el-icon>
            </div>
            <div class="stat-content">
              <h3>96%</h3>
              <p>系统可用</p>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <div class="content-container">
      <el-row :gutter="20">
        <el-col :span="16">
          <div class="chart-card">
            <h3>数据概览</h3>
            <div class="chart-placeholder">
              <p>图表展示区域</p>
            </div>
          </div>
          
          <div class="chart-card">
            <h3>用户活跃度</h3>
            <div class="chart-placeholder">
              <p>用户活跃度图表</p>
            </div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="info-card">
            <h3>系统信息</h3>
            <ul>
              <li><span>系统版本:</span> <span>v1.0.0</span></li>
              <li><span>当前用户:</span> <span>{{ data.user?.name }}</span></li>
              <li><span>角色:</span> <span>{{ data.user?.role }}</span></li>
              <li><span>登录时间:</span> <span>{{ new Date().toLocaleString() }}</span></li>
              <li><span>系统状态:</span> <span class="status-active">运行正常</span></li>
            </ul>
          </div>
          
          <div class="quick-card">
            <h3>快捷操作</h3>
            <div class="quick-actions">
              <el-button type="primary" size="small">新增用户</el-button>
              <el-button type="success" size="small">生成报表</el-button>
              <el-button type="warning" size="small">系统设置</el-button>
              <el-button type="info" size="small">查看日志</el-button>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <div class="extra-cards">
      <el-row :gutter="20">
        <el-col :span="8">
          <div class="info-card">
            <h3>通知中心</h3>
            <div class="notification-list">
              <div class="notification-item">
                <p>新用户注册通知</p>
                <span class="time">2分钟前</span>
              </div>
              <div class="notification-item">
                <p>系统更新完成</p>
                <span class="time">1小时前</span>
              </div>
              <div class="notification-item">
                <p>安全提醒</p>
                <span class="time">昨天</span>
              </div>
            </div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="info-card">
            <h3>最近活动</h3>
            <div class="activity-list">
              <div class="activity-item">
                <p>张三登录系统</p>
                <span class="time">10:30</span>
              </div>
              <div class="activity-item">
                <p>李四更新了资料</p>
                <span class="time">09:45</span>
              </div>
              <div class="activity-item">
                <p>王五创建了新文档</p>
                <span class="time">昨天</span>
              </div>
            </div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="info-card">
            <h3>服务器状态</h3>
            <div class="server-status">
              <div class="status-item">
                <span>CPU使用率</span>
                <el-progress :percentage="60" :color="customColorMethod" />
              </div>
              <div class="status-item">
                <span>内存使用率</span>
                <el-progress :percentage="45" color="#67c23a" />
              </div>
              <div class="status-item">
                <span>磁盘使用率</span>
                <el-progress :percentage="75" color="#e6a23c" />
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, watch, onUnmounted } from 'vue'
import { User, Document, Setting, Monitor, Moon, Sunny } from '@element-plus/icons-vue'

const data = reactive({
  user: JSON.parse(localStorage.getItem('code_user') || "{}"),
})

// 从 localStorage 获取当前主题状态
const darkMode = ref(localStorage.getItem('theme') === 'dark')

// 自定义进度条颜色
const customColorMethod = (percentage) => {
  if (percentage < 70) {
    return '#67c23a'
  } else if (percentage < 90) {
    return '#e6a23c'
  } else {
    return '#f56c6c'
  }
}

// 切换主题
const toggleTheme = (value) => {
  if (value) {
    document.documentElement.setAttribute('data-theme', 'dark')
    localStorage.setItem('theme', 'dark')
  } else {
    document.documentElement.setAttribute('data-theme', 'light')
    localStorage.setItem('theme', 'light')
  }
  // 更新响应式变量
  darkMode.value = value
}

// 监听 localStorage 变化，处理其他标签页的主题变化
const handleStorageChange = (e) => {
  if (e.key === 'theme') {
    const newTheme = e.newValue || 'light'
    document.documentElement.setAttribute('data-theme', newTheme)
    darkMode.value = newTheme === 'dark'
  }
}

// 初始化主题
onMounted(() => {
  const savedTheme = localStorage.getItem('theme') || 'light'
  document.documentElement.setAttribute('data-theme', savedTheme)
  darkMode.value = savedTheme === 'dark'
  
  // 监听 localStorage 变化
  window.addEventListener('storage', handleStorageChange)
})

// 组件卸载时移除事件监听
onUnmounted(() => {
  window.removeEventListener('storage', handleStorageChange)
})
</script>

<style scoped>
.home-container {
  padding: 20px;
  position: relative;
}

.header-tools {
  position: fixed;
  top: 80px;
  right: 30px;
  z-index: 1000;
  background: white;
  padding: 12px;
  border-radius: 50%;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background-color var(--transition), color var(--transition);
  background-color: var(--card-bg-color);
  color: var(--text-color);
}

.theme-switch {
  margin: 0;
}

.welcome-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 10px;
  padding: 30px;
  color: white;
  margin-bottom: 20px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  position: relative;
  overflow: hidden;
}

.welcome-header h1 {
  margin: 0 0 10px 0;
  font-size: 28px;
}

.welcome-header p {
  margin: 0;
  font-size: 16px;
  opacity: 0.9;
}

.stats-container {
  margin-bottom: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  background-color: var(--card-bg-color);
  border-radius: 10px;
  padding: 20px;
  box-shadow: var(--box-shadow);
  height: 100px;
  transition: background-color var(--transition), color var(--transition), box-shadow var(--transition);
  color: var(--text-color);
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
  color: white;
  font-size: 24px;
}

.stat-content h3 {
  margin: 0 0 5px 0;
  font-size: 24px;
  color: var(--text-color);
  transition: color var(--transition);
}

.stat-content p {
  margin: 0;
  color: var(--text-color);
  font-size: 14px;
  opacity: 0.7;
  transition: color var(--transition);
}

.bg-blue {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.bg-green {
  background: linear-gradient(135deg, #66f8ea 0%, #2d9f6d 100%);
}

.bg-orange {
  background: linear-gradient(135deg, #ffa500 0%, #ff7f00 100%);
}

.bg-purple {
  background: linear-gradient(135deg, #9d5cff 0%, #6a4ff7 100%);
}

.content-container {
  margin-top: 20px;
}

.chart-card, .info-card, .quick-card {
  background-color: var(--card-bg-color);
  border-radius: 10px;
  padding: 20px;
  box-shadow: var(--box-shadow);
  margin-bottom: 20px;
  transition: background-color var(--transition), color var(--transition), box-shadow var(--transition);
  color: var(--text-color);
}

.chart-card h3, .info-card h3, .quick-card h3 {
  margin: 0 0 15px 0;
  color: var(--text-color);
  font-size: 18px;
  transition: color var(--transition);
}

.chart-placeholder {
  height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--main-bg-color);
  border-radius: 8px;
  color: var(--text-color);
  font-size: 16px;
  transition: background-color var(--transition), color var(--transition);
}

.info-card ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.info-card li {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px solid var(--border-color);
  transition: border-color var(--transition);
}

.info-card li:last-child {
  border-bottom: none;
}

.info-card li span:first-child {
  color: var(--text-color);
  transition: color var(--transition);
}

.info-card li span:last-child {
  color: var(--text-color);
  transition: color var(--transition);
}

.status-active {
  color: #67c23a;
  font-weight: bold;
  transition: color var(--transition);
}

.quick-actions {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}

.notification-list, .activity-list {
  padding: 0;
}

.notification-item, .activity-item {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px solid var(--border-color);
  transition: border-color var(--transition);
}

.notification-item:last-child, .activity-item:last-child {
  border-bottom: none;
}

.time {
  color: var(--text-color);
  opacity: 0.7;
  font-size: 12px;
  transition: color var(--transition);
}

.server-status {
  padding-top: 10px;
}

.status-item {
  margin-bottom: 15px;
}

.status-item:last-child {
  margin-bottom: 0;
}

.extra-cards {
  margin-top: 20px;
}
</style>