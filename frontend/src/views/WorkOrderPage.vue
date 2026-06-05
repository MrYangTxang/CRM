<template>
  <div>
    <h2>工单管理</h2>
    <el-button type="primary" @click="handleCreate">创建工单</el-button>

    <div style="margin-top: 15px; display: flex; gap: 10px; align-items: center">
      <el-input v-model="searchKeyword" placeholder="搜索工单号/区域" style="width: 240px" clearable @clear="fetchOrders" @keyup.enter="fetchOrders" />
      <el-select v-model="searchStatus" placeholder="状态筛选" style="width: 140px" clearable @clear="fetchOrders">
        <el-option label="已创建" value="created" />
        <el-option label="处理中" value="processing" />
        <el-option label="已完成" value="completed" />
        <el-option label="已回单" value="returned" />
        <el-option label="已退单" value="cancelled" />
      </el-select>
      <el-button type="primary" @click="fetchOrders">搜索</el-button>
    </div>

    <el-table :data="tableData" border style="margin-top: 20px">
      <template #empty>
        <div style="padding: 40px 0; color: #909399;">
          <span style="font-size: 48px;">📝</span>
          <p style="margin-top: 8px;">暂无工单数据</p>
          <p style="font-size: 12px; color: #c0c4cc;">请先创建客户和业务，再创建工单</p>
        </div>
      </template>
      <el-table-column prop="orderNo" label="工单号" width="180" />
      <el-table-column label="客户">
        <template #default="{ row }">
          {{ getCustomerName(row.customerId) }}
        </template>
      </el-table-column>
      <el-table-column label="业务">
        <template #default="{ row }">
          {{ getBusinessName(row.businessId) }}
        </template>
      </el-table-column>
      <el-table-column prop="region" label="区域" />
      <el-table-column label="优先级" width="80">
        <template #default="{ row }">
          <el-tag :type="priorityTagType(row.priority)" size="small">{{ row.priority || '中' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="statusTagType(row.status)">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="160" />
      <el-table-column label="操作" width="300">
        <template #default="{ row }">
          <el-button v-if="row.status === 'created'" size="small" type="primary" @click="handleStart(row.id)">接单</el-button>
          <el-button v-if="row.status === 'processing'" size="small" type="success" @click="handleComplete(row.id)">完成</el-button>
          <el-button size="small" :disabled="!canReturn(row.status)" @click="handleReturn(row.id)">回单</el-button>
          <el-button size="small" type="danger" :disabled="row.status === 'completed'" @click="handleCancel(row.id)">退单</el-button>
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

    <!-- 创建工单弹窗 -->
    <el-dialog v-model="dialogVisible" title="创建工单" width="500px">
      <el-form :model="newOrder" label-width="80px">
        <el-form-item label="客户" required>
          <el-select v-model="newOrder.customerId" filterable placeholder="请选择客户" @change="onCustomerChange">
            <el-option v-for="c in customerList" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="业务" required>
          <el-select v-model="newOrder.businessId" filterable placeholder="请选择业务">
            <el-option v-for="b in filteredBusinessList" :key="b.id" :label="b.name" :value="b.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="区域" required>
          <el-input v-model="newOrder.region" placeholder="请输入区域" />
        </el-form-item>
        <el-form-item label="优先级">
          <el-select v-model="newOrder.priority" placeholder="请选择优先级" style="width: 100%">
            <el-option label="高" value="高" />
            <el-option label="中" value="中" />
            <el-option label="低" value="低" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="createOrder">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../api/request'

const tableData = ref([])
const customerList = ref([])
const businessList = ref([])
const searchKeyword = ref('')
const searchStatus = ref('')
const dialogVisible = ref(false)
const newOrder = ref({
  customerId: null,
  businessId: null,
  region: '',
  priority: '中'
})

const fetchOrders = async () => {
  try {
    const res = await request.get('/workorder/search', {
      params: {
        keyword: searchKeyword.value || undefined,
        status: searchStatus.value || undefined,
        page: currentPage.value,
        size: pageSize.value
      }
    })
    tableData.value = res.records || []
    total.value = res.total || 0
  } catch (e) {
    console.error('[WorkOrderPage] fetchOrders 异常:', e)
    tableData.value = []
    total.value = 0
  }
}

const fetchCustomers = async () => {
  const res = await request.get('/customer/list')
  customerList.value = res
}

const fetchBusinesses = async () => {
  const res = await request.get('/business/list')
  businessList.value = res
}

const filteredBusinessList = ref([])
const onCustomerChange = (customerId) => {
  filteredBusinessList.value = businessList.value.filter(b => b.customerId === customerId)
  newOrder.value.businessId = null
}

const getCustomerName = (id) => {
  const c = customerList.value.find(c => c.id === id)
  return c ? c.name : '—'
}
const getBusinessName = (id) => {
  const b = businessList.value.find(b => b.id === id)
  return b ? b.name : '—'
}

// ---------- 状态显示 ----------
const statusText = (status) => {
  const map = { created: '已创建', processing: '处理中', completed: '已完成', returned: '已回单', cancelled: '已退单' }
  return map[status] || status
}
const statusTagType = (status) => {
  const map = { created: 'info', processing: 'warning', completed: 'success', returned: 'success', cancelled: 'danger' }
  return map[status] || 'info'
}
const priorityTagType = (priority) => {
  const map = { '高': 'danger', '中': 'warning', '低': 'info' }
  return map[priority] || 'info'
}

// 只有 created/processing 状态可以回单
const canReturn = (status) => {
  return status === 'created' || status === 'processing'
}

const createOrder = async () => {
  if (!newOrder.value.customerId || !newOrder.value.businessId || !newOrder.value.region) {
    ElMessage.warning('请完整填写表单')
    return
  }
  try {
    await request.post('/workorder/create', newOrder.value)
    ElMessage.success('工单创建成功')
    dialogVisible.value = false
    newOrder.value = { customerId: null, businessId: null, region: '', priority: '中' }
    fetchOrders()
  } catch (error) {
    console.error(error)
  }
}

// 接单
const handleStart = async (id) => {
  try {
    await request.put(`/workorder/start/${id}`)
    ElMessage.success('接单成功')
    fetchOrders()
  } catch (error) {
    console.error(error)
  }
}

// 完成
const handleComplete = async (id) => {
  try {
    await request.put(`/workorder/complete/${id}`)
    ElMessage.success('工单已完成')
    fetchOrders()
  } catch (error) {
    console.error(error)
  }
}

// 回单
const handleReturn = async (id) => {
  try {
    await request.put(`/workorder/return/${id}`)
    ElMessage.success('回单成功')
    fetchOrders()
  } catch (error) {
    console.error(error)
  }
}

// 退单
const handleCancel = async (id) => {
  try {
    await request.put(`/workorder/cancel/${id}`)
    ElMessage.success('退单成功')
    fetchOrders()
  } catch (error) {
    console.error(error)
  }
}

const currentPage = ref(1)
const pageSize = ref(5)
const total = ref(0)

const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1
  fetchOrders()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchOrders()
}

onMounted(() => {
  fetchOrders()
  fetchCustomers()
  fetchBusinesses()
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
</style>
