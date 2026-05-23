<template>
  <div class="company-jobs">
    <el-card>
      <template #header>
        <div class="card-header">
          <h3>职位管理</h3>
          <el-button type="primary" @click="handleAdd">发布职位</el-button>
        </div>
      </template>

      <!-- 搜索和筛选区域 -->
      <div class="search-filter">
        <el-row :gutter="16">
          <el-col :span="8">
            <el-input
              v-model="searchForm.keyword"
              placeholder="搜索职位标题"
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
              <el-option label="待审核" :value="0" />
              <el-option label="已发布" :value="1" />
              <el-option label="已拒绝" :value="2" />
              <el-option label="已下架" :value="3" />
            </el-select>
          </el-col>
          <el-col :span="3">
            <el-button @click="resetSearch">重置</el-button>
          </el-col>
        </el-row>
      </div>

      <el-table :data="jobs" stripe v-loading="loading">
        <el-table-column prop="title" label="职位标题" />
        <el-table-column prop="category" label="类别" />
        <el-table-column prop="salaryRange" label="薪资" />
        <el-table-column prop="workCity" label="城市" />
        <el-table-column prop="status" label="状态">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ row.statusName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="viewCount" label="浏览" width="80" />
        <el-table-column prop="applyCount" label="投递数" width="80" />
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="jobs.length === 0 && !loading" description="暂无岗位，点击上方发布职位" />

      <div class="pagination" v-if="pagination.total > 0">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑职位' : '发布职位'" width="700px">
      <el-form :model="form" label-position="top">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="职位标题">
              <el-input v-model="form.title" placeholder="请输入职位标题" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="职位类别">
              <el-cascader
                v-model="form.category"
                :options="categoryOptions"
                :props="{ emitPath: false, checkStrictly: true }"
                placeholder="请选择类别"
                style="width: 100%"
                clearable
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="最低薪资(K)">
              <el-input-number v-model="form.salaryMin" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="最高薪资(K)">
              <el-input-number v-model="form.salaryMax" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="工作城市">
              <el-input v-model="form.workCity" placeholder="请输入工作城市" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="经验要求">
              <el-select v-model="form.experience" placeholder="请选择" style="width: 100%">
                <el-option label="不限经验" value="不限经验" />
                <el-option label="1年以下" value="1年以下" />
                <el-option label="1-3年" value="1-3年" />
                <el-option label="3-5年" value="3-5年" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="学历要求">
              <el-select v-model="form.education" placeholder="请选择" style="width: 100%">
                <el-option label="大专" value="大专" />
                <el-option label="本科" value="本科" />
                <el-option label="硕士" value="硕士" />
                <el-option label="博士" value="博士" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="职位描述">
          <el-input v-model="form.jobDesc" type="textarea" :rows="4" placeholder="请输入职位描述" />
        </el-form-item>

        <el-form-item label="岗位要求">
          <el-input v-model="form.requirements" type="textarea" :rows="4" placeholder="请输入岗位要求" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { listCompanyJobs, createJob, updateJob, deleteJob } from '../../api/job'
import { useUserStore } from '../../store/user'
import { JOB_CATEGORY_OPTIONS, resolveCategoryFilterValue } from '../../constants/jobCategoryOptions'

const userStore = useUserStore()
const jobs = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const editingId = ref(null)

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

// 职位分类选项
const categoryOptions = JOB_CATEGORY_OPTIONS

const form = reactive({
  title: '',
  category: '',
  salaryMin: null,
  salaryMax: null,
  workCity: '',
  experience: '',
  education: '',
  jobDesc: '',
  requirements: ''
})

const resetForm = () => {
  form.title = ''
  form.category = ''
  form.salaryMin = null
  form.salaryMax = null
  form.workCity = ''
  form.experience = ''
  form.education = ''
  form.jobDesc = ''
  form.requirements = ''
  editingId.value = null
}

const fetchJobs = async () => {
  try {
    loading.value = true
    const companyId = userStore.userInfo?.id
    if (!companyId) return
    const category = resolveCategoryFilterValue(searchForm.category)
    const params = {
      current: pagination.current,
      size: pagination.size,
      keyword: searchForm.keyword || undefined,
      category: category || undefined,
      status: searchForm.status !== null ? searchForm.status : undefined
    }
    const res = await listCompanyJobs(companyId, params)
    jobs.value = res.data.records || []
    pagination.total = res.data.total || 0
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
  searchForm.status = null
  pagination.current = 1
  fetchJobs()
}

const handleSizeChange = (size) => {
  pagination.size = size
  pagination.current = 1
  fetchJobs()
}

const handlePageChange = (page) => {
  pagination.current = page
  fetchJobs()
}

const getStatusType = (status) => {
  const types = { 0: 'warning', 1: 'success', 2: 'danger', 3: 'info' }
  return types[status] || 'info'
}

const handleAdd = () => {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  editingId.value = row.id
  Object.keys(form).forEach(key => {
    if (row[key] !== undefined) {
      form[key] = row[key]
    }
  })
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该职位吗？', '提示', {
      type: 'warning'
    })
    await deleteJob(row.id)
    ElMessage.success('删除成功')
    fetchJobs()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleSubmit = async () => {
  if (!form.title || !form.category || !form.workCity) {
    ElMessage.warning('请填写必填项')
    return
  }

  try {
    loading.value = true
    const data = { ...form }

    if (isEdit.value) {
      await updateJob(editingId.value, data)
      ElMessage.success('编辑成功')
    } else {
      await createJob(data)
      ElMessage.success('发布成功')
    }
    dialogVisible.value = false
    fetchJobs()
  } catch (error) {
    ElMessage.error(isEdit.value ? '编辑失败' : '发布失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchJobs()
})
</script>

<style scoped>
.company-jobs {
  max-width: 1200px;
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

.search-filter {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  text-align: right;
}
</style>
