<template>
  <div class="operator-companies">
    <el-card>
      <template #header>
        <div class="card-header">
          <h3>企业审核</h3>
          <el-radio-group v-model="filterStatus" @change="fetchCompanies">
            <el-radio-button :label="0">待审核</el-radio-button>
            <el-radio-button :label="1">已通过</el-radio-button>
            <el-radio-button :label="2">未通过</el-radio-button>
            <el-radio-button :label="null">全部</el-radio-button>
          </el-radio-group>
        </div>
      </template>

      <el-table :data="companies" stripe v-loading="loading">
        <el-table-column prop="companyName" label="企业名称" min-width="180">
          <template #default="{ row }">
            <div class="company-name-cell">
              <span>{{ row.companyName }}</span>
              <el-tag v-if="row.hasPendingChanges" size="small" type="warning">资料变更</el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="industry" label="所属行业" width="150" />
        <el-table-column prop="scale" label="企业规模" width="120" />
        <el-table-column prop="contactName" label="联系人" width="110" />
        <el-table-column prop="contactPhone" label="联系电话" width="140" />
        <el-table-column prop="status" label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ row.statusName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="提交时间" width="180" />
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="viewDetail(row)">详情</el-button>
            <el-button v-if="row.status === 0" type="success" size="small" @click="handleAudit(row, 1)">
              通过
            </el-button>
            <el-button v-if="row.status === 0" type="danger" size="small" @click="showRejectDialog(row)">
              拒绝
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination" v-if="pagination.total > 0">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          layout="total, prev, pager, next"
          @current-change="fetchCompanies"
        />
      </div>

      <el-empty v-if="companies.length === 0 && !loading" description="暂无企业数据" />
    </el-card>

    <el-dialog v-model="detailVisible" title="企业详情" width="800px">
      <el-alert
        v-if="currentCompany?.hasPendingChanges"
        title="当前展示的是企业提交的待审核资料"
        type="warning"
        :closable="false"
        show-icon
        class="detail-alert"
      />
      <el-descriptions :column="2" border v-if="currentCompany">
        <el-descriptions-item label="企业名称">{{ currentCompany.companyName }}</el-descriptions-item>
        <el-descriptions-item label="所属行业">{{ currentCompany.industry }}</el-descriptions-item>
        <el-descriptions-item label="企业规模">{{ currentCompany.scale }}</el-descriptions-item>
        <el-descriptions-item label="企业官网">{{ currentCompany.website }}</el-descriptions-item>
        <el-descriptions-item label="企业地址" :span="2">{{ currentCompany.address }}</el-descriptions-item>
        <el-descriptions-item label="企业简介" :span="2">{{ currentCompany.description }}</el-descriptions-item>
        <el-descriptions-item label="联系人">{{ currentCompany.contactName }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ currentCompany.contactPhone }}</el-descriptions-item>
        <el-descriptions-item label="联系邮箱">{{ currentCompany.contactEmail }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentCompany.status)">{{ currentCompany.statusName }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item v-if="currentCompany.rejectReason" label="拒绝原因" :span="2">
          {{ currentCompany.rejectReason }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <el-dialog v-model="rejectDialogVisible" title="拒绝原因" width="500px">
      <el-input
        v-model="rejectReason"
        type="textarea"
        :rows="4"
        placeholder="请输入拒绝原因"
      />
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmReject" :disabled="!rejectReason.trim()">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listCompanies, auditCompany } from '../../api/company'

const companies = ref([])
const loading = ref(false)
const filterStatus = ref(0)
const detailVisible = ref(false)
const rejectDialogVisible = ref(false)
const rejectReason = ref('')
const currentCompany = ref(null)

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const getStatusType = (status) => {
  const types = { 0: 'warning', 1: 'success', 2: 'danger' }
  return types[status] || 'info'
}

const fetchCompanies = async () => {
  loading.value = true
  try {
    const params = {
      current: pagination.current,
      size: pagination.size
    }

    if (filterStatus.value !== null) {
      params.status = filterStatus.value
    }

    const res = await listCompanies(params)
    companies.value = res.data.records || []
    pagination.total = res.data.total || 0
  } catch (error) {
    ElMessage.error('获取企业列表失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

const viewDetail = (row) => {
  currentCompany.value = row
  detailVisible.value = true
}

const showRejectDialog = (row) => {
  currentCompany.value = row
  rejectReason.value = ''
  rejectDialogVisible.value = true
}

const handleAudit = async (row, status) => {
  const action = status === 1 ? '通过' : '拒绝'

  try {
    await ElMessageBox.confirm(
      `确定要${action}该企业吗？`,
      '确认审核',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await auditCompany(row.id, status, '')
    ElMessage.success(`审核${action}成功`)
    await fetchCompanies()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('审核失败')
      console.error(error)
    }
  }
}

const confirmReject = async () => {
  try {
    await auditCompany(currentCompany.value.id, 2, rejectReason.value)
    ElMessage.success('已拒绝该企业，企业岗位已下架')
    rejectDialogVisible.value = false
    await fetchCompanies()
  } catch (error) {
    ElMessage.error('审核失败')
    console.error(error)
  }
}

onMounted(() => {
  fetchCompanies()
})
</script>

<style scoped>
.operator-companies {
  max-width: 1400px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.company-name-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.detail-alert {
  margin-bottom: 16px;
}
</style>
