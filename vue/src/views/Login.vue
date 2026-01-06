<template>
  <div class="bg">
    <div class="mask"></div>

    <transition name="fade-in">
      <div class="login-box">
        <div class="form-container">
          <transition name="slide-up">
            <el-form ref="formRef" :model="data.form" :rules="data.rules" class="form-container">
              <div class="form-title">欢迎登录</div>

              <el-form-item prop="username">
                <el-input size="large" v-model="data.form.username" autocomplete="off" placeholder="请输入账号">
                  <template #prefix><el-icon><User /></el-icon></template>
                </el-input>
              </el-form-item>

              <el-form-item prop="password">
                <el-input size="large" show-password v-model="data.form.password" autocomplete="off" placeholder="请输入密码">
                  <template #prefix><el-icon><Lock /></el-icon></template>
                </el-input>
              </el-form-item>

              <el-form-item prop="role">
                <el-select size="large" style="width: 100%" v-model="data.form.role" placeholder="选择角色">
                  <el-option label="管理员" value="ADMIN" />
                  <el-option label="学生" value="USER" />
<!--                  <el-option label="老师" value="TEACHER" />-->
                </el-select>
              </el-form-item>

              <el-button size="large" class="login-btn" type="primary" @click="login">
                <el-icon><Key /></el-icon> 登 录
              </el-button>

              <div class="register-tip">
                还没有账号？<a href="/register">立即注册</a>
              </div>
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
  form: { role: 'ADMIN' },
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
          router.push('/manager/home')
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
  background-color: #ffffffdd;
  border-radius: 12px;
  padding: 40px 35px 35px;
  box-shadow: 0 10px 35px rgba(0, 0, 0, 0.2);
  font-family: 'Inter', sans-serif;
  position: relative;
}

.form-title {
  margin-bottom: 30px;
  font-size: 26px;
  text-align: center;
  color: #333;
  font-weight: 700;
}

.login-btn {
  width: 100%;
  margin-top: 10px;
  background: linear-gradient(90deg, #5a91ff, #376eff);
  border: none;
  font-weight: bold;
}

.register-tip {
  margin-top: 20px;
  text-align: right;
  font-size: 14px;
  color: #666;
}

.register-tip a {
  color: #376eff;
  text-decoration: none;
  font-weight: 600;
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
