<template>
  <div class="operator-jobs">
    <el-card>
      <template #header>
        <div class="card-header">
          <h3>职位审核</h3>
          <div class="header-right">
            <el-radio-group v-model="filterStatus" @change="fetchJobs">
              <el-radio-button :label="0">待审核</el-radio-button>
              <el-radio-button :label="1">已发布</el-radio-button>
              <el-radio-button :label="2">已拒绝</el-radio-button>
              <el-radio-button :label="null">全部</el-radio-button>
            </el-radio-group>
            <template v-if="filterStatus === 0">
              <el-button type="success" @click="handleBatchAudit(1)" :disabled="selectedJobs.length === 0">
                批量审核({{ selectedJobs.length }})
              </el-button>
              <el-button type="danger" @click="handleBatchReject" :disabled="selectedJobs.length === 0">
                批量拒绝({{ selectedJobs.length }})
              </el-button>
            </template>
          </div>
        </div>
      </template>

      <el-table
        :data="jobs"
        v-loading="loading"
        stripe
        border
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="title" label="职位标题" min-width="150" show-overflow-tooltip />
        <el-table-column prop="companyName" label="企业名称" min-width="120" show-overflow-tooltip />
        <el-table-column prop="category" label="职位类别" width="140" />
        <el-table-column prop="workCity" label="城市" width="100" />
        <el-table-column prop="salaryRange" label="薪资" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="提交时间" width="160" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="viewDetail(row)">详情</el-button>
            <template v-if="row.status === 0">
              <el-button type="success" size="small" @click="handleAudit(row, 1)">通过</el-button>
              <el-button type="danger" size="small" @click="handleReject(row)">拒绝</el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="jobs.length === 0 && !loading" description="暂无待审职位" />

      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <el-dialog v-model="detailVisible" title="职位详情" width="820px">
      <el-descriptions :column="2" border v-if="currentJob">
        <el-descriptions-item label="职位标题">{{ currentJob.title }}</el-descriptions-item>
        <el-descriptions-item label="企业名称">{{ currentJob.companyName }}</el-descriptions-item>
        <el-descriptions-item label="职位类别">{{ currentJob.category }}</el-descriptions-item>
        <el-descriptions-item label="城市">{{ currentJob.workCity }}</el-descriptions-item>
        <el-descriptions-item label="薪资范围">{{ currentJob.salaryRange }}</el-descriptions-item>
        <el-descriptions-item label="经验要求">{{ currentJob.experience }}</el-descriptions-item>
        <el-descriptions-item label="学历要求">{{ currentJob.education }}</el-descriptions-item>
        <el-descriptions-item label="职位类型">{{ currentJob.jobTypeName }}</el-descriptions-item>
        <el-descriptions-item label="职位描述" :span="2">{{ currentJob.jobDesc }}</el-descriptions-item>
        <el-descriptions-item label="岗位要求" :span="2">{{ currentJob.requirements }}</el-descriptions-item>
        <el-descriptions-item label="状态福利" :span="2">{{ currentJob.welfare || '暂无' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <el-dialog v-model="rejectDialogVisible" title="拒绝原因" width="500px">
      <el-input v-model="rejectReason" type="textarea" :rows="4" placeholder="请填写拒绝原因" />
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmReject" :disabled="!rejectReason.trim()">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="batchRejectDialogVisible" title="批量拒绝原因" width="500px">
      <el-input v-model="batchRejectReason" type="textarea" :rows="4" placeholder="请填写拒绝原因" />
      <template #footer>
        <el-button @click="batchRejectDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmBatchReject" :disabled="!batchRejectReason.trim()">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listJobs, auditJob, batchAuditJobs } from '../../api/job'

const jobs = ref([])
const loading = ref(false)
const filterStatus = ref(0)
const selectedJobs = ref([])
const detailVisible = ref(false)
const rejectDialogVisible = ref(false)
const batchRejectDialogVisible = ref(false)
const rejectReason = ref('')
const batchRejectReason = ref('')
const currentJob = ref(null)

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const fetchJobs = async () => {
  loading.value = true
  try {
    const params = {
      current: pagination.current,
      size: pagination.size
    }
    if (filterStatus.value !== null) {
      params.status = filterStatus.value
    }
    const res = await listJobs(params)
    jobs.value = res.data.records || []
    pagination.total = res.data.total || 0
  } catch (error) {
    ElMessage.error('获取职位列表失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleSelectionChange = (selection) => {
  selectedJobs.value = selection
}

const getStatusType = (status) => {
  const types = { 0: 'warning', 1: 'success', 2: 'danger', 3: 'info' }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = { 0: '待审核', 1: '已发布', 2: '已拒绝', 3: '已下架' }
  return texts[status] || '未知'
}

const viewDetail = (row) => {
  currentJob.value = row
  detailVisible.value = true
}

const handleAudit = (row, status) => {
  ElMessageBox.confirm('确定通过该职位？', '审核确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await auditJob(row.id, status)
      ElMessage.success('审核通过成功')
      fetchJobs()
    } catch (error) {
      ElMessage.error('审核通过失败')
      console.error(error)
    }
  })
}

const handleReject = (row) => {
  currentJob.value = row
  rejectReason.value = ''
  rejectDialogVisible.value = true
}

const confirmReject = async () => {
  if (!rejectReason.value.trim()) {
    ElMessage.warning('请填写拒绝原因')
    return
  }

  try {
    await auditJob(currentJob.value.id, 2, rejectReason.value)
    ElMessage.success('拒绝成功')
    rejectDialogVisible.value = false
    fetchJobs()
  } catch (error) {
    ElMessage.error('拒绝失败')
    console.error(error)
  }
}

const handleBatchAudit = (status) => {
  if (selectedJobs.value.length === 0) {
    ElMessage.warning('请先选择职位')
    return
  }
  ElMessageBox.confirm(
    `确定批量通过 ${selectedJobs.value.length} 个职位？`,
    '批量审核',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const jobIds = selectedJobs.value.map((job) => job.id)
      await batchAuditJobs({ jobIds, status })
      ElMessage.success('批量通过成功')
      selectedJobs.value = []
      fetchJobs()
    } catch (error) {
      ElMessage.error('批量通过失败')
      console.error(error)
    }
  })
}

const handleBatchReject = () => {
  if (selectedJobs.value.length === 0) {
    ElMessage.warning('请先选择职位')
    return
  }
  batchRejectReason.value = ''
  batchRejectDialogVisible.value = true
}

const confirmBatchReject = async () => {
  if (!batchRejectReason.value.trim()) {
    ElMessage.warning('请填写拒绝原因')
    return
  }

  try {
    const jobIds = selectedJobs.value.map((job) => job.id)
    await batchAuditJobs({ jobIds, status: 2, reason: batchRejectReason.value })
    ElMessage.success('批量拒绝成功')
    batchRejectDialogVisible.value = false
    selectedJobs.value = []
    fetchJobs()
  } catch (error) {
    ElMessage.error('批量拒绝失败')
    console.error(error)
  }
}

const handlePageChange = (page) => {
  pagination.current = page
  fetchJobs()
}

onMounted(() => {
  fetchJobs()
})
</script>

<style scoped>
.operator-jobs {
  max-width: 1400px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-right {
  display: flex;
  gap: 10px;
  align-items: center;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>
