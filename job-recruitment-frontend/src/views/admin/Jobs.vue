<template>
  <div class="admin-jobs">
    <el-card>
      <template #header>
        <div class="card-header">
          <h3>职位管理</h3>
        </div>
      </template>

      <div class="search-bar">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-input
              v-model="searchForm.keyword"
              placeholder="搜索职位标题/企业"
              clearable
              @keyup.enter="handleSearch"
            />
          </el-col>
          <el-col :span="6">
            <el-cascader
              v-model="searchForm.category"
              :options="categoryOptions"
              placeholder="职位类别"
              clearable
              :props="{ checkStrictly: true }"
              @change="handleSearch"
              style="width: 100%"
            />
          </el-col>
          <el-col :span="4">
            <el-select v-model="searchForm.city" placeholder="工作城市" clearable @change="handleSearch" filterable>
              <el-option-group label="华北地区">
                <el-option label="北京" value="北京" />
                <el-option label="天津" value="天津" />
                <el-option label="石家庄" value="石家庄" />
                <el-option label="沈阳" value="沈阳" />
                <el-option label="呼和浩特" value="呼和浩特" />
                <el-option label="郑州" value="郑州" />
              </el-option-group>
              <el-option-group label="华东地区">
                <el-option label="上海" value="上海" />
                <el-option label="南京" value="南京" />
                <el-option label="杭州" value="杭州" />
                <el-option label="合肥" value="合肥" />
                <el-option label="济南" value="济南" />
              </el-option-group>
              <el-option-group label="华南地区">
                <el-option label="广州" value="广州" />
                <el-option label="深圳" value="深圳" />
                <el-option label="福州" value="福州" />
                <el-option label="厦门" value="厦门" />
              </el-option-group>
            </el-select>
          </el-col>
          <el-col :span="4">
            <el-select v-model="searchForm.status" placeholder="岗位状态" clearable @change="handleSearch">
              <el-option label="全部" :value="null" />
              <el-option label="待审核" :value="0" />
              <el-option label="已发布" :value="1" />
              <el-option label="已拒绝" :value="2" />
              <el-option label="已下架" :value="3" />
            </el-select>
          </el-col>
          <el-col :span="4">
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-col>
        </el-row>
      </div>

      <el-table :data="jobList" v-loading="loading" stripe border>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="title" label="职位标题" min-width="150" show-overflow-tooltip />
        <el-table-column prop="companyName" label="公司" min-width="120" show-overflow-tooltip />
        <el-table-column prop="category" label="职位类别" width="120" />
        <el-table-column prop="workCity" label="城市" width="100" />
        <el-table-column prop="salaryRange" label="薪资" width="120" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="viewCount" label="浏览数" width="70" />
        <el-table-column prop="applyCount" label="投递数" width="70" />
        <el-table-column prop="createTime" label="发布时间" width="160" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="viewDetail(row)">查看</el-button>
            <el-button
              :type="row.status === 3 ? 'success' : 'warning'"
              size="small"
              @click="toggleStatus(row)"
            >
              {{ row.status === 3 ? '上架' : '下架' }}
            </el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="jobList.length === 0 && !loading" description="暂无职位" />

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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listJobs, deleteJob, toggleJobStatus } from '../../api/job'
import { JOB_CATEGORY_OPTIONS, resolveCategoryFilterValue } from '../../constants/jobCategoryOptions'

const router = useRouter()
const loading = ref(false)
const jobList = ref([])

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const searchForm = reactive({
  keyword: '',
  category: '',
  city: '',
  status: null
})

const categoryOptions = JOB_CATEGORY_OPTIONS

const getStatusType = (status) => {
  const types = { 0: 'warning', 1: 'success', 2: 'danger', 3: 'info' }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = { 0: '待审核', 1: '已发布', 2: '已拒绝', 3: '已下架' }
  return texts[status] || '未知'
}

const fetchJobs = async () => {
  loading.value = true
  try {
    const category = resolveCategoryFilterValue(searchForm.category)

    const res = await listJobs({
      current: pagination.current,
      size: pagination.size,
      keyword: searchForm.keyword || undefined,
      category: category || undefined,
      city: searchForm.city || undefined,
      status: searchForm.status !== null ? searchForm.status : undefined,
      includeAll: searchForm.status === null ? true : undefined
    })
    jobList.value = res.data.records
    pagination.total = res.data.total
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  fetchJobs()
}

const resetSearch = () => {
  searchForm.keyword = ''
  searchForm.category = ''
  searchForm.city = ''
  searchForm.status = null
  pagination.current = 1
  fetchJobs()
}

const handlePageChange = (page) => {
  pagination.current = page
  fetchJobs()
}

const handleSizeChange = (size) => {
  pagination.size = size
  pagination.current = 1
  fetchJobs()
}

const viewDetail = (row) => {
  router.push(`/job/${row.id}`)
}

const toggleStatus = (row) => {
  const newStatus = row.status === 3 ? 1 : 3
  const action = newStatus === 3 ? '下架' : '上架'
  ElMessageBox.confirm(`确定${action}该职位？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await toggleJobStatus(row.id, newStatus)
      ElMessage.success(`${action}成功`)
      fetchJobs()
    } catch (error) {
      console.error(error)
      ElMessage.error(`${action}失败`)
    }
  })
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该职位吗？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'danger'
  }).then(async () => {
    try {
      await deleteJob(row.id)
      ElMessage.success('删除成功')
      fetchJobs()
    } catch (error) {
      console.error(error)
    }
  })
}

onMounted(() => {
  fetchJobs()
})
</script>

<style scoped>
.admin-jobs {
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
</style>
