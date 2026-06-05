<template>
  <div>
    <h2>业务管理</h2>
    <el-button type="primary" @click="handleAdd" v-if="userStore.role === 'admin'">新增业务</el-button>
    <el-button @click="handleExport">导出Excel</el-button>
    <el-upload
      action="/api/business/import"
      :show-file-list="false"
      :on-success="handleImportSuccess"
      :before-upload="beforeUpload"
      style="display: inline-block; margin-left: 10px"
      v-if="userStore.role === 'admin'"
    >
      <el-button type="success">导入Excel</el-button>
    </el-upload>

    <div style="margin-top: 15px; display: flex; gap: 10px; align-items: center">
      <el-input v-model="searchKeyword" placeholder="搜索业务名称/类型" style="width: 240px" clearable @clear="fetchData" @keyup.enter="fetchData" />
      <el-select v-model="searchStatus" placeholder="状态筛选" style="width: 130px" clearable @clear="fetchData">
        <el-option label="进行中" value="进行中" />
        <el-option label="已完成" value="已完成" />
        <el-option label="暂停" value="暂停" />
      </el-select>
      <el-button type="primary" @click="fetchData">搜索</el-button>
    </div>

    <el-table :data="tableData" border style="margin-top: 20px">
      <template #empty>
        <div style="padding: 40px 0; color: #909399;">
          <span style="font-size: 48px;">📋</span>
          <p style="margin-top: 8px;">暂无业务数据</p>
          <p style="font-size: 12px; color: #c0c4cc;">请先确保客户数据存在，再添加业务</p>
        </div>
      </template>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="业务名称" />
      <el-table-column prop="type" label="业务类型" />
      <el-table-column prop="price" label="金额" />
      <el-table-column prop="status" label="状态" />
      <el-table-column label="所属客户">

        <template #default="{ row }">
          {{ getCustomerName(row.customerId) }}
        </template>

      </el-table-column>
      <el-table-column label="操作" width="180" v-if="userStore.role === 'admin'">
        <template #default="{ row }">
          <el-button size="small" @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[5, 10, 20, 50]"
            :total="total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            />

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="业务名称" required>
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="业务类型">
          <el-input v-model="form.type" />
        </el-form-item>
        <el-form-item label="金额">
          <el-input-number v-model="form.price" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status">
            <el-option label="进行中" value="进行中" />
            <el-option label="已完成" value="已完成" />
            <el-option label="暂停" value="暂停" />
          </el-select>
        </el-form-item>
        <el-form-item label="所属客户">
          <el-select v-model="form.customerId" filterable placeholder="请选择客户">
            <el-option
              v-for="c in customerList"
              :key="c.id"
              :label="c.name"
              :value="c.id"
            />
          </el-select>
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
import { useUserStore } from '../stores/user'

const userStore = useUserStore()
const tableData = ref([])
const searchKeyword = ref('')
const searchStatus = ref('')
const customerList = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const form = ref({
  id: null,
  name: '',
  type: '',
  price: 0,
  status: '进行中',
  customerId: null
})

// 获取业务列表
const fetchData = async () => {
  try {
    const params = {
      keyword: searchKeyword.value || undefined,
      status: searchStatus.value || undefined,
      page: currentPage.value,
      size: pageSize.value
    }
    const res = await request.get('/business/search', { params })
    tableData.value = res.records || []
    total.value = res.total || 0
    // 数据为空时给出友好提示
    if (res.total === 0 && !searchKeyword.value && !searchStatus.value) {
      console.log('[BusinessPage] 业务表暂无数据，请先确保客户和业务数据已初始化')
    }
  } catch (e) {
    console.error('[BusinessPage] fetchData 异常:', e)
    tableData.value = []
    total.value = 0
  }
}

// 获取客户列表（用于下拉）
const fetchCustomers = async () => {
  const res = await request.get('/customer/list')
  customerList.value = res
}

// 根据客户ID获取名称
const getCustomerName = (id) => {
  const customer = customerList.value.find(c => c.id === id)
  return customer ? customer.name : '—'
}

// 新增
const handleAdd = () => {
  dialogTitle.value = '新增业务'
  form.value = {
    id: null,
    name: '',
    type: '',
    price: 0,
    status: '进行中',
    customerId: null
  }
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row) => {
  dialogTitle.value = '编辑业务'
  form.value = { ...row }
  dialogVisible.value = true
}

// 保存
const save = async () => {
  if (!form.value.name || !form.value.name.trim()) {
    ElMessage.warning('业务名称不能为空')
    return
  }
  if (!form.value.customerId) {
    ElMessage.warning('请选择所属客户')
    return
  }
  if (form.value.price != null && form.value.price < 0) {
    ElMessage.warning('金额不能为负数')
    return
  }
  try {
    if (form.value.id) {
      await request.put('/business/update', form.value)
      ElMessage.success('更新成功')
    } else {
      await request.post('/business/add', form.value)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch (error) {
    console.error(error)
  }
}

// 删除
const handleDelete = (id) => {
  ElMessageBox.confirm('确定删除该业务吗？', '提示', { type: 'warning' }).then(async () => {
    await request.delete(`/business/delete/${id}`)
    ElMessage.success('删除成功')
    fetchData()
  }).catch(() => {
    // 取消删除或删除失败（拦截器已提示错误）
  })
}

// 导出Excel
const handleExport = async () => {
  try {
    const res = await request.get('/business/export', { responseType: 'blob' })
    // res 已经是 Blob（响应拦截器对非200 code 返回原 body）
    const blob = res instanceof Blob ? res : new Blob([res])
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', `业务数据_${new Date().toLocaleDateString()}.xlsx`)
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch {
    ElMessage.error('导出失败')
  }
}

// 导入成功回调
const handleImportSuccess = () => {
  ElMessage.success('导入成功')
  fetchData()
}

// 导入前校验
const beforeUpload = (file) => {
  const isExcel = file.type === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
  if (!isExcel) {
    ElMessage.error('只能上传 .xlsx 文件')
    return false
  }
  return true
}

const currentPage = ref(1)
const pageSize = ref(5)
const total = ref(0)

const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1
  fetchData()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchData()
}

onMounted(() => {
  fetchData()
  fetchCustomers()
})


</script>