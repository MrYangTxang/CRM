<template>
  <div>
    <h2>个人工作台</h2>

    <!-- 统计卡片 -->
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%)">👥</div>
          <div class="stat-info">
            <div class="stat-value">{{ dashboard.customerCount }}</div>
            <div class="stat-label">客户总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%)">📝</div>
          <div class="stat-info">
            <div class="stat-value">{{ dashboard.pendingOrderCount }}</div>
            <div class="stat-label">待处理工单</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)">📅</div>
          <div class="stat-info">
            <div class="stat-value">{{ dashboard.todayFollowUpCount }}</div>
            <div class="stat-label">今日跟进</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <span>客户地区分布统计</span>
          </template>
          <div ref="chartRef" style="width: 100%; height: 350px"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <span>最近跟进记录</span>
          </template>
          <div class="follow-up-scroll">
            <el-timeline>
              <el-timeline-item
                v-for="item in dashboard.recentFollowUps"
                :key="item.id"
                :timestamp="item.createTime"
                placement="top"
              >
                <p style="margin: 0">客户ID: {{ item.customerId }} — {{ item.content }}</p>
              </el-timeline-item>
            </el-timeline>
            <el-empty v-if="!dashboard.recentFollowUps || dashboard.recentFollowUps.length === 0" description="暂无跟进记录" />
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import request from '../api/request'

const chartRef = ref(null)
let chartInstance = null

const dashboard = reactive({
  customerCount: 0,
  pendingOrderCount: 0,
  todayFollowUpCount: 0,
  recentFollowUps: []
})

const fetchDashboard = async () => {
  try {
    const res = await request.get('/dashboard')
    if (res) {
      dashboard.customerCount = res.customerCount || 0
      dashboard.pendingOrderCount = res.pendingOrderCount || 0
      dashboard.todayFollowUpCount = res.todayFollowUpCount || 0
      dashboard.recentFollowUps = res.recentFollowUps || []
    }
  } catch (e) {
    console.error('[Dashboard] 获取仪表盘数据失败:', e)
  }
}

const fetchRegionStats = async () => {
  try {
    const res = await request.get('/customer/region-stats')
    if (res && res.length > 0) {
      renderChart(res)
    }
  } catch (e) {
    console.error('[Dashboard] 获取地区统计失败:', e)
  }
}

const renderChart = (data) => {
  if (!chartRef.value) return
  // 动态导入 ECharts
  import('echarts').then((echarts) => {
    if (chartInstance) chartInstance.dispose()
    chartInstance = echarts.init(chartRef.value)
    chartInstance.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: {
        type: 'category',
        data: data.map(d => d.name),
        axisLabel: { rotate: 30 }
      },
      yAxis: { type: 'value', name: '客户数量', minInterval: 1 },
      series: [{
        name: '客户数',
        type: 'bar',
        data: data.map(d => d.value),
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#667eea' },
            { offset: 1, color: '#764ba2' }
          ])
        },
        barMaxWidth: 50
      }],
      grid: { left: 60, right: 30, bottom: 80, top: 20 }
    })
  })
}

onMounted(() => {
  fetchDashboard()
  fetchRegionStats()
})
</script>

<style scoped>
.stat-card {
  display: flex;
  align-items: center;
}
.stat-card :deep(.el-card__body) {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 24px;
}
.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: #fff;
  flex-shrink: 0;
}
.stat-info {
  flex: 1;
}
.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #2c3e50;
}
.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

/* 跟进记录滚动容器 — 与左侧图表高度对齐 */
.follow-up-scroll {
  height: 350px;
  overflow-y: auto;
  padding-right: 8px;
}

.follow-up-scroll::-webkit-scrollbar {
  width: 5px;
}

.follow-up-scroll::-webkit-scrollbar-thumb {
  background: #dcdfe6;
  border-radius: 3px;
}

.follow-up-scroll::-webkit-scrollbar-thumb:hover {
  background: #c0c4cc;
}
</style>
