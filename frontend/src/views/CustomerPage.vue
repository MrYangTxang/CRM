<template>
  <div>
    <h2>客户管理</h2>
    <el-button type="primary" @click="handleAdd">新增客户</el-button>
    <el-button @click="handleExport">导出Excel</el-button>
    <el-upload
      action="/api/customer/import"
      :show-file-list="false"
      :on-success="handleImportSuccess"
      :before-upload="beforeUpload"
      style="display: inline-block; margin-left: 10px"
    >
      <el-button type="success">导入Excel</el-button>
    </el-upload>

    <div style="margin-top: 15px; display: flex; gap: 10px; align-items: center">
      <el-input v-model="searchKeyword" placeholder="搜索客户名称/联系人/电话/邮箱" style="width: 320px" clearable @clear="fetchData" @keyup.enter="fetchData" />
      <el-button type="primary" @click="fetchData">搜索</el-button>
      <el-divider direction="vertical" />
      <span style="font-size: 13px; color: #606266; white-space: nowrap">VIP筛选：</span>
      <el-radio-group v-model="vipFilter" size="small">
        <el-radio-button value="全部">全部</el-radio-button>
        <el-radio-button value="VIP3">VIP3</el-radio-button>
        <el-radio-button value="VIP2">VIP2</el-radio-button>
        <el-radio-button value="VIP1">VIP1</el-radio-button>
        <el-radio-button value="普通会员">普通会员</el-radio-button>
      </el-radio-group>
    </div>

    <el-table :data="displayData" border style="margin-top: 20px" :row-class-name="tableRowClassName" @sort-change="handleSortChange">
      <template #empty>
        <div style="padding: 40px 0; color: #909399;">
          <span style="font-size: 48px;">🏢</span>
          <p style="margin-top: 8px;">暂无客户数据</p>
          <p style="font-size: 12px; color: #c0c4cc;">请添加客户或检查系统初始化是否完成</p>
        </div>
      </template>
      <el-table-column prop="id" label="ID" width="80" sortable="custom" />
      <el-table-column prop="name" label="客户名称" min-width="140" />
      <el-table-column prop="vipLevel" label="VIP等级" width="110" sortable="custom">
        <template #default="{ row }">
          <span :class="['vip-tag', vipTagClass(row.vipLevel)]">
            {{ row.vipLevel || '普通会员' }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="标签" min-width="150">
        <template #default="{ row }">
          <span v-if="!row.tags" style="color: #c0c4cc; font-size: 12px">—</span>
          <el-tag v-for="tag in parseTags(row.tags)" :key="tag" size="small" style="margin: 2px">
            {{ tag }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="contactPerson" label="联系人" />
      <el-table-column prop="phone" label="电话" />
      <el-table-column prop="email" label="邮箱" />
      <el-table-column label="操作" width="260">
        <template #default="{ row }">
          <el-button size="small" @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" type="success" @click="showFollowUp(row)">跟进</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页组件 -->
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
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="550px">
      <el-form :model="form" :rules="rules" label-width="80px">
        <el-form-item label="客户名称" prop="name" required>
          <el-input v-model="form.name" placeholder="请输入客户名称" />
        </el-form-item>
        <el-form-item label="VIP等级">
          <el-select v-model="form.vipLevel" placeholder="请选择VIP等级" style="width: 100%">
            <el-option label="普通会员" value="普通会员" />
            <el-option label="VIP1" value="VIP1" />
            <el-option label="VIP2" value="VIP2" />
            <el-option label="VIP3" value="VIP3" />
          </el-select>
        </el-form-item>
        <el-form-item label="客户标签">
          <el-input v-model="form.tags" placeholder="多个标签用逗号分隔，如：重点客户,科技行业" />
        </el-form-item>
        <el-form-item label="联系人">
          <el-input v-model="form.contactPerson" placeholder="请输入联系人" />
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入11位手机号码" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱地址" />
        </el-form-item>
        <el-form-item label="地址">
          <el-input v-model="form.address" placeholder="请输入地址" />
        </el-form-item>
        <el-form-item v-if="userStore.role === 'admin'" label="负责人ID">
          <el-input-number v-model="form.salesPerson" :min="1" placeholder="请输入负责人ID" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>

    <!-- 跟进记录弹窗 -->
    <el-dialog v-model="followUpVisible" title="客户跟进记录" width="650px">
      <div style="margin-bottom: 16px">
        <el-input v-model="followUpContent" type="textarea" :rows="3" placeholder="请输入跟进内容..." />
        <el-button type="primary" style="margin-top: 10px" @click="addFollowUp" :loading="addingFollowUp">添加跟进</el-button>
      </div>
      <el-divider />
      <el-timeline v-if="followUps.length > 0">
        <el-timeline-item
          v-for="item in followUps"
          :key="item.id"
          :timestamp="item.createTime"
          placement="top"
        >
          <p style="margin: 0">{{ item.content }}</p>
        </el-timeline-item>
      </el-timeline>
      <el-empty v-else description="暂无跟进记录" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../api/request'
import { useUserStore } from '../stores/user'

const userStore = useUserStore()

const tableData = ref([])
const searchKeyword = ref('')
const dialogVisible = ref(false)
const dialogTitle = ref('')
const form = ref({ id: null, name: '', contactPerson: '', phone: '', email: '', address: '', vipLevel: '普通会员', tags: '', salesPerson: null })

// VIP 筛选与排序
const vipFilter = ref('全部')
const sortField = ref('')
const sortOrder = ref('')

// 跟进记录相关
const followUpVisible = ref(false)
const followUps = ref([])
const followUpContent = ref('')
const addingFollowUp = ref(false)
const currentCustomerId = ref(null)

const fetchData = async () => {
  try {
    const res = await request.get('/customer/search', {
      params: {
        keyword: searchKeyword.value || undefined,
        vipLevel: vipFilter.value !== '全部' ? vipFilter.value : undefined,
        page: currentPage.value,
        size: pageSize.value
      }
    })
    tableData.value = res.records || []
    total.value = res.total || 0
  } catch (e) {
    console.error('[CustomerPage] fetchData 异常:', e)
    tableData.value = []
    total.value = 0
  }
}

// 客户端排序（后端已按VIP分组 + ID升序，此处处理表头点击排序）
const vipOrder = { 'VIP3': 0, 'VIP2': 1, 'VIP1': 2, '普通会员': 3 }
const displayData = computed(() => {
  let list = [...tableData.value]
  if (sortField.value === 'id') {
    list.sort((a, b) => sortOrder.value === 'descending' ? b.id - a.id : a.id - b.id)
  } else if (sortField.value === 'vipLevel') {
    list.sort((a, b) => {
      const orderA = vipOrder[a.vipLevel] ?? 4
      const orderB = vipOrder[b.vipLevel] ?? 4
      const cmp = orderA - orderB || a.id - b.id
      return sortOrder.value === 'descending' ? -cmp : cmp
    })
  }
  // 默认保持后端顺序（VIP分组，同级ID升序）
  return list
})

const handleSortChange = ({ prop, order }) => {
  sortField.value = prop || ''
  sortOrder.value = order || ''
}

// 监听 VIP 筛选切换 → 重置页码并重新请求后端
watch(vipFilter, () => {
  sortField.value = ''
  sortOrder.value = ''
  currentPage.value = 1
  fetchData()
})

// VIP分组行样式
const tableRowClassName = ({ row }) => {
  const map = {
    'VIP3': 'vip-group-diamond',
    'VIP2': 'vip-group-gold',
    'VIP1': 'vip-group-silver'
  }
  return map[row.vipLevel] || 'vip-group-normal'
}

const handleAdd = () => {
  dialogTitle.value = '新增客户'
  form.value = { id: null, name: '', contactPerson: '', phone: '', email: '', address: '', vipLevel: '普通会员', tags: '', salesPerson: null }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑客户'
  form.value = { ...row }
  dialogVisible.value = true
}

const save = async () => {
  if (!form.value.name || !form.value.name.trim()) {
    ElMessage.warning('客户名称不能为空')
    return
  }
  if (form.value.phone && !/^1[3-9]\d{9}$/.test(form.value.phone)) {
    ElMessage.warning('请输入正确的11位手机号码')
    return
  }
  if (form.value.email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.value.email)) {
    ElMessage.warning('请输入正确的邮箱地址')
    return
  }
  try {
    if (form.value.id) {
      await request.put('/customer/update', form.value)
      ElMessage.success('更新成功')
    } else {
      await request.post('/customer/add', form.value)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch (error) {
    console.error(error)
  }
}

const handleDelete = (id) => {
  ElMessageBox.confirm('确定删除该客户吗？', '提示', { type: 'warning' }).then(async () => {
    await request.delete(`/customer/delete/${id}`)
    ElMessage.success('删除成功')
    fetchData()
  }).catch(() => {})
}

const handleExport = async () => {
  try {
    const res = await request.get('/customer/export', { responseType: 'blob' })
    const blob = res instanceof Blob ? res : new Blob([res])
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', `客户数据_${new Date().toLocaleDateString()}.xlsx`)
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch {
    ElMessage.error('导出失败')
  }
}

const handleImportSuccess = () => {
  ElMessage.success('导入成功')
  fetchData()
}

const beforeUpload = (file) => {
  const isExcel = file.type === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
  if (!isExcel) {
    ElMessage.error('只能上传 .xlsx 文件')
    return false
  }
  return true
}

// ---------- VIP 标签样式 ----------
const vipTagClass = (level) => {
  const map = {
    'VIP1': 'vip-silver', 'VIP会员': 'vip-silver',
    'VIP2': 'vip-gold', '金卡会员': 'vip-gold',
    'VIP3': 'vip-diamond', '钻石会员': 'vip-diamond'
  }
  return map[level] || 'vip-normal'
}

// ---------- 标签解析 ----------
const parseTags = (tags) => {
  if (!tags || !tags.trim()) return []
  return tags.split(',').map(t => t.trim()).filter(t => t)
}

// ---------- 跟进记录 ----------
const showFollowUp = async (row) => {
  currentCustomerId.value = row.id
  followUpVisible.value = true
  followUpContent.value = ''
  await loadFollowUps()
}

const loadFollowUps = async () => {
  try {
    const res = await request.get(`/followup/customer/${currentCustomerId.value}`)
    followUps.value = res || []
  } catch (e) {
    followUps.value = []
  }
}

const addFollowUp = async () => {
  if (!followUpContent.value.trim()) {
    ElMessage.warning('请输入跟进内容')
    return
  }
  addingFollowUp.value = true
  try {
    await request.post('/followup/add', {
      customerId: currentCustomerId.value,
      content: followUpContent.value
    })
    ElMessage.success('跟进记录添加成功')
    followUpContent.value = ''
    await loadFollowUps()
  } catch (e) {
    console.error(e)
  } finally {
    addingFollowUp.value = false
  }
}

// 分页相关
const currentPage = ref(1)
const pageSize = ref(20)
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

const rules = {
  name: [
    { required: true, message: '客户名称不能为空', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入11位手机号码', trigger: 'blur' }
  ],
  email: [
    { pattern: /^[^\s@]+@[^\s@]+\.[^\s@]+$/, message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
:deep(.el-table) {
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}
:deep(.el-table th) {
  background-color: #f8f9fc;
  color: #2c3e50;
  font-weight: 600;
}
:deep(.el-button--primary) {
  border-radius: 6px;
}
:deep(.el-button--danger) {
  border-radius: 6px;
}

/* ========== VIP 等级标签样式 ========== */
.vip-tag {
  display: inline-block;
  padding: 2px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.5px;
  white-space: nowrap;
}

/* 普通会员 — 低调灰 */
.vip-normal {
  background: #f5f5f5;
  color: #909399;
  border: 1px solid #e4e7ed;
}

/* VIP1 — 白银质感 */
.vip-silver {
  background: linear-gradient(135deg, #f0f2f5, #e0e4e8);
  color: #5a6c7d;
  border: 1px solid #bcc4ce;
  box-shadow: 0 1px 3px rgba(120, 130, 150, 0.2);
}

/* VIP2 — 尊贵金 */
.vip-gold {
  background: linear-gradient(135deg, #fff7e6, #ffe4b0);
  color: #b8860b;
  border: 1px solid #daa520;
  box-shadow: 0 1px 4px rgba(218, 165, 32, 0.3);
}

/* VIP3 — 黑金钻石（最高等级） */
.vip-diamond {
  background: linear-gradient(135deg, #1a1a2e, #2d2138);
  color: #f0c040;
  border: 1px solid #c8a030;
  box-shadow: 0 2px 6px rgba(200, 160, 48, 0.35);
  text-shadow: 0 0 6px rgba(240, 192, 64, 0.3);
}

/* ========== VIP 分组行背景色 ========== */
:deep(.vip-group-diamond) { background-color: #fdf6f0; }
:deep(.vip-group-gold)    { background-color: #fffef8; }
:deep(.vip-group-silver)  { background-color: #f8f9fc; }
:deep(.vip-group-normal)  { background-color: #fafafa; }

:deep(.vip-group-diamond:hover) { background-color: #faf0e4 !important; }
:deep(.vip-group-gold:hover)    { background-color: #fef9ee !important; }
:deep(.vip-group-silver:hover)  { background-color: #f0f2f6 !important; }
:deep(.vip-group-normal:hover)  { background-color: #f2f2f2 !important; }
</style>
