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
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import { User, Document, Setting, Monitor } from '@element-plus/icons-vue'

const data = reactive({
  user: JSON.parse(localStorage.getItem('code_user') || "{}"),
})
</script>

<style scoped>
.home-container {
  padding: 20px;
}

.welcome-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 10px;
  padding: 30px;
  color: white;
  margin-bottom: 20px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
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
  background: white;
  border-radius: 10px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  height: 100px;
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
  color: #333;
}

.stat-content p {
  margin: 0;
  color: #999;
  font-size: 14px;
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

.chart-card, .info-card {
  background: white;
  border-radius: 10px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  margin-bottom: 20px;
}

.chart-card h3, .info-card h3 {
  margin: 0 0 15px 0;
  color: #333;
  font-size: 18px;
}

.chart-placeholder {
  height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f8f9ff;
  border-radius: 8px;
  color: #999;
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
  border-bottom: 1px solid #eee;
}

.info-card li:last-child {
  border-bottom: none;
}

.info-card li span:first-child {
  color: #666;
}

.info-card li span:last-child {
  color: #333;
}

.status-active {
  color: #67c23a;
  font-weight: bold;
}
</style>