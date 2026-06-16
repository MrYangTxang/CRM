<template>
  <div>
    <h2>待办事项</h2>
    <el-button type="primary" @click="handleAdd">新增待办</el-button>
    <div style="margin-top: 15px; display: flex; gap: 10px">
      <el-select v-model="filterStatus" placeholder="状态筛选" style="width: 130px" clearable @change="fetchData">
        <el-option label="待处理" value="pending" />
        <el-option label="已完成" value="done" />
      </el-select>
      <el-button type="primary" @click="fetchData">刷新</el-button>
    </div>
    <el-table :data="tableData" border style="margin-top: 15px">
      <template #empty>
        <div style="padding: 40px 0; color: #909399;">
          <span style="font-size: 48px;">✅</span>
          <p style="margin-top: 8px;">暂无待办事项</p>
        </div>
      </template>
      <el-table-column prop="title" label="标题" min-width="160" />
      <el-table-column label="优先级" width="80">
        <template #default="{ row }">
          <el-tag :type="priorityType(row.priority)" size="small">{{ row.priority }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 'done' ? 'success' : 'warning'" size="small">
            {{ row.status === 'done' ? '已完成' : '待处理' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="deadline" label="截止时间" width="160" />
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button v-if="row.status === 'pending'" size="small" type="success" @click="markDone(row.id)">完成</el-button>
          <el-button size="small" @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      v-model:current-page="page" v-model:page-size="size"
      :page-sizes="[5,10,20,50]" :total="total"
      layout="total, sizes, prev, pager, next, jumper"
      @size-change="fetchData" @current-change="fetchData" style="margin-top:15px" />

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="标题" required><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="内容"><el-input v-model="form.content" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="优先级">
          <el-select v-model="form.priority"><el-option label="高" value="高" /><el-option label="中" value="中" /><el-option label="低" value="低" /></el-select>
        </el-form-item>
        <el-form-item label="截止时间">
          <el-date-picker v-model="form.deadline" type="datetime" placeholder="选择截止时间" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../api/request'

const tableData = ref([])
const filterStatus = ref('')
const dialogVisible = ref(false)
const dialogTitle = ref('')
const form = ref({ id: null, title: '', content: '', priority: '中', deadline: null })
const page = ref(1), size = ref(10), total = ref(0)

const fetchData = async () => {
  try {
    const res = await request.get('/todo/search', {
      params: { status: filterStatus.value || undefined, page: page.value, size: size.value }
    })
    tableData.value = res.records || []
    total.value = res.total || 0
  } catch (e) { tableData.value = []; total.value = 0 }
}

const priorityType = (p) => ({ '高': 'danger', '中': 'warning', '低': 'info' }[p] || 'info')

const handleAdd = () => {
  dialogTitle.value = '新增待办'; form.value = { id: null, title: '', content: '', priority: '中', deadline: null }
  dialogVisible.value = true
}
const handleEdit = (row) => {
  dialogTitle.value = '编辑待办'; form.value = { ...row }
  dialogVisible.value = true
}
const save = async () => {
  if (!form.value.title?.trim()) { ElMessage.warning('标题不能为空'); return }
  try {
    if (form.value.id) { await request.put('/todo/update', form.value) }
    else { await request.post('/todo/add', form.value) }
    ElMessage.success('保存成功'); dialogVisible.value = false; fetchData()
  } catch (e) { console.error(e) }
}
const markDone = async (id) => {
  await request.put(`/todo/done/${id}`); ElMessage.success('已完成'); fetchData()
}
const handleDelete = (id) => {
  ElMessageBox.confirm('确定删除？', '提示', { type: 'warning' }).then(async () => {
    await request.delete(`/todo/delete/${id}`); ElMessage.success('删除成功'); fetchData()
  }).catch(() => {})
}
onMounted(fetchData)
</script>
