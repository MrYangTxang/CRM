<template>
  <div>
    <h2>回收站</h2>
    <el-tabs v-model="activeTable" @tab-change="fetchData">
      <el-tab-pane label="客户" name="customer" />
      <el-tab-pane label="业务" name="business" />
      <el-tab-pane label="工单" name="work_order" />
    </el-tabs>
    <div style="margin: 15px 0; display: flex; gap: 10px;">
      <el-tag type="danger">客户: {{ stats.customer || 0 }} 条</el-tag>
      <el-tag type="warning">业务: {{ stats.business || 0 }} 条</el-tag>
      <el-tag type="info">工单: {{ stats.work_order || 0 }} 条</el-tag>
    </div>
    <el-table :data="tableData" border>
      <template #empty>
        <div style="padding: 40px 0; color: #909399;">
          <span style="font-size: 48px;">🗑️</span>
          <p style="margin-top: 8px;">回收站为空</p>
        </div>
      </template>
      <el-table-column prop="recordId" label="记录ID" width="100" />
      <el-table-column prop="recordName" label="名称" min-width="200" />
      <el-table-column prop="deletedTime" label="删除时间" width="180" />
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button size="small" type="success" @click="restore(row.recordId)">恢复</el-button>
          <el-button size="small" type="danger" @click="permanentDelete(row.recordId)">彻底删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../api/request'

const activeTable = ref('customer')
const tableData = ref([])
const stats = ref({ customer: 0, business: 0, work_order: 0 })

const fetchData = async () => {
  try {
    const [listRes, statsRes] = await Promise.all([
      request.get(`/recyclebin/list/${activeTable.value}`),
      request.get('/recyclebin/stats')
    ])
    tableData.value = listRes || []
    stats.value = statsRes || {}
  } catch (e) { tableData.value = [] }
}

const restore = async (recordId) => {
  try {
    await request.put(`/recyclebin/restore/${activeTable.value}/${recordId}`)
    ElMessage.success('恢复成功'); fetchData()
  } catch (e) { console.error(e) }
}

const permanentDelete = (recordId) => {
  ElMessageBox.confirm('彻底删除后不可恢复，确定？', '警告', { type: 'warning', confirmButtonText: '确定删除' }).then(async () => {
    await request.delete(`/recyclebin/permanent/${activeTable.value}/${recordId}`)
    ElMessage.success('已彻底删除'); fetchData()
  }).catch(() => {})
}

onMounted(fetchData)
</script>
