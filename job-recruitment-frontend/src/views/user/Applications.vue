<template>
  <div class="applications-container">
    <el-card>
      <template #header>
        <h3>我的投递记录</h3>
      </template>

      <el-table :data="applications" stripe v-loading="loading">
        <el-table-column prop="jobTitle" label="职位" />
        <el-table-column prop="companyName" label="公司" />
        <el-table-column prop="applyTime" label="投递时间" width="180" />
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ row.statusName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="130">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="openProgress(row)">查看进度</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="applications.length === 0 && !loading" description="暂无投递记录" />
    </el-card>

      <el-dialog v-model="progressDialogVisible" title="投递进度" width="880px">
      <div v-if="selectedApplication" v-loading="progressLoading" class="progress-content">
        <el-descriptions title="申请信息" :column="2" border size="small" direction="vertical">
          <el-descriptions-item label="职位">{{ selectedApplication.jobTitle || '--' }}</el-descriptions-item>
          <el-descriptions-item label="公司">{{ selectedApplication.companyName || '--' }}</el-descriptions-item>
          <el-descriptions-item label="申请时间">{{ selectedApplication.applyTime || '--' }}</el-descriptions-item>
          <el-descriptions-item label="当前状态">
            <el-tag :type="getStatusType(selectedApplication.status)">
              {{ selectedApplication.statusName || '未知' }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>

        <el-card class="section-card" shadow="never">
          <template #header>
            <span>状态时间线</span>
          </template>
          <el-empty v-if="statusHistories.length === 0" description="暂无状态记录" />
          <el-timeline v-else>
            <el-timeline-item
              v-for="item in statusHistories"
              :key="item.id"
              :timestamp="item.createTime"
              placement="top"
            >
              <div class="history-title">
                {{ item.actionName || '状态更新' }}
                <el-tag size="small" :type="getStatusType(item.newStatus)">
                  {{ item.newStatusName || '未知' }}
                </el-tag>
              </div>
              <div class="history-meta">
                {{ item.oldStatusName || '无' }} → {{ item.newStatusName || '未知' }}
                <span v-if="item.changedRole"> · {{ item.changedRole }}</span>
              </div>
              <div v-if="item.remark" class="history-remark">{{ item.remark }}</div>
            </el-timeline-item>
          </el-timeline>
        </el-card>

        <el-card class="section-card" shadow="never">
          <template #header>
            <span>面试结果摘要</span>
          </template>
          <el-descriptions :column="2" border size="small" direction="vertical">
            <el-descriptions-item label="最新进度">{{ interviewSummary }}</el-descriptions-item>
            <el-descriptions-item label="面试结果">
              <template v-if="latestInterviewRound">
                <el-tag :type="getRoundResultType(latestInterviewRound.result)">
                  {{ latestInterviewRound.resultName || '待定' }}
                </el-tag>
              </template>
              <span v-else>暂无</span>
            </el-descriptions-item>
            <el-descriptions-item label="对应轮次">
              {{ latestInterviewRound?.roundNo ? `第 ${latestInterviewRound.roundNo} 轮` : '--' }}
            </el-descriptions-item>
            <el-descriptions-item label="面试时间">
              {{ latestInterviewRound?.interviewTime || '--' }}
            </el-descriptions-item>
          </el-descriptions>
        </el-card>

        <el-card class="section-card" shadow="never">
          <template #header>
            <span>面试记录</span>
          </template>
          <el-empty v-if="interviewRounds.length === 0" description="暂无面试记录" />
          <el-table v-else :data="interviewRounds" stripe size="small">
            <el-table-column prop="roundNo" label="轮次" width="70" />
            <el-table-column prop="interviewType" label="方式" width="90" />
            <el-table-column prop="interviewTime" label="时间" width="170" />
            <el-table-column label="地点/链接">
              <template #default="{ row }">
                <div class="location-field">
                  <div v-if="row.location">{{ row.location }}</div>
                  <el-link
                    v-if="row.meetingLink"
                    type="primary"
                    :href="normalizeMeetingLink(row.meetingLink)"
                    target="_blank"
                    rel="noreferrer"
                  >
                    {{ row.meetingLink }}
                  </el-link>
                  <span v-if="!row.location && !row.meetingLink">--</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="contactPerson" label="联系人" width="110" />
            <el-table-column prop="contactPhone" label="联系电话" width="130" />
            <el-table-column prop="confirmationStatusName" label="确认状态" width="110">
              <template #default="{ row }">
                <el-tag :type="getConfirmationStatusType(row.confirmationStatus)">
                  {{ row.confirmationStatusName || '待确认' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="resultName" label="结果" width="110">
              <template #default="{ row }">
                <el-tag :type="getRoundResultType(row.result)">{{ row.resultName || '待定' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="180">
              <template #default="{ row }">
                <el-button size="small" type="success" :disabled="!canRespondInterview(row)" @click="handleConfirmInterview(row)">
                  确认
                </el-button>
                <el-button size="small" :disabled="!canRespondInterview(row)" @click="openRescheduleDialog(row)">
                  改期
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>

        <el-card class="section-card" shadow="never">
          <template #header>
            <span>Offer 信息</span>
          </template>

          <el-empty v-if="!selectedOffer" description="暂无 Offer" />

          <div v-else>
            <el-descriptions :column="2" border size="small" direction="vertical">
              <el-descriptions-item label="Offer 标题">{{ selectedOffer.offerTitle || '--' }}</el-descriptions-item>
              <el-descriptions-item label="状态">
                <el-tag :type="getOfferStatusType(selectedOffer.status)">
                  {{ selectedOffer.statusName || '未知' }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="薪资区间">{{ formatSalary(selectedOffer) }}</el-descriptions-item>
              <el-descriptions-item label="工作地点">{{ selectedOffer.workLocation || '--' }}</el-descriptions-item>
              <el-descriptions-item label="福利说明">{{ selectedOffer.benefits || '--' }}</el-descriptions-item>
              <el-descriptions-item label="入职日期">{{ selectedOffer.entryDate || '--' }}</el-descriptions-item>
              <el-descriptions-item label="失效时间">{{ selectedOffer.expireTime || '--' }}</el-descriptions-item>
              <el-descriptions-item label="反馈时间">{{ selectedOffer.responseTime || '--' }}</el-descriptions-item>
              <el-descriptions-item label="反馈说明">{{ selectedOffer.responseComment || '--' }}</el-descriptions-item>
            </el-descriptions>
            <el-descriptions :column="1" border size="small" direction="vertical" style="margin-top: 12px">
              <el-descriptions-item label="Offer 内容">{{ selectedOffer.offerContent || '--' }}</el-descriptions-item>
            </el-descriptions>
          </div>
        </el-card>
      </div>

      <template #footer>
        <el-button @click="progressDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="rescheduleDialogVisible" title="申请面试改期" width="480px">
      <el-form :model="rescheduleForm" label-width="110px">
        <el-form-item label="期望时间">
          <el-date-picker
            v-model="rescheduleForm.rescheduleTime"
            type="datetime"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
            placeholder="选择期望时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="改期原因">
          <el-input v-model="rescheduleForm.rescheduleReason" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rescheduleDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="rescheduleSubmitting" @click="submitReschedule">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getUserApplications,
  getUserOffers,
  getUserApplicationInterviews,
  getUserApplicationStatusHistory,
  confirmInterviewRound,
  requestInterviewReschedule
} from '../../api/user'

const applications = ref([])
const loading = ref(false)
const progressDialogVisible = ref(false)
const progressLoading = ref(false)

const selectedApplication = ref(null)
const interviewRounds = ref([])
const statusHistories = ref([])
const offersByApplicationId = ref({})
const selectedOffer = ref(null)
const rescheduleDialogVisible = ref(false)
const rescheduleSubmitting = ref(false)
const selectedRound = ref(null)
const rescheduleForm = reactive({
  rescheduleTime: '',
  rescheduleReason: ''
})

const latestInterviewRound = computed(() => {
  if (!interviewRounds.value.length) {
    return null
  }
  return [...interviewRounds.value].sort((a, b) => {
    const aRound = Number(a.roundNo || 0)
    const bRound = Number(b.roundNo || 0)
    if (aRound !== bRound) {
      return bRound - aRound
    }
    return (b.interviewTime || '').localeCompare(a.interviewTime || '')
  })[0]
})

const isPermissionLikeError = (error) => {
  const text = [
    error?.response?.status,
    error?.response?.data?.code,
    error?.response?.data?.message,
    error?.message
  ].filter(Boolean).join(' ').toLowerCase()
  return ['401', '403', '权限', '无权', '没有权限', 'forbidden', 'permission', '鏃犳潈', '娌℃湁鏉冮檺']
    .some((item) => text.includes(String(item).toLowerCase()))
}

const interviewSummary = computed(() => {
  if (!latestInterviewRound.value) {
    return '暂无面试记录'
  }
  if (latestInterviewRound.value.result === 0) {
    return `已安排第 ${latestInterviewRound.value.roundNo || '?'} 轮`
  }
  if (latestInterviewRound.value.resultName) {
    return latestInterviewRound.value.resultName
  }
  return '暂无结果'
})

const fetchApplications = async () => {
  loading.value = true
  try {
    const appResult = await getUserApplications()
    applications.value = appResult.data || []
  } catch (error) {
    if (!isPermissionLikeError(error)) {
      ElMessage.error('获取投递记录失败')
    }
    console.error(error)
    applications.value = []
  }

  try {
    const offerResult = await getUserOffers()
    const offersData = offerResult.data || []
    offersByApplicationId.value = offersData.reduce((acc, item) => {
      if (item?.applicationId != null) {
        acc[item.applicationId] = item
      }
      return acc
    }, {})
  } catch (error) {
    console.warn('获取 Offer 记录失败：', error)
    offersByApplicationId.value = {}
  } finally {
    loading.value = false
  }
}

const fetchProgress = async (applicationId) => {
  progressLoading.value = true
  try {
    interviewRounds.value = []
    statusHistories.value = []
    const [interviewRes, historyRes] = await Promise.all([
      getUserApplicationInterviews(applicationId),
      getUserApplicationStatusHistory(applicationId)
    ])
    interviewRounds.value = interviewRes.data || []
    statusHistories.value = historyRes.data || []
  } catch (error) {
    console.error(error)
    if (!isPermissionLikeError(error)) {
      ElMessage.error('获取投递进度失败')
    }
  } finally {
    progressLoading.value = false
  }
}

const openProgress = async (row) => {
  selectedApplication.value = row
  selectedOffer.value = offersByApplicationId.value[row.id] || null
  progressDialogVisible.value = true
  await fetchProgress(row.id)
}

const getStatusType = (status) => {
  const types = {
    0: 'info',
    1: 'primary',
    2: 'success',
    3: 'danger',
    4: 'warning',
    5: 'danger',
    6: 'warning',
    7: 'success',
    8: 'info',
    9: 'success',
    10: 'success',
    11: 'info'
  }
  return types[status] || 'info'
}

const getRoundResultType = (result) => {
  const types = {
    0: 'info',
    1: 'success',
    2: 'danger',
    3: 'warning'
  }
  return types[result] || 'info'
}

const getConfirmationStatusType = (status) => {
  const map = {
    0: 'warning',
    1: 'success',
    2: 'primary'
  }
  return map[status ?? 0] || 'info'
}

const canRespondInterview = (row) => row?.result === 0 && [0, null, undefined].includes(row?.confirmationStatus)

const handleConfirmInterview = async (row) => {
  try {
    await ElMessageBox.confirm('确认参加该轮面试吗？', '确认面试', {
      type: 'success',
      confirmButtonText: '确认',
      cancelButtonText: '取消'
    })
    await confirmInterviewRound(row.id)
    ElMessage.success('已确认面试')
    await fetchProgress(selectedApplication.value.id)
  } catch (error) {
    if (error?.action === 'cancel' || error?.action === 'close' || error === 'cancel' || error === 'close') {
      return
    }
    ElMessage.error('确认面试失败')
  }
}

const openRescheduleDialog = (row) => {
  selectedRound.value = row
  rescheduleForm.rescheduleTime = row?.interviewTime || ''
  rescheduleForm.rescheduleReason = ''
  rescheduleDialogVisible.value = true
}

const submitReschedule = async () => {
  if (!selectedRound.value?.id) {
    ElMessage.warning('请选择面试记录')
    return
  }
  if (!rescheduleForm.rescheduleTime) {
    ElMessage.warning('请选择期望改期时间')
    return
  }
  rescheduleSubmitting.value = true
  try {
    await requestInterviewReschedule(selectedRound.value.id, {
      rescheduleTime: rescheduleForm.rescheduleTime,
      rescheduleReason: rescheduleForm.rescheduleReason
    })
    ElMessage.success('已提交改期申请')
    rescheduleDialogVisible.value = false
    await fetchProgress(selectedApplication.value.id)
  } catch (error) {
    ElMessage.error('提交改期申请失败')
  } finally {
    rescheduleSubmitting.value = false
  }
}

const getOfferStatusType = (status) => {
  const map = {
    0: 'info',
    1: 'success',
    2: 'danger',
    3: 'warning'
  }
  return map[status] || 'info'
}

const formatSalary = (row) => {
  if (!row) {
    return '--'
  }
  if (row.salaryMin == null && row.salaryMax == null) {
    return '--'
  }
  const min = row.salaryMin == null ? '...' : `${row.salaryMin}K`
  const max = row.salaryMax == null ? '...' : `${row.salaryMax}K`
  return `${min} - ${max}`
}

const normalizeMeetingLink = (value) => {
  if (!value) {
    return ''
  }
  if (/^https?:\/\//i.test(value) || /^mailto:|^tel:/i.test(value)) {
    return value
  }
  return `https://${value}`
}

onMounted(() => {
  fetchApplications()
})
</script>

<style scoped>
.applications-container {
  max-width: 900px;
  margin: 0 auto;
}

.section-card {
  margin-top: 12px;
}

.progress-content {
  max-height: 70vh;
  overflow-y: auto;
}

.location-field :deep(.el-link) {
  white-space: normal;
  word-break: break-word;
}

.history-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
}

.history-meta,
.history-remark {
  margin-top: 4px;
  color: #606266;
  font-size: 13px;
}
</style>
