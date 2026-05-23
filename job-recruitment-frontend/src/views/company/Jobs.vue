<template>
  <div class="company-jobs">
    <el-card>
      <template #header>
        <div class="card-header">
          <h3>鑱屼綅绠＄悊</h3>
          <el-button type="primary" @click="handleAdd">鍙戝竷鑱屼綅</el-button>
        </div>
      </template>
      
      <!-- 鎼滅储鍜岀瓫閫夊尯鍩?-->
      <div class="search-filter">
        <el-row :gutter="16">
          <el-col :span="8">
            <el-input
              v-model="searchForm.keyword"
              placeholder="鎼滅储鑱屼綅鏍囬"
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
              placeholder="鑱屼綅绫诲埆"
              clearable
              @change="handleSearch"
              style="width: 100%"
            />
          </el-col>
          <el-col :span="4">
            <el-select v-model="searchForm.status" placeholder="状态" clearable @change="handleSearch" style="width: 100%">
              <el-option label="鍏ㄩ儴" :value="null" />
              <el-option label="待审核" :value="0" />
              <el-option label="已发布" :value="1" />
              <el-option label="已拒绝" :value="2" />
              <el-option label="已下架" :value="3" />
            </el-select>
          </el-col>
          <el-col :span="3">
            <el-button @click="resetSearch">閲嶇疆</el-button>
          </el-col>
        </el-row>
      </div>
      
      <el-table :data="jobs" stripe v-loading="loading">
        <el-table-column prop="title" label="鑱屼綅鏍囬" />
        <el-table-column prop="category" label="绫诲埆" />
        <el-table-column prop="salaryRange" label="钖祫" />
        <el-table-column prop="workCity" label="鍩庡競" />
        <el-table-column prop="status" label="状态">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ row.statusName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="viewCount" label="娴忚" width="80" />
        <el-table-column prop="applyCount" label="投递数" width="80" />
        <el-table-column label="鎿嶄綔" width="200">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEdit(row)">缂栬緫</el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">鍒犻櫎</el-button>
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
    
    <el-dialog v-model="dialogVisible" :title="isEdit ? '缂栬緫鑱屼綅' : '鍙戝竷鑱屼綅'" width="700px">
      <el-form :model="form" label-position="top">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="鑱屼綅鏍囬">
              <el-input v-model="form.title" placeholder="请输入职位标题" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="鑱屼綅绫诲埆">
              <el-cascader 
                v-model="form.category" 
                :options="categoryOptions" 
                :props="{ emitPath: false, checkStrictly: true }"
                placeholder="璇烽€夋嫨绫诲埆" 
                style="width: 100%" 
                clearable
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="鏈€浣庤柂璧?K)">
              <el-input-number v-model="form.salaryMin" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="鏈€楂樿柂璧?K)">
              <el-input-number v-model="form.salaryMax" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="宸ヤ綔鍩庡競">
               <el-input v-model="form.workCity" placeholder="请输入工作城市" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="缁忛獙瑕佹眰">
              <el-select v-model="form.experience" placeholder="璇烽€夋嫨" style="width: 100%">
                <el-option label="不限经验" value="不限经验" />
                <el-option label="1年以下" value="1年以下" />
                <el-option label="1-3年" value="1-3年" />
                <el-option label="3-5年" value="3-5年" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="瀛﹀巻瑕佹眰">
              <el-select v-model="form.education" placeholder="璇烽€夋嫨" style="width: 100%">
                <el-option label="澶т笓" value="澶т笓" />
                <el-option label="鏈" value="鏈" />
                <el-option label="纭曞＋" value="纭曞＋" />
                <el-option label="鍗氬＋" value="鍗氬＋" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="鑱屼綅鎻忚堪">
          <el-input v-model="form.jobDesc" type="textarea" :rows="4" placeholder="请输入职位描述" />
        </el-form-item>
        
        <el-form-item label="宀椾綅瑕佹眰">
          <el-input v-model="form.requirements" type="textarea" :rows="4" placeholder="请输入岗位要求" />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="dialogVisible = false">鍙栨秷</el-button>
        <el-button type="primary" @click="handleSubmit">纭畾</el-button>
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

// 鑱屼綅鍒嗙被閫夐」
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
    await ElMessageBox.confirm('纭畾瑕佸垹闄よ鑱屼綅鍚楋紵', '鎻愮ず', {
      type: 'warning'
    })
    await deleteJob(row.id)
    ElMessage.success('鍒犻櫎鎴愬姛')
    fetchJobs()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('鍒犻櫎澶辫触')
    }
  }
}

const handleSubmit = async () => {
  if (!form.title || !form.category || !form.workCity) {
    ElMessage.warning('璇峰～鍐欏繀濉」')
    return
  }
  
  try {
    loading.value = true
    const data = { ...form }
    
    if (isEdit.value) {
      await updateJob(editingId.value, data)
      ElMessage.success('缂栬緫鎴愬姛')
    } else {
      await createJob(data)
      ElMessage.success('发布成功')
    }
    dialogVisible.value = false
    fetchJobs()
  } catch (error) {
    ElMessage.error(isEdit.value ? '缂栬緫澶辫触' : '鍙戝竷澶辫触')
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
