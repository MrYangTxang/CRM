<template>
  <el-container>
    <el-aside width="200px">
      <el-menu router :default-active="$route.path">
        <el-menu-item index="/dashboard">📊 工作台</el-menu-item>
        <el-menu-item index="/customer">👥 客户管理</el-menu-item>
        <el-menu-item index="/staff" v-if="userStore.role === 'admin'">👤 人员管理</el-menu-item>
        <el-menu-item index="/business">💼 业务管理</el-menu-item>
        <el-menu-item index="/workorder">📝 工单管理</el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header>
        <div style="float: right;">
          欢迎, {{ username }} | <el-button type="text" @click="logout">退出</el-button>
        </div>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()
const username = computed(() => userStore.username)

const logout = () => {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.el-container {
  height: 100vh;
  background-color: #f5f7fa;
}
.el-aside {
  background: linear-gradient(180deg, #2b3a4a 0%, #1c2a36 100%);
  color: #fff;
  box-shadow: 2px 0 12px rgba(0, 0, 0, 0.1);
}
.el-menu {
  border-right: none;
  background-color: transparent;
}
.el-menu-item {
  color: #e0e0e0;
  border-radius: 10px;
  margin: 8px 12px;
  transition: all 0.2s;
}
.el-menu-item:hover {
  background-color: rgba(255, 255, 255, 0.1);
  color: #fff;
}
.el-menu-item.is-active {
  background: linear-gradient(90deg, #409eff, #36d1dc);
  color: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}
.el-header {
  background-color: #fff;
  border-bottom: 1px solid #e9ecef;
  line-height: 60px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding: 0 24px;
}
.el-main {
  padding: 20px;
  background-color: #f5f7fa;
}
</style>
