<template>
  <div class="register-container">
    <el-card class="register-card">
      <h2>用户注册</h2>
      <el-form :model="form" label-width="80px">
        <el-form-item label="姓名" required>
          <el-input v-model="form.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="用户名" required>
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" required>
          <el-input type="password" v-model="form.password" placeholder="请输入密码（至少6位）" />
        </el-form-item>
        <el-form-item label="确认密码" required>
          <el-input type="password" v-model="form.confirmPassword" placeholder="请再次输入密码" />
        </el-form-item>
        <el-form-item label="电话">
          <el-input v-model="form.phone" placeholder="请输入11位手机号码（可选）" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleRegister" :loading="loading">注册</el-button>
          <el-button @click="goToLogin">已有账号？去登录</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref} from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '../api/request'

const router = useRouter()
const form = reactive({
  name: '',
  username: '',
  password: '',
  confirmPassword: '',
  phone: ''
})
const loading = ref(false)

const handleRegister = async () => {
  // 表单校验
  if (!form.name || !form.name.trim()) {
    ElMessage.warning('姓名不能为空')
    return
  }
  if (!form.username || !form.username.trim()) {
    ElMessage.warning('用户名不能为空')
    return
  }
  if (!form.password || form.password.length < 6) {
    ElMessage.warning('密码至少6位')
    return
  }
  if (form.password !== form.confirmPassword) {
    ElMessage.warning('两次输入的密码不一致')
    return
  }
  if (form.phone && !/^1[3-9]\d{9}$/.test(form.phone)) {
    ElMessage.warning('请输入正确的11位手机号码')
    return
  }

  loading.value = true
  try {
    await request.post('/staff/add', {
      name: form.name,
      username: form.username,
      password: form.password,
      phone: form.phone || '',
      role: 'employee'
    })
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch (error) {
    console.error('注册错误:', error)
    // 不重复弹框：axios 拦截器已提示过
  } finally {
    loading.value = false
    // 重置表单
    form.name = ''
    form.username = ''
    form.password = ''
    form.confirmPassword = ''
    form.phone = ''
  }
}

const goToLogin = () => {
  router.push('/login')
}
</script>