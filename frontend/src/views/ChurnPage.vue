<template>
  <div>
    <h2>流失管理</h2>
    <div style="display: flex; gap: 10px; align-items: center">
      <el-input v-model="keyword" placeholder="搜索客户名称/电话" style="width: 240px" clearable @clear="fetchData" @keyup.enter="fetchData" />
      <el-button type="primary" @click="fetchData">搜索</el-button>
    </div>
    <el-table :data="tableData" border style="margin-top: 15px">
      <template #empty>
        <div style="padding: 40px 0; color: #909399;">
          <span style="font-size: 48px;">📉</span>
          <p style="margin-top: 8px;">暂无流失客户</p>
        </div>
      </template>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="客户名称" min-width="140" />
      <el-table-column prop="phone" label="电话" width="130" />
      <el-table-column prop="churnReason" label="流失原因" min-width="160" />
      <el-table-column prop="churnTime" label="流失时间" width="160" />
      <el-table-column label="操作" width="100" v-if="userStore.role === 'admin'">
        <template #default="{ row }">
          <el-button size="small" type="success" @click="restoreCustomer(row.id)">恢复</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      v-model:current-page="page" v-model:page-size="size"
      :page-sizes="[5,10,20,50]" :total="total"
      layout="total, sizes, prev, pager, next, jumper"
      @size-change="fetchData" @current-change="fetchData" style="margin-top:15px" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../api/request'
import { useUserStore } from '../stores/user'

const userStore = useUserStore()
const tableData = ref([])
const keyword = ref('')
const page = ref(1), size = ref(10), total = ref(0)

const fetchData = async () => {
  try {
    const res = await request.get('/churn/search', {
      params: { keyword: keyword.value || undefined, page: page.value, size: size.value }
    })
    tableData.value = res.records || []
    total.value = res.total || 0
  } catch (e) { tableData.value = []; total.value = 0 }
}

const restoreCustomer = async (id) => {
  try {
    await request.put(`/churn/restore/${id}`)
    ElMessage.success('已恢复为活跃状态')
    fetchData()
  } catch (e) { console.error(e) }
}

onMounted(fetchData)
</script>
