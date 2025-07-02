<template>
  <div class="confirm-box">
    <h2>扫码登录确认</h2>
    <el-form :model="form">
      <el-form-item label="账号">
        <el-input v-model="form.username"></el-input>
      </el-form-item>
      <el-form-item label="密码">
        <el-input type="password" v-model="form.password"></el-input>
      </el-form-item>
      <el-form-item label="角色">
        <el-select v-model="form.role" placeholder="选择角色">
          <el-option label="管理员" value="ADMIN" />
          <el-option label="学生" value="USER" />
          <el-option label="老师" value="TEACHER" />
        </el-select>
      </el-form-item>
      <el-button type="primary" @click="confirmLogin">确认登录</el-button>
    </el-form>
  </div>
</template>

<script setup>
import { reactive } from "vue";
import { useRoute } from "vue-router";
import request from "@/utils/request";
import { ElMessage } from "element-plus";

const route = useRoute();
const uuid = route.query.uuid;

const form = reactive({
  username: '',
  password: '',
  role: 'ADMIN'
});

const confirmLogin = async () => {
  const res = await request.post('/qrcode/confirm', { ...form, uuid });
  if (res.code === 20000) {
    ElMessage.success("确认成功，可关闭此页面");
  } else {
    ElMessage.error(res.message);
  }
};
</script>
