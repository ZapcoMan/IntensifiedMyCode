<template>
  <div class="bg">
    <div class="login-box">
      <!-- 切换登录模式 -->
      <div class="tab-switch">
        <el-button :type="mode === 'form' ? 'primary' : 'default'" @click="mode = 'form'">账号登录</el-button>
        <el-button :type="mode === 'qrcode' ? 'primary' : 'default'" @click="mode = 'qrcode'">扫码登录</el-button>
      </div>

      <!-- 账号密码登录 -->
      <el-form v-if="mode === 'form'" ref="formRef" :model="data.form" :rules="data.rules">
        <div class="title">欢 迎 登 录</div>
        <el-form-item prop="username">
          <el-input size="large" v-model="data.form.username" autocomplete="off" prefix-icon="User" placeholder="请输入账号" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input size="large" show-password v-model="data.form.password" autocomplete="off" prefix-icon="Lock" placeholder="请输入密码" />
        </el-form-item>
        <el-form-item prop="role">
          <el-select size="large" style="width: 100%" v-model="data.form.role">
            <el-option label="管理员" value="ADMIN"></el-option>
            <el-option label="学生" value="USER"></el-option>
            <el-option label="老师" value="TEACHER"></el-option>
          </el-select>
        </el-form-item>
        <el-button style="width: 100%" size="large" type="primary" @click="login">登 录</el-button>
        <div style="text-align: right; margin-top: 10px">
          还没有账号？请 <a href="/register" style="color: #274afa">注册</a>
        </div>
      </el-form>

      <!-- 二维码登录 -->
      <div v-else class="qrcode-login">
        <div class="title">扫码登录</div>
        <div v-if="qrcodeUrl">
          <img :src="qrcodeUrl" style="width: 200px; height: 200px" />
        </div>
        <div style="margin-top: 10px">{{ statusText }}</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, watch } from "vue";
import router from "@/router";
import request from "@/utils/request";
import QRCode from "qrcode";
import { ElMessage } from "element-plus";

const mode = ref("form"); // 登录模式 form / qrcode
const formRef = ref();
const token = ref("");
const qrcodeUrl = ref("");
const statusText = ref("请使用移动端扫码登录");

const data = reactive({
  form: { role: "ADMIN" },
  rules: {
    username: [
      { required: true, message: "请输入账号", trigger: "blur" },
      { min: 6, message: "账号最少6位", trigger: "blur" },
    ],
    password: [{ required: true, message: "请输入密码", trigger: "blur" }],
  },
});

const login = () => {
  formRef.value.validate((valid) => {
    if (valid) {
      request.post("/login", data.form).then((res) => {
        if (res.code === 20000) {
          localStorage.setItem("code_user", JSON.stringify(res.data || {}));
          ElMessage.success("登录成功");
          router.push("/");
        } else {
          ElMessage.error(res.message);
        }
      });
    }
  });
};

const generateQr = async () => {
  const res = await request.get("/qrcode/token");
  token.value = res.data;
  qrcodeUrl.value = await QRCode.toDataURL(`http://localhost:5173/qrcode/confirm?token=${token.value}`);
  pollQrStatus();
};

const pollQrStatus = () => {
  const interval = setInterval(async () => {
    const res = await request.get("/qrcode/status", { params: { token: token.value } });
    if (res.code === 20000 && res.data.token) {
      localStorage.setItem("code_user", JSON.stringify(res.data.account));
      ElMessage.success("扫码登录成功");
      clearInterval(interval);
      router.push("/");
    }
  }, 3000);
};

watch(mode, (newVal) => {
  if (newVal === "qrcode") generateQr();
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
  width: 350px;
  padding: 40px 20px;
  background-color: #fff;
  border-radius: 5px;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
}
.tab-switch {
  display: flex;
  justify-content: space-around;
  margin-bottom: 30px;
}
.title {
  text-align: center;
  font-weight: bold;
  font-size: 24px;
  margin-bottom: 30px;
}
.qrcode-login {
  text-align: center;
}
</style>
