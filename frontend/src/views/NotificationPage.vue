<template>
  <div>
    <h2>
      通知中心
      <el-badge :value="unread" :hidden="unread === 0" style="margin-left: 10px" />
    </h2>
    <el-button size="small" @click="markAllRead" :disabled="unread === 0">全部标为已读</el-button>
    <el-table :data="tableData" border style="margin-top: 15px">
      <template #empty>
        <div style="padding: 40px 0; color: #909399;">
          <span style="font-size: 48px;">🔔</span>
          <p style="margin-top: 8px;">暂无通知</p>
        </div>
      </template>
      <el-table-column label="类型" width="120">
        <template #default="{ row }">
          <el-tag :type="typeTag(row.type)" size="small">{{ typeText(row.type) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="title" label="标题" min-width="160" />
      <el-table-column prop="content" label="内容" min-width="240" show-overflow-tooltip />
      <el-table-column prop="createTime" label="时间" width="160" />
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag v-if="row.isRead === 0" type="danger" size="small">未读</el-tag>
          <span v-else style="color: #909399; font-size: 12px;">已读</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="80">
        <template #default="{ row }">
          <el-button v-if="row.isRead === 0" size="small" @click="markRead(row.id)">标为已读</el-button>
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
const unread = ref(0)
const page = ref(1), size = ref(10), total = ref(0)

const fetchData = async () => {
  try {
    const [listRes, countRes] = await Promise.all([
      request.get('/notification/list', { params: { page: page.value, size: size.value } }),
      request.get('/notification/unread-count')
    ])
    tableData.value = listRes.records || []
    total.value = listRes.total || 0
    unread.value = countRes ?? 0
  } catch (e) { tableData.value = []; total.value = 0; unread.value = 0 }
}

const typeText = (t) => ({ customer_assign: '客户分配', work_order: '工单', system: '系统' }[t] || t)
const typeTag = (t) => ({ customer_assign: 'primary', work_order: 'warning', system: 'info' }[t] || 'info')

const markRead = async (id) => { await request.put(`/notification/read/${id}`); fetchData() }
const markAllRead = async () => { await request.put('/notification/read-all'); ElMessage.success('全部已读'); fetchData() }

onMounted(fetchData)
</script>
