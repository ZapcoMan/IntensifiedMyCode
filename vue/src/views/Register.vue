<template>
  <div class="bg">
    <div
        style="width: 350px; background-color: #fff; border-radius: 5px; box-shadow: 0 0 10px rgba(0,0,0,0.1); padding: 40px 20px">
      <el-form status-icon ref="formRef" :model="data.form" :rules="data.rules">
        <div style="margin-bottom: 40px; text-align: center; font-weight: bold; font-size: 24px">欢 迎 注 册</div>
        <el-form-item prop="username">
          <el-input size="large" v-model="data.form.username" autocomplete="off" prefix-icon="User"
                    placeholder="请输入账号"/>
        </el-form-item>
        <el-form-item prop="name">
          <el-input size="large" v-model="data.form.name" autocomplete="off" prefix-icon="UserFilled"
                    placeholder="请输入真实姓名"/>
        </el-form-item>
        <el-form-item prop="password">
          <el-input size="large" show-password v-model="data.form.password" autocomplete="off" prefix-icon="Lock"
                    placeholder="请输入密码"/>
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input size="large" show-password v-model="data.form.confirmPassword" autocomplete="off" prefix-icon="Lock"
                    placeholder="请再次确认密码"/>
        </el-form-item>
        <el-form-item prop="avatar">
          <el-upload
              :action="fileUploadUrl"
              :headers="{ token: data.user?.token }"
              :on-success="handleFileSuccess"
              list-type="picture"
          >
            <el-button type="primary">上传头像</el-button>
          </el-upload>
        </el-form-item>
        <div style="margin-bottom: 20px">
          <el-button style="width: 100%; background-color: #248243; border-color: #248243" size="large" type="primary"
                     @click="registers">注 册
          </el-button>
        </div>
        <div style="text-align: right">
          已有账号？请 <a style="color: #248243" href="/login">登录</a>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import {reactive, ref} from "vue";
import {ElMessage} from "element-plus";
import router from "@/router/index.js";
import {register} from "@/api/user.js";
import {fileUploadUrl} from '@/utils/request'

// 定义一个验证密码的函数
const validatePass = (rule, value, callback) => {
  // value 表示用户输入的确认密码
  if (value !== data.form.password) {
    callback(new Error("两次输入的密码不匹配！"))
  } else {
    callback()
  }
}

// 创建一个对表单的引用，用于表单验证
const formRef = ref()
// 定义一个响应式对象，包含表单数据和验证规则
const data = reactive({
  form: {},
  rules: {
    username: [
      {required: true, message: '请输入账号', trigger: 'blur'},
      {min: 6, message: '账号最少6位', trigger: 'blur'},
    ],
    name: [
      {required: true, message: '请输入真实姓名', trigger: 'blur'},
    ],
    password: [
      {required: true, message: '请输入密码', trigger: 'blur'}
    ],
    confirmPassword: [
      {required: true, message: '请再次确认密码', trigger: 'blur'},
      {validator: validatePass, trigger: 'blur'}
    ]
  }
})

// 处理文件上传成功的回调
const handleFileSuccess = (res) => {
  data.form.avatar = res.data
}

// 定义一个执行注册的函数
const registers = () => {
  // 调用表单验证方法，如果表单验证通过，则执行注册逻辑
  formRef.value.validate((valid) => {
    if (valid) {
      // 调用注册API进行注册，根据返回结果给出相应反馈
      register(data.form).then(res => {
        if (res.code === 20000) {
          ElMessage.success('注册成功')
          router.push('/login')
        } else {
          ElMessage.error(res.message)
        }
      })
    }
  })
}


</script>

<style scoped>
.bg {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;
  background-image: url("@/assets/imgs/bg1.jpg");
  background-size: cover;
}
</style>