<template>
  <div class="bg">
    <div class="login-box">
      <el-tabs v-model="activeTab" stretch>
        <el-tab-pane label="账号密码登录" name="password">
          <el-form ref="formRef" :model="data.form" :rules="data.rules">
            <div class="form-title">欢 迎 登 录</div>
            <el-form-item prop="username">
              <el-input size="large" v-model="data.form.username" autocomplete="off" prefix-icon="User"
                        placeholder="请输入账号"/>
            </el-form-item>
            <el-form-item prop="password">
              <el-input size="large" show-password v-model="data.form.password" autocomplete="off" prefix-icon="Lock"
                        placeholder="请输入密码"/>
            </el-form-item>
            <el-form-item prop="role">
              <el-select size="large" style="width: 100%" v-model="data.form.role">
                <el-option label="管理员" value="ADMIN"></el-option>
                <el-option label="学生" value="USER"></el-option>
                <el-option label="老师" value="TEACHER"></el-option>
              </el-select>
            </el-form-item>
            <div style="margin-bottom: 20px">
              <el-button style="width: 100%" size="large" type="primary" @click="login">登 录</el-button>
            </div>
            <div style="text-align: right">
              还没有账号？请 <a style="color: #274afa" href="/register">注册</a>
            </div>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="扫码登录" name="qrcode">
          <div class="qr-box">
            <canvas ref="canvasRef"></canvas>
            <p v-if="qrStatus === 'pending'">请使用手机扫码确认登录</p>
            <p v-if="qrStatus === 'confirmed'">登录成功，正在跳转...</p>
            <p v-if="qrStatus === 'expired'" style="color:red">二维码已过期，请切换刷新</p>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup>
import {reactive, ref, watch} from "vue";
import QRCode from "qrcode";
import request from "@/utils/request.js";
import {ElMessage} from "element-plus";
import router from "@/router/index.js";

const formRef = ref();
const activeTab = ref("password");
const canvasRef = ref();
const uuid = ref("");
const qrStatus = ref("pending");
let pollTimer = null;

const data = reactive({
  form: {role: 'ADMIN'},
  rules: {
    username: [
      {required: true, message: '请输入账号', trigger: 'blur'},
      {min: 6, message: '账号最少6位', trigger: 'blur'},
    ],
    password: [
      {required: true, message: '请输入密码', trigger: 'blur'}
    ],
  }
});

const login = () => {
  formRef.value.validate((valid) => {
    if (valid) {
      request.post('/login', data.form).then(res => {
        if (res.code === 20000) {
          localStorage.setItem("code_user", JSON.stringify(res.data || {}));
          localStorage.setItem("token", res.data.token);
          ElMessage.success('登录成功');
          router.push('/manager/home');
        } else {
          ElMessage.error(res.message);
        }
      });
    }
  });
};

/**
 * 初始化二维码登录功能
 * 此函数负责生成二维码，并启动轮询机制以检查二维码扫描状态
 */
const initQrLogin = async () => {
  // 请求生成二维码的UUID
  const res = await request.get('/qrcode/generate');
  // 将返回的UUID绑定到响应式变量uuid
  uuid.value = res.data;
  // 设置二维码状态为"pending"，表示尚未扫描
  qrStatus.value = "pending";
  // 生成二维码到Canvas，二维码内容包含UUID和当前域名
  // 引入 utils/request.js的baseURL
  // const qrContent = `${import.meta.env.VITE_BASE_API}/qr-confirm?uuid=${uuid.value}`;
  const qrContent = `${location.origin}/qr-confirm?uuid=${uuid.value}`;
  QRCode.toCanvas(canvasRef.value, qrContent);

  // QRCode.toCanvas(canvasRef.value, `${request.baseURL}/qr-confirm?uuid=${uuid.value}`);
  // QRCode.toCanvas(canvasRef.value, `${location.origin}/qr-confirm?uuid=${uuid.value}`);
  // 启动轮询机制，检查二维码扫描后的状态
  startPolling();
};


const startPolling = () => {
  clearInterval(pollTimer);
  pollTimer = setInterval(async () => {
    const res = await request.get(`/qrcode/status/${uuid.value}`);
    const status = res.data.status;
    if (status === 'confirmed') {
      clearInterval(pollTimer);
      const account = res.data.account;
      localStorage.setItem("code_user", JSON.stringify(account));
      localStorage.setItem("token", account.token);
      qrStatus.value = "confirmed";
      ElMessage.success("扫码成功");
      setTimeout(() => {
        router.push('/manager/home');
      }, 800);
    } else if (status === 'expired') {
      clearInterval(pollTimer);
      qrStatus.value = "expired";
    }
  }, 2000);
};

watch(activeTab, (val) => {
  if (val === "qrcode") {
    initQrLogin();
  } else {
    clearInterval(pollTimer);
  }
});
</script>

<style scoped>
.bg {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-image: url("@/assets/imgs/bg.jpg");
  background-size: cover;
}

.login-box {
  width: 380px;
  background-color: #fff;
  border-radius: 6px;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.12);
  padding: 30px 25px;
}

.form-title {
  margin-bottom: 30px;
  text-align: center;
  font-weight: bold;
  font-size: 22px;
}

.qr-box {
  display: flex;
  flex-direction: column;
  align-items: center;
}
</style>
