<template>
  <div class="offers-page">
    <el-card>
      <template #header>
        <h3>我的 Offer 列表</h3>
      </template>

      <el-table :data="offers" stripe v-loading="loading">
        <el-table-column prop="jobTitle" label="职位" min-width="160" />
        <el-table-column prop="companyName" label="公司" min-width="140" />
        <el-table-column prop="offerTitle" label="Offer 标题" min-width="140" />
        <el-table-column label="薪资区间">
          <template #default="{ row }">
            <span>{{ formatSalary(row) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="entryDate" label="入职日期" width="120" />
        <el-table-column prop="expireTime" label="截止响应" width="170" />
        <el-table-column label="状态">
          <template #default="{ row }">
            <el-tag :type="getOfferType(row.status)">{{ row.statusName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220">
          <template #default="{ row }">
            <el-button
              type="success"
              size="small"
              :disabled="row.status !== 0"
              @click="respondOffer(row, 1)"
            >接受</el-button>
            <el-button
              type="danger"
              size="small"
              :disabled="row.status !== 0"
              @click="respondOffer(row, 2)"
            >拒绝</el-button>
            <el-button
              type="primary"
              size="small"
              @click="viewOffer(row)"
            >查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="offers.length === 0 && !loading" description="暂无 Offer" />

      <div class="pagination" v-if="pagination.total > 0">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          layout="total, prev, pager, next"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <el-dialog v-model="detailVisible" title="Offer 详情" width="680px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="职位">{{ selectedOffer?.jobTitle || '--' }}</el-descriptions-item>
        <el-descriptions-item label="公司">{{ selectedOffer?.companyName || '--' }}</el-descriptions-item>
        <el-descriptions-item label="Offer 标题">{{ selectedOffer?.offerTitle || '--' }}</el-descriptions-item>
        <el-descriptions-item label="薪资">{{ formatSalary(selectedOffer) }}</el-descriptions-item>
        <el-descriptions-item label="福利">{{ selectedOffer?.benefits || '--' }}</el-descriptions-item>
        <el-descriptions-item label="工作地点">{{ selectedOffer?.workLocation || '--' }}</el-descriptions-item>
        <el-descriptions-item label="Offer 内容">{{ selectedOffer?.offerContent || '--' }}</el-descriptions-item>
        <el-descriptions-item label="入职日期">{{ selectedOffer?.entryDate || '--' }}</el-descriptions-item>
        <el-descriptions-item label="响应时间">{{ selectedOffer?.responseTime || '--' }}</el-descriptions-item>
        <el-descriptions-item label="反馈说明">{{ selectedOffer?.responseComment || '--' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserOffers, respondOffer as respondOfferApi } from '../../api/user'

const offers = ref([])
const loading = ref(false)
const selectedOffer = ref(null)
const detailVisible = ref(false)

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const fetchOffers = async () => {
  loading.value = true
  try {
    const res = await getUserOffers()
    const list = res.data || []
    const start = (pagination.current - 1) * pagination.size
    const end = start + pagination.size
    offers.value = list.slice(start, end)
    pagination.total = list.length
  } catch (error) {
    console.error(error)
    const text = [error?.response?.status, error?.response?.data?.message, error?.message].filter(Boolean).join(' ')
    if (!/(401|403|权限|无权|没有权限|forbidden|permission|鏃犳潈|娌℃湁鏉冮檺)/i.test(text)) {
      ElMessage.error('获取 Offer 列表失败')
    }
  } finally {
    loading.value = false
  }
}

const getOfferType = (status) => {
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

const respondOffer = async (row, status) => {
  const statusName = status === 1 ? '接受' : '拒绝'
  try {
    const input = await ElMessageBox.prompt('可填写说明（可选）', `${statusName} Offer`, {
      inputType: 'textarea',
      inputPlaceholder: '说明',
      confirmButtonText: '确认',
      cancelButtonText: '取消'
    })

    await respondOfferApi(row.id, {
      status,
      responseComment: input.value
    })

    ElMessage.success(`已${statusName} Offer`)
    await fetchOffers()
  } catch (error) {
    if (error?.action === 'cancel' || error?.action === 'close' || error === 'cancel' || error === 'close') {
      return
    }
    console.error(error)
    ElMessage.error('Offer 响应失败')
    await fetchOffers()
  }
}

const viewOffer = (row) => {
  selectedOffer.value = row
  detailVisible.value = true
}

const handleSizeChange = (size) => {
  pagination.size = size
  pagination.current = 1
  fetchOffers()
}

const handlePageChange = (page) => {
  pagination.current = page
  fetchOffers()
}

onMounted(() => {
  fetchOffers()
})
</script>

<style scoped>
.offers-page {
  max-width: 1100px;
  margin: 0 auto;
}

.pagination {
  margin-top: 20px;
  text-align: right;
}
</style>
