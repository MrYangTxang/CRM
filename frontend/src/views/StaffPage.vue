<template>
  <div>
    <h2>人员管理</h2>
    <el-button type="primary" @click="handleAdd" v-if="userStore.role === 'admin'">新增人员</el-button>

    <div style="margin-top: 15px; display: flex; gap: 10px; align-items: center">
      <el-input v-model="searchKeyword" placeholder="搜索姓名/用户名/电话" style="width: 240px" clearable @clear="fetchData" @keyup.enter="fetchData" />
      <el-select v-model="searchRole" placeholder="角色筛选" style="width: 150px" clearable @clear="fetchData">
        <el-option label="管理员" value="admin" />
        <el-option label="销售经理" value="sales_manager" />
        <el-option label="普通用户" value="employee" />
      </el-select>
      <el-button type="primary" @click="fetchData">搜索</el-button>
    </div>

    <el-table :data="tableData" border style="margin-top: 20px">
      <template #empty>
        <div style="padding: 40px 0; color: #909399;">
          <span style="font-size: 48px;">👤</span>
          <p style="margin-top: 8px;">暂无人员数据</p>
        </div>
      </template>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="姓名" />
      <el-table-column prop="username" label="用户名" />
      <el-table-column label="角色" width="140">
        <template #default="{ row }">
          <el-select
            v-model="row.role"
            size="small"
            :disabled="userStore.role !== 'admin' || row.username === userStore.username"
            @change="updateRole(row.id, row.role)"
            placeholder="请选择"
          >
            <el-option label="管理员" value="admin" />
            <el-option label="销售经理" value="sales_manager" />
            <el-option label="普通用户" value="employee" />
          </el-select>
        </template>
      </el-table-column>
      <el-table-column prop="phone" label="电话" />
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
          style="margin-top: 20px"
        />
    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="姓名" required>
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="用户名" required>
          <el-input v-model="form.username" />
        </el-form-item>
        <el-form-item label="密码" :required="!form.id">
          <el-input type="password" v-model="form.password" placeholder="编辑时留空则不修改密码" />
        </el-form-item>
        <el-form-item label="电话">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="角色" v-if="userStore.role === 'admin'">
          <el-select v-model="form.role">
            <el-option label="普通用户" value="employee" />
            <el-option label="销售经理" value="sales_manager" />
            <el-option label="管理员" value="admin" />
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
const searchRole = ref('')
const dialogVisible = ref(false)
const dialogTitle = ref('')
const form = ref({
  id: null,
  name: '',
  username: '',
  password: '',
  phone: '',
  role: 'employee'   // 默认普通用户
})

// 获取人员列表
const fetchData = async () => {
  try {
    const res = await request.get('/staff/search', {
      params: {
        keyword: searchKeyword.value || undefined,
        role: searchRole.value || undefined,
        page: currentPage.value,
        size: pageSize.value
      }
    })
    tableData.value = res.records || []
    total.value = res.total || 0
  } catch (e) {
    console.error('[StaffPage] fetchData 异常:', e)
    tableData.value = []
    total.value = 0
  }
}

// 新增
const handleAdd = () => {
  dialogTitle.value = '新增人员'
  form.value = {
    id: null,
    name: '',
    username: '',
    password: '',
    phone: '',
    role: 'employee'
  }
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row) => {
  dialogTitle.value = '编辑人员'
  form.value = { ...row, password: '' }  // 密码字段清空
  dialogVisible.value = true
}

// 保存
const save = async () => {
  if (!form.value.name || !form.value.username) {
    ElMessage.warning('姓名和用户名不能为空')
    return
  }
  if (!form.value.id && !form.value.password) {
    ElMessage.warning('密码不能为空')
    return
  }

  try {
    if (form.value.id) {
      // 更新
      await request.put('/staff/update', form.value)
      ElMessage.success('更新成功')
    } else {
      // 新增
      await request.post('/staff/add', form.value)
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
  ElMessageBox.confirm('确定删除该人员吗？', '提示', { type: 'warning' }).then(async () => {
    await request.delete(`/staff/delete/${id}`)
    ElMessage.success('删除成功')
    fetchData()
  }).catch(() => {
    // 取消删除或删除失败（拦截器已提示错误）
  })
}

// 更新角色（即时生效）
const updateRole = async (userId, newRole) => {
  try {
    await request.put(`/staff/role/${userId}?role=${newRole}`)
    ElMessage.success('角色更新成功')
    // 刷新表格以获取最新数据（确保显示一致）
    fetchData()
  } catch (error) {
    console.error(error)
    fetchData() // 刷新还原显示
  }
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
})
</script>