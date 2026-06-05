<template>
  <div class="login-container">
    <el-card class="login-card">
      <h2>CRM 系统登录</h2>
      <el-form :model="form" label-width="100px">
        <el-form-item label="用户名">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input type="password" v-model="form.password" placeholder="请输入密码" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleLogin" :loading="loading">登录</el-button>
          <el-link type="primary" @click="goToRegister" style="margin-left: 20px;">没有账号？立即注册</el-link>
          <el-link type="warning" @click="forgotPasswordVisible = true" style="margin-left: 20px;">忘记密码？</el-link>
        </el-form-item>
      </el-form>
    </el-card>
    <!-- 重置密码弹窗 -->
      <el-dialog v-model="forgotPasswordVisible" title="重置密码" width="550px" :style="{ height: '400px' }">
        <el-form :model="resetForm" label-width="100px">
          <el-form-item label="用户名">
            <el-input v-model="resetForm.username" />
          </el-form-item>
          <el-form-item label="新密码">
            <el-input type="password" v-model="resetForm.newPassword" />
          </el-form-item>
          <el-form-item label="确认密码">
            <el-input type="password" v-model="resetForm.confirmPassword" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="forgotPasswordVisible = false">取消</el-button>
          <el-button type="primary" @click="resetPassword">确认重置</el-button>
        </template>
      </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '../api/request'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()
const form = reactive({ username: 'admin', password: '123456' })
const loading = ref(false)

// 忘记密码弹窗相关
const forgotPasswordVisible = ref(false)
const resetForm = reactive({
  username: '',
  newPassword: '',
  confirmPassword: ''
})

// 登录
const handleLogin = async () => {
  if (!form.username || !form.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  loading.value = true
  try {
    const res = await request.post(`/staff/login?username=${form.username}&password=${form.password}`)
    if (res && res.username) {
      userStore.setUser(res.username, res.role, btoa(form.username + ':' + Date.now()))
      ElMessage.success('登录成功')
      router.push('/')
    } else {
      ElMessage.error(res?.message || '用户名或密码错误')
    }
  } catch (error) {
    console.error('登录错误', error)
  } finally {
    loading.value = false
  }
}

// 重置密码
const resetPassword = async () => {
  if (!resetForm.username || !resetForm.newPassword) {
    ElMessage.warning('用户名和新密码不能为空')
    return
  }
  if (resetForm.newPassword !== resetForm.confirmPassword) {
    ElMessage.warning('两次输入的密码不一致')
    return
  }
  if (resetForm.newPassword.length < 6) {
    ElMessage.warning('新密码至少6位')
    return
  }
  try {
    const res = await request.put('/staff/resetPassword', null, {
      params: {
        username: resetForm.username,
        newPassword: resetForm.newPassword
      }
    })
    ElMessage.success(res)
    forgotPasswordVisible.value = false
    // 清空表单
    resetForm.username = ''
    resetForm.newPassword = ''
    resetForm.confirmPassword = ''
  } catch (error) {
    console.error(error)
  }
}

const goToRegister = () => {
  router.push('/register')
}
</script>

<style scoped>
/* 保持原有样式不变 */
.login-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
.login-card {
  width: 400px;
  border-radius: 16px;
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.2);
  backdrop-filter: blur(2px);
  background-color: rgba(255, 255, 255, 0.95);
}
.login-card h2 {
  text-align: center;
  margin-bottom: 24px;
  color: #333;
  font-weight: 500;
}
:deep(.el-input__wrapper) {
  border-radius: 8px;
  transition: all 0.3s;
}
:deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #667eea inset;
}
:deep(.el-button--primary) {
  width: 100%;
  background: linear-gradient(90deg, #667eea, #764ba2);
  border: none;
  border-radius: 8px;
  font-weight: 500;
}
:deep(.el-button--primary:hover) {
  background: linear-gradient(90deg, #5a67d8, #6b46a0);
}
</style>