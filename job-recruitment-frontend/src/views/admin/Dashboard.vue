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
          <v-chart class="chart" :option="applicationStatusChartOption" autoresize />
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <div class="chart-header">
              <span>热门城市TOP10</span>
            </div>
          </template>
          <v-chart class="chart" :option="cityChartOption" autoresize />
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

const chartColors = ['#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de', '#3ba272', '#fc8452', '#9a60b4', '#ea7ccc', '#6b7280']
const chartDecals = [
  { symbol: 'rect', dashArrayX: [1, 0], dashArrayY: [6, 3], rotation: 0 },
  { symbol: 'rect', dashArrayX: [6, 3], dashArrayY: [1, 0], rotation: 0 },
  { symbol: 'rect', dashArrayX: [1, 0], dashArrayY: [5, 3], rotation: Math.PI / 4 },
  { symbol: 'circle', dashArrayX: [1, 0], dashArrayY: [5, 5], symbolSize: 1.5 },
  { symbol: 'triangle', dashArrayX: [1, 0], dashArrayY: [6, 6], symbolSize: 2 },
  { symbol: 'diamond', dashArrayX: [1, 0], dashArrayY: [6, 6], symbolSize: 2 },
  { symbol: 'rect', dashArrayX: [8, 4], dashArrayY: [8, 4], rotation: Math.PI / 4 },
  { symbol: 'circle', dashArrayX: [2, 4], dashArrayY: [2, 4], symbolSize: 2 },
  { symbol: 'rect', dashArrayX: [2, 3], dashArrayY: [8, 2], rotation: 0 },
  { symbol: 'triangle', dashArrayX: [4, 4], dashArrayY: [4, 4], rotation: Math.PI / 6 }
]

const withPrintableStyle = (items = []) => items.map((item, index) => ({
  ...(typeof item === 'object' && item !== null ? item : { value: item }),
  itemStyle: {
    color: chartColors[index % chartColors.length],
    decal: {
      color: 'rgba(17, 24, 39, 0.28)',
      backgroundColor: 'transparent',
      ...chartDecals[index % chartDecals.length]
    },
    borderColor: '#111827',
    borderWidth: 1.5
  }
}))

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
  legend: { bottom: 0, left: 'center', itemWidth: 18, itemHeight: 12 },
  series: [{
    type: 'pie',
    radius: ['28%', '66%'],
    center: ['50%', '43%'],
    roseType: 'radius',
    label: {
      show: true,
      color: '#1f2937',
      fontWeight: 600,
      formatter: '{b}\n{c} ({d}%)'
    },
    labelLine: {
      length: 12,
      length2: 10,
      lineStyle: { color: '#6b7280' }
    },
    emphasis: {
      scaleSize: 8,
      label: { fontWeight: 700 }
    },
    data: []
  }]
})

// 热门城市TOP10柱状图
const cityChartOption = ref({
  tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
  grid: { left: '4%', right: '12%', top: '8%', bottom: '6%', containLabel: true },
  xAxis: {
    type: 'value',
    splitLine: { lineStyle: { type: 'dashed', color: '#d1d5db' } }
  },
  yAxis: {
    type: 'category',
    data: [],
    inverse: true,
    axisTick: { show: false },
    axisLabel: { color: '#1f2937', fontWeight: 600 }
  },
  series: [{
    type: 'bar',
    barWidth: 16,
    data: [],
    label: {
      show: true,
      position: 'right',
      color: '#111827',
      fontWeight: 700,
      formatter: '{c}'
    }
  }]
})

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
    applicationStatusChartOption.value.series[0].data = withPrintableStyle(data.applicationStatusDistribution || [])
    
    // 更新热门城市
    cityChartOption.value.yAxis.data = data.topCities?.cities || []
    cityChartOption.value.series[0].data = withPrintableStyle((data.topCities?.counts || []).map((count) => Number(count || 0)))
    
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

</style>
