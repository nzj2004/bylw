<template>
  <div class="job-list-container">
    <div class="search-section">
      <el-card>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-input
              v-model="searchForm.keyword"
              placeholder="搜索职位"
              clearable
              @keyup.enter="handleSearch"
            >
              <template #append>
                <el-button @click="handleSearch">
                  <el-icon><Search /></el-icon>
                </el-button>
              </template>
            </el-input>
          </el-col>

          <el-col :span="5">
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

          <el-col :span="5">
            <el-select
              v-model="searchForm.city"
              placeholder="工作城市"
              clearable
              filterable
              @change="handleSearch"
            >
              <el-option label="北京" value="北京" />
              <el-option label="上海" value="上海" />
              <el-option label="广州" value="广州" />
              <el-option label="深圳" value="深圳" />
            </el-select>
          </el-col>

          <el-col :span="3">
            <el-select
              v-model="searchForm.experience"
              placeholder="经验要求"
              clearable
              @change="handleSearch"
            >
              <el-option label="不限" value="" />
              <el-option label="1-3年" value="1-3年" />
              <el-option label="3-5年" value="3-5年" />
              <el-option label="5-10年" value="5-10年" />
            </el-select>
          </el-col>

          <el-col :span="3">
            <el-select
              v-model="searchForm.education"
              placeholder="学历要求"
              clearable
              @change="handleSearch"
            >
              <el-option label="不限" value="" />
              <el-option label="大专" value="大专" />
              <el-option label="本科" value="本科" />
              <el-option label="硕士" value="硕士" />
            </el-select>
          </el-col>
        </el-row>
      </el-card>
    </div>

    <div class="job-list">
      <el-card
        v-for="job in jobList"
        :key="job.id"
        class="job-item"
        shadow="hover"
        @click="goToDetail(job.id)"
      >
        <div class="job-main">
          <div class="job-info">
            <h3 class="job-title">{{ job.title }}</h3>
            <div class="job-tags">
              <el-tag size="small">{{ job.workCity }}</el-tag>
              <el-tag size="small" type="info">{{ job.experience || '不限' }}</el-tag>
              <el-tag size="small" type="success">{{ job.education || '不限' }}</el-tag>
              <el-tag size="small" type="warning">{{ job.jobTypeName || '其它' }}</el-tag>
            </div>
            <div class="job-salary">{{ job.salaryRange }}</div>
          </div>
          <div class="company-info">
            <div class="company-name">
              <el-avatar :size="40" :src="getImageUrl(job.companyLogo)" />
              <span>{{ job.companyName }}</span>
            </div>
            <div class="job-meta">
              <span>浏览 {{ job.viewCount }}</span>
              <span>投递 {{ job.applyCount }}</span>
            </div>
          </div>
        </div>
      </el-card>

      <el-empty v-if="jobList.length === 0" description="暂无职位" />
    </div>

    <div class="pagination">
      <el-pagination
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @current-change="handlePageChange"
        @size-change="handleSizeChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import { listJobs } from '../../api/job'
import { getImageUrl } from '../../utils/image'
import { JOB_CATEGORY_OPTIONS, resolveCategoryFilterValue } from '../../constants/jobCategoryOptions'

const route = useRoute()
const router = useRouter()

const searchForm = reactive({
  keyword: '',
  category: '',
  city: '',
  experience: '',
  education: ''
})

const categoryOptions = JOB_CATEGORY_OPTIONS

const toTextFilterValue = (value) => {
  if (Array.isArray(value)) {
    value = value[0] || ''
  }
  if (value == null) {
    return ''
  }
  return String(value).trim()
}

const toPositiveNumber = (value, fallback) => {
  const raw = Array.isArray(value) ? value[0] : value
  const num = Number(raw)
  if (Number.isInteger(num) && num > 0) {
    return num
  }
  return fallback
}

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const syncSearchFormFromRoute = () => {
  searchForm.keyword = toTextFilterValue(route.query.keyword)
  searchForm.category = resolveCategoryFilterValue(route.query.category)
  searchForm.city = toTextFilterValue(route.query.city)
  searchForm.experience = toTextFilterValue(route.query.experience)
  searchForm.education = toTextFilterValue(route.query.education)
  pagination.current = toPositiveNumber(route.query.current, pagination.current)
  pagination.size = toPositiveNumber(route.query.size, pagination.size)
}

const buildRouteQuery = () => {
  const query = {
    current: String(pagination.current),
    size: String(pagination.size)
  }

  const category = resolveCategoryFilterValue(searchForm.category)
  if (searchForm.keyword) query.keyword = searchForm.keyword.trim()
  if (category) query.category = category
  if (searchForm.city) query.city = searchForm.city
  if (searchForm.experience) query.experience = searchForm.experience
  if (searchForm.education) query.education = searchForm.education

  return query
}

const syncRouteQuery = () => {
  router.push({ path: '/jobs', query: buildRouteQuery() })
}

const jobList = ref([])

const fetchJobs = async () => {
  try {
    const category = resolveCategoryFilterValue(searchForm.category)
    const params = {
      ...searchForm,
      category,
      current: pagination.current,
      size: pagination.size
    }
    if (!params.category) {
      delete params.category
    }

    const res = await listJobs(params)
    jobList.value = res.data.records
    pagination.total = res.data.total
  } catch (error) {
    console.error(error)
  }
}

const handleSearch = () => {
  pagination.current = 1
  syncRouteQuery()
}

const handlePageChange = (page) => {
  pagination.current = page
  syncRouteQuery()
}

const handleSizeChange = (size) => {
  pagination.size = size
  pagination.current = 1
  syncRouteQuery()
}

const goToDetail = (id) => {
  router.push(`/job/${id}`)
}

watch(
  () => route.query,
  () => {
    syncSearchFormFromRoute()
    fetchJobs()
  },
  { immediate: true, deep: true }
)
</script>

<style scoped>
.job-list-container {
  max-width: 1200px;
  margin: 0 auto;
}

.search-section {
  margin-bottom: 20px;
}

.job-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.job-item {
  cursor: pointer;
}

.job-main {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.job-info {
  flex: 1;
}

.job-title {
  margin: 0 0 12px 0;
  font-size: 18px;
  color: #303133;
}

.job-tags {
  margin-bottom: 12px;
}

.job-tags .el-tag {
  margin-right: 8px;
}

.job-salary {
  color: #f56c6c;
  font-size: 16px;
  font-weight: bold;
}

.company-info {
  text-align: right;
}

.company-name {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  color: #606266;
}

.job-meta {
  color: #909399;
  font-size: 13px;
}

.job-meta span {
  margin-left: 16px;
}

.pagination {
  margin-top: 24px;
  text-align: center;
}
</style>
