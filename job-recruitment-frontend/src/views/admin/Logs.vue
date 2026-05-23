<template>
  <div class="admin-logs">
    <el-card>
      <template #header>
        <div class="card-header">
          <h3>日志管理</h3>
          <el-button type="danger" @click="handleClear">清空日志</el-button>
        </div>
      </template>

      <!-- 搜索筛选区域 -->
      <div class="search-bar">
        <el-row :gutter="20">
          <el-col :span="4">
            <el-input
              v-model="searchForm.username"
              placeholder="操作人"
              clearable
              @keyup.enter="handleSearch"
            />
          </el-col>
          <el-col :span="4">
            <el-select v-model="searchForm.operationType" placeholder="操作类型" clearable @change="handleSearch">
              <el-option label="登录" value="LOGIN" />
              <el-option label="退出" value="LOGOUT" />
              <el-option label="创建" value="CREATE" />
              <el-option label="更新" value="UPDATE" />
              <el-option label="删除" value="DELETE" />
              <el-option label="查询" value="QUERY" />
              <el-option label="导出" value="EXPORT" />
              <el-option label="导入" value="IMPORT" />
              <el-option label="其他" value="OTHER" />
            </el-select>
          </el-col>
          <el-col :span="4">
            <el-select v-model="searchForm.operationModule" placeholder="操作模块" clearable @change="handleSearch">
              <el-option label="用户管理" value="用户管理" />
              <el-option label="职位管理" value="职位管理" />
              <el-option label="企业审核" value="企业审核" />
              <el-option label="职位审核" value="职位审核" />
              <el-option label="日志管理" value="日志管理" />
              <el-option label="数据统计" value="数据统计" />
              <el-option label="系统配置" value="系统配置" />
              <el-option label="其他" value="其他" />
            </el-select>
          </el-col>
          <el-col :span="3">
            <el-select v-model="searchForm.status" placeholder="操作结果" clearable @change="handleSearch">
              <el-option label="成功" :value="1" />
              <el-option label="失败" :value="0" />
            </el-select>
          </el-col>
          <el-col :span="6">
            <el-date-picker
              v-model="searchForm.dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              @change="handleSearch"
              style="width: 100%"
            />
          </el-col>
          <el-col :span="3">
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-col>
        </el-row>
      </div>

      <!-- 日志列表 -->
      <el-table :data="logList" v-loading="loading" stripe border>
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="roleName" label="角色" width="100">
          <template #default="{ row }">
            <el-tag size="small">{{ row.roleName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operationType" label="操作类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getOperationTypeType(row.operationType)" size="small">
              {{ getOperationTypeText(row.operationType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operationModule" label="操作模块" width="120" />
        <el-table-column prop="operationDesc" label="操作描述" width="150" show-overflow-tooltip />
        <el-table-column prop="requestMethod" label="请求方法" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="getMethodType(row.requestMethod)" size="small">{{ row.requestMethod }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="requestUrl" label="请求URL" min-width="200" show-overflow-tooltip />
        <el-table-column prop="ipAddress" label="IP地址" width="130" />
        <el-table-column prop="status" label="结果" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="executionTime" label="执行时长" width="100" align="center">
          <template #default="{ row }">
            <span :class="getExecutionTimeClass(row.executionTime)">{{ row.executionTime }}ms</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="操作时间" width="180" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="showDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next"
          @current-change="handlePageChange"
          @size-change="handleSizeChange"
        />
      </div>
    </el-card>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="日志详情" width="700px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="用户ID">{{ currentLog.userId }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ currentLog.username }}</el-descriptions-item>
        <el-descriptions-item label="角色">{{ currentLog.roleName }}</el-descriptions-item>
        <el-descriptions-item label="操作类型">{{ getOperationTypeText(currentLog.operationType) }}</el-descriptions-item>
        <el-descriptions-item label="操作模块">{{ currentLog.operationModule }}</el-descriptions-item>
        <el-descriptions-item label="操作描述">{{ currentLog.operationDesc }}</el-descriptions-item>
        <el-descriptions-item label="请求方法">{{ currentLog.requestMethod }}</el-descriptions-item>
        <el-descriptions-item label="请求URL">{{ currentLog.requestUrl }}</el-descriptions-item>
        <el-descriptions-item label="IP地址">{{ currentLog.ipAddress }}</el-descriptions-item>
        <el-descriptions-item label="IP归属地">{{ currentLog.ipLocation || '-' }}</el-descriptions-item>
        <el-descriptions-item label="执行时长">{{ currentLog.executionTime }}ms</el-descriptions-item>
        <el-descriptions-item label="操作结果">
          <el-tag :type="currentLog.status === 1 ? 'success' : 'danger'">
            {{ currentLog.status === 1 ? '成功' : '失败' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="操作时间">{{ currentLog.createTime }}</el-descriptions-item>
        <el-descriptions-item label="用户代理" :span="2">
          <div class="text-ellipsis">{{ currentLog.userAgent }}</div>
        </el-descriptions-item>
      </el-descriptions>
      
      <el-divider />
      
      <div class="detail-section">
        <h4>请求参数</h4>
        <el-input
          v-model="currentLog.requestParams"
          type="textarea"
          :rows="4"
          readonly
        />
      </div>
      
      <el-divider />
      
      <div class="detail-section">
        <h4>响应结果</h4>
        <el-input
          v-model="currentLog.responseResult"
          type="textarea"
          :rows="4"
          readonly
        />
      </div>
      
      <div v-if="currentLog.errorMsg" class="detail-section">
        <el-divider />
        <h4>错误信息</h4>
        <el-alert :title="currentLog.errorMsg" type="error" :closable="false" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listLogs, clearLogs } from '../../api/log'

const loading = ref(false)
const logList = ref([])
const detailVisible = ref(false)
const currentLog = ref({})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const searchForm = reactive({
  username: '',
  operationType: '',
  operationModule: '',
  status: null,
  dateRange: []
})

// 获取操作类型文本
const getOperationTypeText = (type) => {
  const map = {
    'LOGIN': '登录',
    'LOGOUT': '退出',
    'CREATE': '创建',
    'UPDATE': '更新',
    'DELETE': '删除',
    'QUERY': '查询',
    'EXPORT': '导出',
    'IMPORT': '导入',
    'OTHER': '其他'
  }
  return map[type] || type
}

// 获取操作类型标签样式
const getOperationTypeType = (type) => {
  const map = {
    'LOGIN': 'success',
    'LOGOUT': 'info',
    'CREATE': 'success',
    'UPDATE': 'warning',
    'DELETE': 'danger',
    'QUERY': 'primary',
    'EXPORT': 'warning',
    'IMPORT': 'warning',
    'OTHER': 'info'
  }
  return map[type] || 'info'
}

// 获取请求方法标签样式
const getMethodType = (method) => {
  const map = {
    'GET': 'success',
    'POST': 'primary',
    'PUT': 'warning',
    'DELETE': 'danger'
  }
  return map[method] || 'info'
}

// 获取执行时长样式
const getExecutionTimeClass = (time) => {
  if (time < 100) return 'time-normal'
  if (time < 500) return 'time-warning'
  return 'time-danger'
}

const fetchLogs = async () => {
  loading.value = true
  try {
    const params = {
      current: pagination.current,
      size: pagination.size,
      username: searchForm.username || undefined,
      operationType: searchForm.operationType || undefined,
      operationModule: searchForm.operationModule || undefined,
      status: searchForm.status !== null ? searchForm.status : undefined
    }
    
    // 处理日期范围
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      params.startTime = searchForm.dateRange[0] + ' 00:00:00'
      params.endTime = searchForm.dateRange[1] + ' 23:59:59'
    }
    
    const res = await listLogs(params)
    logList.value = res.data.records || []
    pagination.total = res.data.total || 0
  } catch (error) {
    console.error(error)
    logList.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  fetchLogs()
}

const resetSearch = () => {
  searchForm.username = ''
  searchForm.operationType = ''
  searchForm.operationModule = ''
  searchForm.status = null
  searchForm.dateRange = []
  pagination.current = 1
  fetchLogs()
}

const handlePageChange = (page) => {
  pagination.current = page
  fetchLogs()
}

const handleSizeChange = (size) => {
  pagination.size = size
  pagination.current = 1
  fetchLogs()
}

const showDetail = (row) => {
  currentLog.value = row
  detailVisible.value = true
}

const handleClear = () => {
  ElMessageBox.confirm('确定要清空所有日志吗？此操作不可恢复！', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await clearLogs()
      ElMessage.success('日志已清空')
      fetchLogs()
    } catch (error) {
      console.error(error)
    }
  })
}

onMounted(() => {
  fetchLogs()
})
</script>

<style scoped>
.admin-logs {
  max-width: 1400px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h3 {
  margin: 0;
}

.search-bar {
  padding: 15px 0;
  border-bottom: 1px solid #ebeef5;
  margin-bottom: 15px;
}

.pagination {
  margin-top: 20px;
  text-align: center;
}

.time-normal {
  color: #67c23a;
}

.time-warning {
  color: #e6a23c;
}

.time-danger {
  color: #f56c6c;
}

.detail-section h4 {
  margin: 0 0 10px 0;
  color: #606266;
}

.text-ellipsis {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
