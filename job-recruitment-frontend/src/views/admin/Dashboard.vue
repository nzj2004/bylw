<template>
  <div class="admin-dashboard">
    <!-- 顶部统计卡片 -->
    <el-row :gutter="20" class="stat-cards">
      <el-col :span="6">
        <el-card class="stat-card blue">
          <div class="stat-icon">
            <el-icon><User /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.totalUsers }}</div>
            <div class="stat-label">总用户数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card orange">
          <div class="stat-icon">
            <el-icon><OfficeBuilding /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.totalCompanies }}</div>
            <div class="stat-label">企业数量</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card red">
          <div class="stat-icon">
            <el-icon><Briefcase /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.totalJobs }}</div>
            <div class="stat-label">职位总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card green">
          <div class="stat-icon">
            <el-icon><DocumentChecked /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.totalApplications }}</div>
            <div class="stat-label">投递总数</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <div class="chart-header">
              <span>用户角色分布</span>
            </div>
          </template>
          <v-chart class="chart" :option="roleChartOption" autoresize />
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <div class="chart-header">
              <span>近7天用户注册趋势</span>
            </div>
          </template>
          <v-chart class="chart" :option="userTrendChartOption" autoresize />
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="chart-row">
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <div class="chart-header">
              <span>职位类别分布</span>
            </div>
          </template>
          <v-chart class="chart" :option="jobCategoryChartOption" autoresize />
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <div class="chart-header">
              <span>近7天职位发布趋势</span>
            </div>
          </template>
          <v-chart class="chart" :option="jobTrendChartOption" autoresize />
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="chart-row">
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <div class="chart-header">
              <span>投递状态分布</span>
            </div>
          </template>
          <div class="print-list">
            <div
              v-for="item in applicationStatusRows"
              :key="item.name"
              class="print-row"
            >
              <div class="print-row-meta">
                <span class="print-row-name">{{ item.name }}</span>
                <span class="print-row-value">{{ item.value }} / {{ item.percent }}%</span>
              </div>
              <div class="print-track">
                <div class="print-fill" :style="{ width: item.percent + '%' }"></div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <div class="chart-header">
              <span>热门城市TOP10</span>
            </div>
          </template>
          <div class="rank-table">
            <div class="rank-head">
              <span>排名</span>
              <span>城市</span>
              <span>职位数</span>
              <span>占比</span>
            </div>
            <div
              v-for="item in cityRows"
              :key="item.city"
              class="rank-row"
            >
              <span class="rank-index">{{ item.rank }}</span>
              <span class="rank-city">{{ item.city }}</span>
              <span class="rank-count">{{ item.count }}</span>
              <span class="rank-percent">{{ item.percent }}%</span>
              <div class="rank-track">
                <div class="rank-fill" :style="{ width: item.percent + '%' }"></div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { User, OfficeBuilding, Briefcase, DocumentChecked } from '@element-plus/icons-vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { PieChart, BarChart, LineChart } from 'echarts/charts'
import { TitleComponent, TooltipComponent, LegendComponent, GridComponent } from 'echarts/components'
import VChart from 'vue-echarts'
import { getDashboardStatistics } from '../../api/statistics'
import { ElMessage } from 'element-plus'

// 注册 ECharts 组件
use([CanvasRenderer, PieChart, BarChart, LineChart, TitleComponent, TooltipComponent, LegendComponent, GridComponent])

const stats = ref({
  totalUsers: 0,
  totalCompanies: 0,
  totalJobs: 0,
  totalApplications: 0
})

const applicationStatusRows = ref([])
const cityRows = ref([])

// 用户角色分布饼图
const roleChartOption = ref({
  tooltip: { trigger: 'item' },
  legend: { bottom: '5%', left: 'center' },
  series: [{
    type: 'pie',
    radius: ['40%', '70%'],
    avoidLabelOverlap: false,
    itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 2 },
    label: { show: false, position: 'center' },
    emphasis: { label: { show: true, fontSize: 20, fontWeight: 'bold' } },
    labelLine: { show: false },
    data: []
  }]
})

// 用户注册趋势折线图
const userTrendChartOption = ref({
  tooltip: { trigger: 'axis' },
  xAxis: { type: 'category', data: [] },
  yAxis: { type: 'value' },
  series: [{
    data: [],
    type: 'line',
    smooth: true,
    areaStyle: {
      color: {
        type: 'linear', x: 0, y: 0, x2: 0, y2: 1,
        colorStops: [
          { offset: 0, color: 'rgba(64, 158, 255, 0.5)' },
          { offset: 1, color: 'rgba(64, 158, 255, 0.1)' }
        ]
      }
    },
    itemStyle: { color: '#409eff' }
  }]
})

// 职位类别分布饼图
const jobCategoryChartOption = ref({
  tooltip: { trigger: 'item' },
  legend: { orient: 'vertical', left: 'left' },
  series: [{
    type: 'pie',
    radius: '60%',
    data: [],
    emphasis: { itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0, 0, 0, 0.5)' } }
  }]
})

// 职位发布趋势柱状图
const jobTrendChartOption = ref({
  tooltip: { trigger: 'axis' },
  xAxis: { type: 'category', data: [] },
  yAxis: { type: 'value' },
  series: [{
    data: [],
    type: 'bar',
    itemStyle: { color: '#67c23a', borderRadius: [5, 5, 0, 0] }
  }]
})

