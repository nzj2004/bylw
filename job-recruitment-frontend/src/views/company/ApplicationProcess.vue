<template>
  <div class="application-process">
    <el-card>
      <template #header>
        <div class="page-header">
          <el-button @click="goBack">返回</el-button>
          <span>申请流程处理 #{{ applicationId }}</span>
        </div>
      </template>

      <el-tabs v-model="activeTab">
        <el-tab-pane label="面试记录" name="interview">
          <el-card class="section-card" shadow="never">
            <template #header>
              <div class="section-title">新增面试安排</div>
            </template>
            <el-form :model="roundForm" label-width="110px" inline>
              <el-form-item label="面试轮次">
                <el-input-number v-model="roundForm.roundNo" :min="1" :precision="0" style="width: 120px" />
              </el-form-item>
              <el-form-item label="面试类型">
                <el-select v-model="roundForm.interviewType" style="width: 140px">
                  <el-option label="线上" value="线上" />
                  <el-option label="线下" value="线下" />
                </el-select>
              </el-form-item>
              <el-form-item label="面试时间">
                <el-date-picker
                  v-model="roundForm.interviewTime"
                  type="datetime"
                  format="YYYY-MM-DD HH:mm:ss"
                  value-format="YYYY-MM-DD HH:mm:ss"
                  placeholder="选择时间"
                />
              </el-form-item>
              <el-form-item label="地点/链接">
                <el-input v-model="roundForm.location" placeholder="线下填写地点，线上填写平台" />
              </el-form-item>
              <el-form-item label="会议链接" v-if="roundForm.interviewType === '线上'">
                <el-input v-model="roundForm.meetingLink" placeholder="请填写线上会议链接" />
              </el-form-item>
              <el-form-item label="联系人">
                <el-input v-model="roundForm.contactPerson" style="width: 180px" />
              </el-form-item>
              <el-form-item label="联系电话">
                <el-input v-model="roundForm.contactPhone" style="width: 180px" />
              </el-form-item>
              <el-form-item label="备注" class="full-row">
                <el-input v-model="roundForm.notes" type="textarea" :rows="2" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" :loading="creatingRound" @click="submitRound">保存面试安排</el-button>
              </el-form-item>
            </el-form>
          </el-card>

            <el-table :data="rounds" stripe style="margin-top: 16px">
            <el-table-column prop="roundNo" label="轮次" width="80" />
            <el-table-column prop="interviewType" label="方式" width="90" />
            <el-table-column prop="interviewTime" label="时间" width="170" />
            <el-table-column prop="updateTime" label="结果更新时间" width="170" />
            <el-table-column label="地点/链接">
              <template #default="{ row }">
                <div class="location-field">
                  <span>{{ row.location || '--' }}</span>
                  <el-link
                    v-if="row.meetingLink"
                    type="primary"
                    :href="normalizeMeetingLink(row.meetingLink)"
                    target="_blank"
                    rel="noreferrer"
                  >
                    {{ row.meetingLink }}
                  </el-link>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="contactPerson" label="联系人" width="110" />
            <el-table-column prop="contactPhone" label="联系电话" width="120" />
            <el-table-column prop="confirmationStatusName" label="确认状态" width="110">
              <template #default="{ row }">
                <el-tag :type="getConfirmationStatusType(row.confirmationStatus)">
                  {{ row.confirmationStatusName || '待确认' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="改期申请" min-width="180">
              <template #default="{ row }">
                <div v-if="row.confirmationStatus === 2">
                  <div>{{ row.rescheduleTime || '--' }}</div>
                  <div class="reschedule-reason">{{ row.rescheduleReason || '未填写原因' }}</div>
                </div>
                <span v-else>--</span>
              </template>
            </el-table-column>
            <el-table-column prop="resultName" label="结果" width="110">
              <template #default="{ row }">
                <el-tag :type="getRoundResultType(row.result)">{{ row.resultName || '待定' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="notes" label="备注" min-width="180" show-overflow-tooltip />
            <el-table-column label="操作" width="300">
              <template #default="{ row }">
                <el-button
                  size="small"
                  type="primary"
                  :disabled="row.result !== 0"
                  @click="openResultDialog(row)"
                >
                  录入结果
                </el-button>
                <el-button size="small" @click="openResultDialog(row)">更新结果</el-button>
                <el-button
                  size="small"
                  type="success"
                  :disabled="row.confirmationStatus !== 2"
                  @click="handleAcceptReschedule(row)"
                >
                  同意改期
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="Offer 管理" name="offer">
          <el-card class="section-card" shadow="never">
            <template #header>
              <div class="section-title">{{ offer?.id ? '编辑 Offer' : '发送 Offer' }}</div>
            </template>
            <el-form :model="offerForm" label-width="110px" :disabled="offerSubmitting">
              <el-form-item label="Offer 标题">
                <el-input v-model="offerForm.offerTitle" />
              </el-form-item>
              <el-form-item label="薪资下限">
                <el-input-number v-model="offerForm.salaryMin" :min="0" />
              </el-form-item>
              <el-form-item label="薪资上限">
                <el-input-number v-model="offerForm.salaryMax" :min="0" />
              </el-form-item>
              <el-form-item label="工作地点">
                <el-input v-model="offerForm.workLocation" />
              </el-form-item>
              <el-form-item label="入职日期">
                <el-date-picker
                  v-model="offerForm.entryDate"
                  type="date"
                  format="YYYY-MM-DD"
                  value-format="YYYY-MM-DD"
                  placeholder="选择入职日期"
                />
              </el-form-item>
              <el-form-item label="失效时间">
                <el-date-picker
                  v-model="offerForm.expireTime"
                  type="datetime"
                  format="YYYY-MM-DD HH:mm:ss"
                  value-format="YYYY-MM-DD HH:mm:ss"
                  placeholder="选择失效时间"
                />
              </el-form-item>
              <el-form-item label="福利说明">
                <el-input v-model="offerForm.benefits" type="textarea" :rows="2" />
              </el-form-item>
              <el-form-item label="Offer 详情" class="full-row">
                <el-input v-model="offerForm.offerContent" type="textarea" :rows="3" />
              </el-form-item>
              <el-form-item label="备注">
                <el-input v-model="offerForm.responseComment" type="textarea" :rows="2" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" :loading="offerSubmitting" @click="submitOffer">{{ offer?.id ? '更新 Offer' : '提交 Offer' }}</el-button>
              </el-form-item>
            </el-form>

            <el-empty v-if="!offer" description="当前无 Offer" />
            <template v-if="offer">
              <el-descriptions :column="2" border size="small" style="margin-top: 16px">
                <el-descriptions-item label="Offer 状态">{{ offer.statusName || '--' }}</el-descriptions-item>
                <el-descriptions-item label="创建时间">{{ offer.createTime || '--' }}</el-descriptions-item>
                <el-descriptions-item label="更新时间">{{ offer.updateTime || '--' }}</el-descriptions-item>
                <el-descriptions-item label="候选人反馈">
                  {{ offer.responseTime || '--' }}{{ offer.responseComment ? `（${offer.responseComment}）` : '' }}
                </el-descriptions-item>
              </el-descriptions>
              <div class="offer-actions" v-if="offer.status === 1">
                <el-button type="success" :loading="onboarding" @click="handleConfirmOnboard">确认已入职</el-button>
              </div>
            </template>
          </el-card>
        </el-tab-pane>

        <el-tab-pane label="状态时间线" name="history">
          <el-card class="section-card" shadow="never">
            <template #header>
              <div class="section-title">投递状态记录</div>
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
                  <el-tag size="small" :type="getApplicationStatusType(item.newStatus)">
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
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <el-dialog v-model="resultDialogVisible" title="录入面试结果" width="480px">
      <el-form :model="resultForm" label-width="100px">
        <el-form-item label="结果">
          <el-select v-model="resultForm.result" style="width: 100%">
            <el-option v-for="item in roundResultOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="resultForm.notes" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resultDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitRoundResult" :loading="resultSubmitting">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  createInterviewRound,
  getCompanyApplicationInterviews,
  updateInterviewRoundResult,
  getCompanyApplicationOffer,
  sendOffer,
  confirmOnboard,
  getCompanyApplicationStatusHistory,
  acceptInterviewReschedule
} from '../../api/user'

const route = useRoute()
const router = useRouter()

const applicationId = Number(route.params.applicationId)
const activeTab = ref('interview')
const loading = ref(false)
const creatingRound = ref(false)
const offerSubmitting = ref(false)
const onboarding = ref(false)
const rounds = ref([])
const offer = ref(null)
const statusHistories = ref([])

const roundForm = reactive({
  roundNo: 1,
  interviewType: '线上',
  interviewTime: '',
  location: '',
  meetingLink: '',
  contactPerson: '',
  contactPhone: '',
  notes: ''
})

const roundResultOptions = [
  { label: '待定', value: 0 },
  { label: '通过', value: 1 },
  { label: '失败', value: 2 },
  { label: '取消', value: 3 }
]

const resultDialogVisible = ref(false)
const resultSubmitting = ref(false)
const editingRound = ref(null)
const resultForm = reactive({
  result: 0,
  notes: ''
})

const offerForm = reactive({
  offerTitle: '',
  salaryMin: null,
  salaryMax: null,
  benefits: '',
  workLocation: '',
  offerContent: '',
  entryDate: '',
  expireTime: '',
  responseComment: ''
})

const fetchRounds = async () => {
  const res = await getCompanyApplicationInterviews(applicationId)
  rounds.value = res.data || []
  if (res.data?.length > 0) {
    const maxRound = Math.max(...res.data.map(item => Number(item.roundNo || 0)))
    roundForm.roundNo = maxRound + 1
  } else {
    roundForm.roundNo = 1
  }
}

const fetchOffer = async () => {
  try {
    const res = await getCompanyApplicationOffer(applicationId)
    offer.value = res?.data || null
    if (offer.value) {
      offerForm.offerTitle = offer.value.offerTitle || ''
      offerForm.salaryMin = offer.value.salaryMin
      offerForm.salaryMax = offer.value.salaryMax
      offerForm.benefits = offer.value.benefits || ''
      offerForm.workLocation = offer.value.workLocation || ''
      offerForm.offerContent = offer.value.offerContent || ''
      offerForm.entryDate = offer.value.entryDate || ''
      offerForm.expireTime = offer.value.expireTime || ''
      offerForm.responseComment = offer.value.responseComment || ''
    }
  } catch (error) {
    offer.value = null
  }
}

const fetchStatusHistories = async () => {
  const res = await getCompanyApplicationStatusHistory(applicationId)
  statusHistories.value = res.data || []
}

const loadAll = async () => {
  if (!applicationId) {
    ElMessage.error('申请 ID 不存在')
    router.push('/company/applications')
    return
  }

  loading.value = true
  try {
    await Promise.all([fetchRounds(), fetchOffer(), fetchStatusHistories()])
  } finally {
    loading.value = false
  }
}

const submitRound = async () => {
  if (!roundForm.interviewType) {
    ElMessage.warning('请先选择面试类型')
    return
  }
  if (!roundForm.interviewTime) {
    ElMessage.warning('请先选择面试时间')
    return
  }
  if (roundForm.interviewType === '线上' && !roundForm.meetingLink) {
    ElMessage.warning('线上面试请填写会议链接')
    return
  }
  if (roundForm.interviewType === '线下' && !roundForm.location) {
    ElMessage.warning('线下面试请填写面试地点')
    return
  }

  creatingRound.value = true
  try {
    const payload = {
      roundNo: roundForm.roundNo,
      interviewType: roundForm.interviewType,
      interviewTime: roundForm.interviewTime,
      location: roundForm.interviewType === '线下' ? roundForm.location : (roundForm.location || ''),
      meetingLink: roundForm.interviewType === '线上' ? normalizeMeetingLink(roundForm.meetingLink) : '',
      contactPerson: roundForm.contactPerson,
      contactPhone: roundForm.contactPhone,
      notes: roundForm.notes
    }
    await createInterviewRound(applicationId, payload)
    ElMessage.success('面试安排已保存')
    roundForm.location = ''
    roundForm.meetingLink = ''
    roundForm.contactPerson = ''
    roundForm.contactPhone = ''
    roundForm.notes = ''
    await loadAll()
    activeTab.value = 'interview'
  } catch (error) {
    ElMessage.error('面试安排保存失败')
  } finally {
    creatingRound.value = false
  }
}

const openResultDialog = (row) => {
  if (!row) {
    ElMessage.warning('请先选择面试记录')
    return
  }
  editingRound.value = row
  resultForm.result = row?.result ?? 0
  resultForm.notes = row?.notes || ''
  resultDialogVisible.value = true
}

const submitRoundResult = async () => {
  if (!editingRound.value?.id) {
    ElMessage.warning('请选择要更新的面试记录')
    return
  }
  resultSubmitting.value = true
  try {
    await updateInterviewRoundResult(editingRound.value.id, {
      result: Number(resultForm.result),
      notes: resultForm.notes
    })
    ElMessage.success('面试结果已更新')
    resultDialogVisible.value = false
    await loadAll()
  } catch (error) {
    ElMessage.error('面试结果更新失败')
  } finally {
    resultSubmitting.value = false
  }
}

const submitOffer = async () => {
  offerSubmitting.value = true
  try {
    const payload = {
      offerTitle: offerForm.offerTitle,
      salaryMin: roundZeroToNull(offerForm.salaryMin),
      salaryMax: roundZeroToNull(offerForm.salaryMax),
      benefits: offerForm.benefits,
      workLocation: offerForm.workLocation,
      offerContent: offerForm.offerContent,
      entryDate: offerForm.entryDate,
      expireTime: offerForm.expireTime,
      responseComment: offerForm.responseComment
    }
    const res = await sendOffer(applicationId, payload)
    offer.value = res.data || offer.value
    ElMessage.success('Offer 已提交')
    await loadAll()
  } catch (error) {
    ElMessage.error('Offer 提交失败')
  } finally {
    offerSubmitting.value = false
  }
}

const handleConfirmOnboard = async () => {
  onboarding.value = true
  try {
    await ElMessageBox.confirm('确认该候选人已入职吗？', '确认入职', {
      type: 'success',
      confirmButtonText: '确认',
      cancelButtonText: '取消'
    })
    await confirmOnboard(applicationId)
    ElMessage.success('已确认入职')
    await loadAll()
  } catch (error) {
    if (error?.action === 'cancel' || error?.action === 'close' || error === 'cancel' || error === 'close') {
      return
    }
    ElMessage.error('确认入职失败')
  } finally {
    onboarding.value = false
  }
}

const roundZeroToNull = (value) => {
  if (value === null || value === undefined || value === '') {
    return null
  }
  return Number(value)
}

const goBack = () => {
  router.push('/company/applications')
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
  const types = {
    0: 'warning',
    1: 'success',
    2: 'primary'
  }
  return types[status ?? 0] || 'info'
}

const handleAcceptReschedule = async (row) => {
  if (!row?.id) {
    ElMessage.warning('请选择面试记录')
    return
  }
  try {
    await ElMessageBox.confirm(`确认同意改期到 ${row.rescheduleTime || '--'} 吗？`, '同意改期', {
      type: 'success',
      confirmButtonText: '同意',
      cancelButtonText: '取消'
    })
    await acceptInterviewReschedule(row.id)
    ElMessage.success('已同意改期')
    await loadAll()
  } catch (error) {
    if (error?.action === 'cancel' || error?.action === 'close' || error === 'cancel' || error === 'close') {
      return
    }
    ElMessage.error('处理改期申请失败')
  }
}

const getApplicationStatusType = (status) => {
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

const normalizeMeetingLink = (value) => {
  if (!value) {
    return ''
  }
  if (/^https?:\/\/|^mailto:|^tel:/i.test(value)) {
    return value
  }
  return `https://${value}`
}

onMounted(() => {
  loadAll()
})
</script>

<style scoped>
.application-process {
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 12px;
}

.section-card {
  margin-bottom: 16px;
}

.offer-actions {
  margin-top: 16px;
  text-align: right;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
}

.full-row {
  width: 100%;
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

.reschedule-reason {
  margin-top: 4px;
  color: #606266;
  font-size: 12px;
}
</style>
