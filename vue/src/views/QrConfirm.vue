<template>
  <div class="confirm-container">
    <el-card class="confirm-card">
      <h2 style="text-align: center; margin-bottom: 20px">扫码确认登录</h2>

      <div style="margin-bottom: 10px">二维码 Token：{{ token }}</div>

      <el-form :model="form" ref="formRef" :rules="rules" label-width="80px">
        <el-form-item prop="username" label="账号">
          <el-input v-model="form.username" placeholder="请输入账号" />
        </el-form-item>

        <el-form-item prop="password" label="密码">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" />
        </el-form-item>

        <el-form-item prop="role" label="角色">
          <el-select v-model="form.role" placeholder="请选择角色">
            <el-option label="管理员" value="ADMIN" />
            <el-option label="学生" value="USER" />
            <el-option label="教师" value="TEACHER" />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" style="width: 100%" @click="confirmLogin">确认登录</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from "vue";
import { useRoute } from "vue-router";
import request from "@/utils/request";
import { ElMessage } from "element-plus";

const route = useRoute();
const token = route.query.token;

const formRef = ref();

const form = reactive({
  username: "",
  password: "",
  role: "ADMIN",
});

const rules = {
  username: [{ required: true, message: "请输入账号", trigger: "blur" }],
  password: [{ required: true, message: "请输入密码", trigger: "blur" }],
  role: [{ required: true, message: "请选择角色", trigger: "change" }],
};

const confirmLogin = () => {
  formRef.value.validate(async (valid) => {
    if (!valid) return;

    const payload = { ...form, token };
    try {
      const res = await request.post("/qrcode/confirm", payload);
      if (res.code === 20000) {
        ElMessage.success("扫码登录确认成功，请回到电脑网页端继续操作");
      } else {
        ElMessage.error(res.message);
      }
    } catch (e) {
      ElMessage.error("请求失败");
    }
  });
};
</script>

<style scoped>
.confirm-container {
  padding: 20px;
  max-width: 400px;
  margin: 50px auto;
}
.confirm-card {
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
}
</style>
