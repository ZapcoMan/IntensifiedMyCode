<template>
  <div class="profile-container">
    <div class="profile-card">
      <div class="profile-header">
        <h2>个人中心</h2>
      </div>
      
      <div class="profile-content">
        <div class="avatar-section">
          <div class="avatar-wrapper">
            <img v-if="data.user?.avatar" :src="data.user.avatar" class="avatar-img" alt="头像">
            <img v-else src="https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png" class="avatar-img" alt="默认头像">
          </div>
          <el-upload
            :action="fileUploadUrl"
            :headers="{ token: data.user.token }"
            :on-success="handleFileSuccess"
            list-type="picture"
            :show-file-list="false"
            class="upload-section"
          >
            <el-button type="primary" size="small" class="upload-btn">上传头像</el-button>
          </el-upload>
        </div>
        
        <el-form ref="formRef" :model="data.user" :rules="rules" label-width="100px" class="profile-form">
          <el-form-item prop="username" label="账号">
            <el-input v-model="data.user.username" placeholder="请输入账号" disabled />
          </el-form-item>
          <el-form-item prop="role" label="身份">
            <el-input v-model="data.user.role" placeholder="身份" disabled />
          </el-form-item>
          <el-form-item prop="name" label="真实姓名">
            <el-input v-model="data.user.name" placeholder="请输入真实姓名" />
          </el-form-item>
          <el-form-item prop="phone" label="手机号">
            <el-input v-model="data.user.phone" placeholder="请输入手机号" />
          </el-form-item>
          <el-form-item prop="email" label="邮箱">
            <el-input v-model="data.user.email" placeholder="请输入邮箱" />
          </el-form-item>
        </el-form>
      </div>
      
      <div class="profile-footer">
        <el-button type="primary" @click="update" class="save-btn">保存信息</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive } from "vue";
import { ElMessage } from "element-plus";
import request, { fileUploadUrl } from "@/utils/request.js";

const data = reactive({
  user: JSON.parse(localStorage.getItem('code_user') || "{}")
})

// 表单验证规则
const rules = {
  name: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' },
    { min: 2, max: 10, message: '姓名长度应在2-10个字符之间', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
}

const handleFileSuccess = (res) => {
  data.user.avatar = res.data
  // 更新本地存储
  localStorage.setItem("code_user", JSON.stringify(data.user))
}

const emit = defineEmits(['updateUser'])

const update = () => {
  let url
  if (data.user.role === 'SUPER_ADMIN') {
    url = '/admin/update'
  }
  if (data.user.role === 'USER') {
    url = '/user/update'
  }

  request.put(url, data.user).then(res => {
    if (res.code === 20000) {
      ElMessage.success('更新成功')
      localStorage.setItem("code_user", JSON.stringify(data.user))
      emit('updateUser')
    } else {
      ElMessage.error(res.message || '更新失败')
    }
  }).catch(err => {
    console.error('更新失败:', err)
    ElMessage.error('更新失败，请稍后重试')
  })
}
</script>

<style scoped>
.profile-container {
  display: flex;
  justify-content: center;
  padding: 20px;
  min-height: 100%;
  background-color: var(--main-bg-color);
  transition: background-color var(--transition);
}

.profile-card {
  width: 100%;
  max-width: 600px;
  background-color: var(--card-bg-color);
  border-radius: 12px;
  box-shadow: var(--box-shadow);
  padding: 30px;
  color: var(--text-color);
  transition: background-color var(--transition), color var(--transition), box-shadow var(--transition);
}

.profile-header {
  text-align: center;
  margin-bottom: 30px;
}

.profile-header h2 {
  margin: 0;
  font-size: 24px;
  color: var(--text-color);
  transition: color var(--transition);
}

.profile-content {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 30px;
}

.avatar-wrapper {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  overflow: hidden;
  margin-bottom: 15px;
  border: 3px solid #e4e7ed;
  transition: border-color var(--transition);
}

:deep([data-theme="dark"] .avatar-wrapper) {
  border-color: #44474a;
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.upload-section {
  margin-top: 10px;
}

.upload-btn {
  background-color: var(--el-color-primary);
  border-color: var(--el-color-primary);
  color: white;
  transition: background-color var(--transition), border-color var(--transition), color var(--transition);
}

.profile-form {
  width: 100%;
}

.profile-form :deep(.el-form-item__label) {
  color: var(--text-color);
  transition: color var(--transition);
}

.profile-footer {
  text-align: center;
  margin-top: 20px;
}

.save-btn {
  padding: 12px 40px;
  font-size: 16px;
  background-color: var(--el-color-primary);
  border-color: var(--el-color-primary);
  color: white;
  transition: background-color var(--transition), border-color var(--transition), color var(--transition);
}

:deep([data-theme="dark"] .save-btn:hover),
:deep([data-theme="dark"] .upload-btn:hover) {
  background-color: #66b1ff;
  border-color: #66b1ff;
  color: white;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .profile-card {
    margin: 10px;
    padding: 20px;
  }
  
  .profile-header h2 {
    font-size: 20px;
  }
  
  .avatar-wrapper {
    width: 100px;
    height: 100px;
  }
}
</style>