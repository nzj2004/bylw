<template>
  <div class="company-applications">
    <el-card>
      <template #header>
        <h3>简历管理</h3>
      </template>
      
      <!-- 搜索筛选区域 -->
      <div class="search-filter">
        <el-row :gutter="16">
          <el-col :span="6">
            <el-input
              v-model="searchForm.keyword"
              placeholder="搜索职位名称"
              clearable
              @keyup.enter="handleSearch"
            />
          </el-col>
          <el-col :span="6">
            <el-cascader
              v-model="searchForm.category"
              :options="categoryOptions"
              :props="{ emitPath: false, checkStrictly: true }"
              placeholder="职位类别"
              clearable
              @change="handleSearch"
              style="width: 100%"
            />
          </el-col>
          <el-col :span="4">
            <el-select v-model="searchForm.status" placeholder="状态" clearable @change="handleSearch" style="width: 100%">
              <el-option label="全部" :value="null" />
              <el-option label="待查看" :value="0" />
              <el-option label="已查看" :value="1" />
              <el-option label="感兴趣" :value="2" />
              <el-option label="不合适" :value="3" />
              <el-option label="面试中" :value="4" />
              <el-option label="面试失败" :value="5" />
              <el-option label="Offer待确认" :value="6" />
              <el-option label="Offer接受" :value="7" />
              <el-option label="Offer拒绝" :value="8" />
              <el-option label="已入职" :value="9" />
              <el-option label="面试通过" :value="10" />
              <el-option label="Offer已过期" :value="11" />
            </el-select>
          </el-col>
          <el-col :span="4">
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-col>
        </el-row>
      </div>
      
      <el-table :data="applications" stripe v-loading="loading">
        <el-table-column prop="jobTitle" label="应聘职位" />
        <el-table-column prop="resumeName" label="求职者" />
        <el-table-column prop="applyTime" label="投递时间" />
        <el-table-column prop="status" label="状态">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ row.statusName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleView(row)">查看简历</el-button>
            <el-button type="warning" size="small" @click="openProcess(row)">流程处理</el-button>
            <el-button type="success" size="small" :disabled="!canScreen(row)" @click="handleStatus(row, 2)">感兴趣</el-button>
            <el-button type="danger" size="small" :disabled="!canScreen(row)" @click="handleStatus(row, 3)">不合适</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <el-empty v-if="applications.length === 0 && !loading" description="暂无投递记录" />
      
      <!-- 分页 -->
      <div class="pagination" v-if="pagination.total > 0">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 简历详情弹窗 -->
    <el-dialog v-model="resumeDialogVisible" title="简历详情" width="700px">
      <div v-if="resumeDetail" class="resume-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="姓名">{{ resumeDetail.realName }}</el-descriptions-item>
          <el-descriptions-item label="性别">{{ resumeDetail.genderName }}</el-descriptions-item>
          <el-descriptions-item label="出生日期">{{ resumeDetail.birthDate }}</el-descriptions-item>
          <el-descriptions-item label="毕业年份">{{ resumeDetail.graduationYear }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ resumeDetail.phone }}</el-descriptions-item>
          <el-descriptions-item label="邮箱">{{ resumeDetail.email }}</el-descriptions-item>
          <el-descriptions-item label="学历">{{ resumeDetail.education }}</el-descriptions-item>
          <el-descriptions-item label="毕业院校">{{ resumeDetail.school }}</el-descriptions-item>
          <el-descriptions-item label="专业" :span="2">{{ resumeDetail.major }}</el-descriptions-item>
          <el-descriptions-item label="期望城市">{{ resumeDetail.expectedCity }}</el-descriptions-item>
          <el-descriptions-item label="期望薪资">
            {{ resumeDetail.expectedSalaryMin }}-{{ resumeDetail.expectedSalaryMax }}K
          </el-descriptions-item>
          <el-descriptions-item label="技能特长" :span="2">{{ resumeDetail.skills || '暂无' }}</el-descriptions-item>
          <el-descriptions-item label="工作经历" :span="2">
            <div class="desc-content">{{ resumeDetail.workExperience || '暂无' }}</div>
          </el-descriptions-item>
          <el-descriptions-item label="项目经历" :span="2">
            <div class="desc-content">{{ resumeDetail.projectExp || '暂无' }}</div>
          </el-descriptions-item>
          <el-descriptions-item label="自我评价" :span="2">
            <div class="desc-content">{{ resumeDetail.selfEval || '暂无' }}</div>
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button @click="resumeDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getCompanyApplications, updateApplicationStatus, getResumeById } from '../../api/user'
import { JOB_CATEGORY_OPTIONS, resolveCategoryFilterValue } from '../../constants/jobCategoryOptions'

const applications = ref([])
const loading = ref(false)
const resumeDialogVisible = ref(false)
const resumeDetail = ref(null)

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const searchForm = reactive({
  keyword: '',
  category: '',
  status: null
})

const router = useRouter()

const categoryOptions = JOB_CATEGORY_OPTIONS

const fetchApplications = async () => {
  try {
    loading.value = true
    const category = resolveCategoryFilterValue(searchForm.category)
    const res = await getCompanyApplications({
      current: pagination.current,
      size: pagination.size,
      keyword: searchForm.keyword || undefined,
      category: category || undefined,
      status: searchForm.status !== null ? searchForm.status : undefined
    })
    if (res.data.records) {
      applications.value = res.data.records
      pagination.total = res.data.total || 0
    } else {
      applications.value = res.data || []
      pagination.total = applications.value.length
    }
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  fetchApplications()
}

const resetSearch = () => {
  searchForm.keyword = ''
  searchForm.category = ''
  searchForm.status = null
  pagination.current = 1
  fetchApplications()
}

const handleSizeChange = (size) => {
  pagination.size = size
  pagination.current = 1
  fetchApplications()
}

const handlePageChange = (page) => {
  pagination.current = page
  fetchApplications()
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

const canScreen = (row) => [0, 1, 2].includes(row?.status)

const handleView = async (row) => {
  if (!row.resumeId) {
    ElMessage.warning('该投递无关联简历')
    return
  }
  try {
    const res = await getResumeById(row.resumeId, row.id)
    resumeDetail.value = res.data
    resumeDialogVisible.value = true
  } catch (error) {
    ElMessage.error('获取简历信息失败')
  }
}

const handleStatus = async (row, status) => {
  try {
    await updateApplicationStatus(row.id, status)
    const statusText = status === 2 ? '已标记为感兴趣' : '已标记为不合适'
    ElMessage.success(statusText)
    fetchApplications()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const openProcess = (row) => {
  if (!row?.id) {
    ElMessage.warning('申请记录异常')
    return
  }
  router.push(`/company/applications/${row.id}/process`)
}

onMounted(() => {
  fetchApplications()
})
</script>

<style scoped>
.company-applications {
  max-width: 1200px;
  margin: 0 auto;
}

.search-filter {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  text-align: right;
}

.resume-detail {
  max-height: 60vh;
  overflow-y: auto;
}

.desc-content {
  white-space: pre-line;
  line-height: 1.6;
}
</style>


