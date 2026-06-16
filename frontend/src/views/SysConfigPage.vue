<template>
  <div>
    <h2>系统配置</h2>
    <el-tabs v-model="activeTab">
      <!-- 系统参数 -->
      <el-tab-pane label="系统参数" name="config">
        <el-button type="primary" @click="handleAddConfig">新增参数</el-button>
        <el-table :data="configs" border style="margin-top: 10px">
          <template #empty><div style="padding:40px; color:#909399;">暂无系统参数</div></template>
          <el-table-column prop="configKey" label="参数键" width="200" />
          <el-table-column prop="configValue" label="参数值" min-width="200" />
          <el-table-column prop="description" label="说明" min-width="180" />
          <el-table-column label="操作" width="160">
            <template #default="{ row }">
              <el-button size="small" @click="handleEditConfig(row)">编辑</el-button>
              <el-button size="small" type="danger" @click="handleDeleteConfig(row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- 区域管理 -->
      <el-tab-pane label="区域管理" name="region">
        <el-button type="primary" @click="handleAddRegion">新增区域</el-button>
        <el-table :data="regions" border row-key="id" style="margin-top: 10px" default-expand-all>
          <template #empty><div style="padding:40px; color:#909399;">暂无区域数据</div></template>
          <el-table-column prop="name" label="区域名称" min-width="180" />
          <el-table-column label="级别" width="80">
            <template #default="{ row }"><el-tag size="small">{{ levelText(row.level) }}</el-tag></template>
          </el-table-column>
          <el-table-column label="操作" width="160">
            <template #default="{ row }">
              <el-button size="small" @click="handleEditRegion(row)">编辑</el-button>
              <el-button size="small" type="danger" @click="handleDeleteRegion(row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <!-- 参数弹窗 -->
    <el-dialog v-model="configDialogVisible" :title="configDialogTitle" width="450px">
      <el-form :model="configForm" label-width="80px">
        <el-form-item label="参数键" required><el-input v-model="configForm.configKey" /></el-form-item>
        <el-form-item label="参数值"><el-input v-model="configForm.configValue" /></el-form-item>
        <el-form-item label="说明"><el-input v-model="configForm.description" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="configDialogVisible = false">取消</el-button><el-button type="primary" @click="saveConfig">保存</el-button></template>
    </el-dialog>

    <!-- 区域弹窗 -->
    <el-dialog v-model="regionDialogVisible" :title="regionDialogTitle" width="450px">
      <el-form :model="regionForm" label-width="80px">
        <el-form-item label="名称" required><el-input v-model="regionForm.name" /></el-form-item>
        <el-form-item label="级别">
          <el-select v-model="regionForm.level"><el-option label="省" :value="1" /><el-option label="市" :value="2" /><el-option label="区" :value="3" /></el-select>
        </el-form-item>
        <el-form-item label="上级ID"><el-input-number v-model="regionForm.parentId" :min="0" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="regionDialogVisible = false">取消</el-button><el-button type="primary" @click="saveRegion">保存</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../api/request'

const activeTab = ref('config')
const configs = ref([])
const regions = ref([])
const configDialogVisible = ref(false), configDialogTitle = ref('')
const configForm = ref({ id: null, configKey: '', configValue: '', description: '' })
const regionDialogVisible = ref(false), regionDialogTitle = ref('')
const regionForm = ref({ id: null, name: '', level: 1, parentId: 0 })

const levelText = (l) => ({ 1: '省', 2: '市', 3: '区' }[l] || l)

// ========== 系统参数 ==========
const fetchConfigs = async () => { const res = await request.get('/sysconfig/configs'); configs.value = res || [] }
const handleAddConfig = () => {
  configDialogTitle.value = '新增参数'; configForm.value = { id: null, configKey: '', configValue: '', description: '' }; configDialogVisible.value = true
}
const handleEditConfig = (row) => {
  configDialogTitle.value = '编辑参数'; configForm.value = { ...row }; configDialogVisible.value = true
}
const saveConfig = async () => {
  if (!configForm.value.configKey?.trim()) { ElMessage.warning('参数键不能为空'); return }
  try {
    if (configForm.value.id) { await request.put('/sysconfig/config', configForm.value) }
    else { await request.post('/sysconfig/config', configForm.value) }
    ElMessage.success('保存成功'); configDialogVisible.value = false; fetchConfigs()
  } catch (e) { console.error(e) }
}
const handleDeleteConfig = (id) => {
  ElMessageBox.confirm('确定删除？', '提示', { type: 'warning' }).then(async () => {
    await request.delete(`/sysconfig/config/${id}`); ElMessage.success('删除成功'); fetchConfigs()
  }).catch(() => {})
}

// ========== 区域管理 ==========
const fetchRegions = async () => { const res = await request.get('/sysconfig/regions'); regions.value = res || [] }
const handleAddRegion = () => {
  regionDialogTitle.value = '新增区域'; regionForm.value = { id: null, name: '', level: 1, parentId: 0 }; regionDialogVisible.value = true
}
const handleEditRegion = (row) => {
  regionDialogTitle.value = '编辑区域'; regionForm.value = { ...row }; regionDialogVisible.value = true
}
const saveRegion = async () => {
  if (!regionForm.value.name?.trim()) { ElMessage.warning('名称不能为空'); return }
  try {
    if (regionForm.value.id) { await request.put('/sysconfig/region', regionForm.value) }
    else { await request.post('/sysconfig/region', regionForm.value) }
    ElMessage.success('保存成功'); regionDialogVisible.value = false; fetchRegions()
  } catch (e) { console.error(e) }
}
const handleDeleteRegion = (id) => {
  ElMessageBox.confirm('确定删除？', '提示', { type: 'warning' }).then(async () => {
    await request.delete(`/sysconfig/region/${id}`); ElMessage.success('删除成功'); fetchRegions()
  }).catch(() => {})
}

onMounted(() => { fetchConfigs(); fetchRegions() })
</script>
