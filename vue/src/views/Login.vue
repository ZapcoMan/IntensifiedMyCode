<template>
  <div class="bg">
    <div class="mask"></div>

    <transition name="fade-in">
      <div class="login-box">
        <div class="form-container">
          <div class="login-header">
            <div class="logo-section">
              <img src="@/assets/imgs/logo.png" alt="Logo" class="logo">
              <h2 class="title">增强型脚手架</h2>
            </div>
            <p class="subtitle">欢迎使用我们的系统</p>
          </div>

          <transition name="slide-up">
            <el-form ref="formRef" :model="data.form" :rules="data.rules" class="form-container">

              <el-form-item prop="username">
                <el-input size="large" v-model="data.form.username" autocomplete="off" placeholder="请输入账号">
                  <template #prefix>
                    <el-icon><User /></el-icon>
                  </template>
                </el-input>
              </el-form-item>

              <el-form-item prop="password">
                <el-input size="large" show-password v-model="data.form.password" autocomplete="off"
                          placeholder="请输入密码" >
                  <template #prefix>
                    <el-icon><Lock /></el-icon>
                  </template>
                </el-input>
              </el-form-item>

              <el-form-item prop="role">
                <el-select size="large" style="width: 100%" v-model="data.form.role" placeholder="选择角色">
                  <el-option label="超级管理员" value="SUPER_ADMIN" />
                  <el-option label="学生" value="USER" />
                </el-select>
              </el-form-item>

              <el-button size="large" class="login-btn" type="primary" @click="login">
                <el-icon><Key /></el-icon> 登 录
              </el-button>


            </el-form>
          </transition>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'

import request from '@/utils/request.js'
import { ElMessage } from 'element-plus'
import router from '@/router/index.js'
import { User, Lock, Key } from '@element-plus/icons-vue'

const formRef = ref()

const data = reactive({
  form: { role: 'SUPER_ADMIN' },
  rules: {
    username: [
      { required: true, message: '请输入账号', trigger: 'blur' },
      { min: 6, message: '账号最少6位', trigger: 'blur' },
    ],
    password: [
      { required: true, message: '请输入密码', trigger: 'blur' }
    ],
  }
})

const login = () => {
  formRef.value.validate((valid) => {
    if (valid) {
      request.post('/login', data.form).then(res => {
        if (res.code === 20000) {
          localStorage.setItem("code_user", JSON.stringify(res.data || {}))
          localStorage.setItem("token", res.data.token)
          ElMessage.success('登录成功')
          router.push('/manager/index')
        } else {
          ElMessage.error(res.message)
        }
      })
    }
  })
}




</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@500;700&display=swap');

.bg {
  height: 100vh;
  position: relative;
  background-image: url('@/assets/imgs/bg.jpg');
  background-size: cover;
  background-position: center;
  display: flex;
  justify-content: center;
  align-items: center;
}

.mask {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  backdrop-filter: blur(4px);
  background-color: rgba(0, 0, 0, 0.4);
}

.login-box {
  z-index: 1;
  width: 420px;
  background-color: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  padding: 40px 35px 35px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.15);
  font-family: 'Inter', sans-serif;
  position: relative;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.logo-section {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 10px;
}

.logo {
  width: 40px;
  height: 40px;
  margin-right: 12px;
}

.title {
  margin: 0;
  font-size: 22px;
  color: #333;
  font-weight: 700;
}

.subtitle {
  margin: 0;
  font-size: 14px;
  color: #666;
}

.form-container {
  padding-top: 20px;
}

.login-btn {
  width: 100%;
  margin-top: 10px;
  background: linear-gradient(90deg, #5a91ff, #376eff);
  border: none;
  font-weight: bold;
  height: 48px;
  font-size: 16px;
  letter-spacing: 1px;
  transition: all 0.3s ease;
}

.login-btn:hover {
  background: linear-gradient(90deg, #4a81ef, #275eff);
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(55, 110, 255, 0.3);
}

.register-tip {
  margin-top: 20px;
  text-align: center;
  font-size: 14px;
  color: #666;
}

.register-tip a {
  color: #376eff;
  text-decoration: none;
  font-weight: 600;
  position: relative;
  padding: 4px 0;
}

.register-tip a::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 0;
  height: 1px;
  background: #376eff;
  transition: width 0.3s ease;
}

.register-tip a:hover::after {
  width: 100%;
}

.el-form-item {
  margin-bottom: 24px;
}

.el-input__wrapper {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.el-input__wrapper:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.el-input__wrapper.is-focus {
  box-shadow: 0 4px 12px rgba(55, 110, 255, 0.2);
}

.el-select .el-input__wrapper {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

@keyframes fadeIn {
  0% { opacity: 0; transform: translateY(20px); }
  100% { opacity: 1; transform: translateY(0); }
}

@keyframes slideUp {
  0% { opacity: 0; transform: translateY(40px); }
  100% { opacity: 1; transform: translateY(0); }
}

@keyframes zoomIn {
  0% { transform: scale(0.8); opacity: 0; }
  100% { transform: scale(1); opacity: 1; }
}
</style>
