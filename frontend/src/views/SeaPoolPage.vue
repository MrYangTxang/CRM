<template>
  <div>
    <h2>公海池</h2>
    <div style="margin-bottom: 10px; color: #e6a23c;">
      今日剩余领取额度：<strong>{{ quota }}</strong> / 3
    </div>
    <div style="display: flex; gap: 10px; align-items: center">
      <el-input v-model="keyword" placeholder="搜索客户名称/电话" style="width: 240px" clearable @clear="fetchData" @keyup.enter="fetchData" />
      <el-button type="primary" @click="fetchData">搜索</el-button>
    </div>
    <el-table :data="tableData" border style="margin-top: 15px">
      <template #empty>
        <div style="padding: 40px 0; color: #909399;">
          <span style="font-size: 48px;">🌊</span>
          <p style="margin-top: 8px;">公海池暂无客户</p>
          <p style="font-size: 12px; color: #c0c4cc;">所有客户都已被认领</p>
        </div>
      </template>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="客户名称" min-width="140" />
      <el-table-column prop="phone" label="电话" width="130" />
      <el-table-column prop="address" label="地址" min-width="160" />
      <el-table-column prop="vipLevel" label="VIP等级" width="100" />
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="claimCustomer(row.id)">领取</el-button>
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

const tableData = ref([])
const keyword = ref('')
const quota = ref(0)
const page = ref(1), size = ref(10), total = ref(0)

const fetchData = async () => {
  try {
    const [dataRes, quotaRes] = await Promise.all([
      request.get('/seapool/search', { params: { keyword: keyword.value || undefined, page: page.value, size: size.value } }),
      request.get('/seapool/quota')
    ])
    tableData.value = dataRes.records || []
    total.value = dataRes.total || 0
    quota.value = quotaRes ?? 0
  } catch (e) {
    tableData.value = []; total.value = 0
  }
}

const claimCustomer = async (id) => {
  try {
    await request.post(`/seapool/claim/${id}`)
    ElMessage.success('领取成功')
    fetchData()
  } catch (e) { console.error(e) }
}

onMounted(fetchData)
</script>