// 投递状态分布饼图
const applicationStatusChartOption = ref({
  tooltip: { trigger: 'item' },
  legend: { bottom: '5%', left: 'center' },
  series: [{
    type: 'pie',
    radius: ['40%', '70%'],
    itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 2 },
    data: []
  }]
})

// 热门城市TOP10柱状图
const cityChartOption = ref({
  tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
  grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
  xAxis: { type: 'value' },
  yAxis: { type: 'category', data: [] },
  series: [{
    type: 'bar',
    data: [],
    itemStyle: {
      color: {
        type: 'linear', x: 0, y: 0, x2: 1, y2: 0,
        colorStops: [
          { offset: 0, color: '#91cc75' },
          { offset: 1, color: '#5470c6' }
        ]
      },
      borderRadius: [0, 5, 5, 0]
    }
  }]
})

const toPercentRows = (items = []) => {
  const total = items.reduce((sum, item) => sum + Number(item.value || 0), 0)
  return items.map((item) => ({
    ...item,
    percent: total > 0 ? Math.round((Number(item.value || 0) / total) * 100) : 0
  }))
}

const toCityRows = (cities = [], counts = []) => {
  const max = Math.max(...counts.map((count) => Number(count || 0)), 0)
  return cities.map((city, index) => {
    const count = Number(counts[index] || 0)
    return {
      rank: index + 1,
      city,
      count,
      percent: max > 0 ? Math.round((count / max) * 100) : 0
    }
  })
}

const fetchStats = async () => {
  try {
    const res = await getDashboardStatistics()
    const data = res.data
    
    // 更新基础统计数据
    stats.value = {
      totalUsers: data.totalUsers,
      totalCompanies: data.totalCompanies,
      totalJobs: data.totalJobs,
      totalApplications: data.totalApplications
    }
    
    // 更新用户角色分布
    roleChartOption.value.series[0].data = data.roleDistribution
    
    // 更新用户注册趋势
    userTrendChartOption.value.xAxis.data = data.userTrend.dates
    userTrendChartOption.value.series[0].data = data.userTrend.counts
    
    // 更新职位类别分布
    jobCategoryChartOption.value.series[0].data = data.jobCategoryDistribution
    
    // 更新职位发布趋势
    jobTrendChartOption.value.xAxis.data = data.jobTrend.dates
    jobTrendChartOption.value.series[0].data = data.jobTrend.counts
    
    // 更新投递状态分布
    applicationStatusRows.value = toPercentRows(data.applicationStatusDistribution || [])
    
    // 更新热门城市
    cityRows.value = toCityRows(data.topCities?.cities || [], data.topCities?.counts || [])
    
  } catch (error) {
    console.error(error)
    ElMessage.error('获取统计数据失败')
  }
}

onMounted(() => {
  fetchStats()
})
</script>

<style scoped>
.admin-dashboard {
  max-width: 1400px;
  margin: 0 auto;
}

.stat-cards {
  margin-bottom: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 20px;
  color: #fff;
  border: none;
}

.stat-card.blue { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
.stat-card.orange { background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); }
.stat-card.red { background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); }
.stat-card.green { background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%); }

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20px;
}

.stat-icon .el-icon {
  font-size: 30px;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  opacity: 0.9;
}

.chart-row {
  margin-bottom: 20px;
}

.chart-card {
  height: 400px;
}

.chart-header {
  font-weight: bold;
  font-size: 16px;
}

.chart {
  width: 100%;
  height: 320px;
}

.print-list {
  padding: 14px 8px 0;
}

.print-row {
  margin-bottom: 14px;
}

.print-row-meta {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 6px;
  color: #1f2937;
  font-size: 14px;
}

.print-row-name {
  font-weight: 600;
}

.print-row-value {
  font-family: Consolas, "Courier New", monospace;
}

.print-track,
.rank-track {
  height: 10px;
  border: 1px solid #111827;
  background: repeating-linear-gradient(
    45deg,
    #fff,
    #fff 4px,
    #e5e7eb 4px,
    #e5e7eb 8px
  );
}

.print-fill,
.rank-fill {
  height: 100%;
  background: #111827;
}

.rank-table {
  padding-top: 8px;
  font-size: 14px;
}

.rank-head,
.rank-row {
  display: grid;
  grid-template-columns: 52px 1fr 72px 64px;
  gap: 10px;
  align-items: center;
}

.rank-head {
  padding: 8px 0;
  color: #374151;
  font-weight: 700;
  border-bottom: 2px solid #111827;
}

.rank-row {
  padding: 8px 0;
  border-bottom: 1px solid #d1d5db;
}

.rank-index,
.rank-count,
.rank-percent {
  font-family: Consolas, "Courier New", monospace;
}

.rank-city {
  font-weight: 600;
}

.rank-track {
  grid-column: 2 / 5;
  height: 8px;
}

@media print {
  .print-track,
  .rank-track {
    border-color: #000;
    background: #fff;
  }

  .print-fill,
  .rank-fill {
    background: #000;
  }
}
</style>
