<template>
  <div class="home-container">
    <div class="banner">
      <div class="banner-content">
        <h1>找到理想的工作</h1>
        <p>连接优秀毕业生与优质企业，开启职业新篇章</p>
        <div class="search-wrapper" v-click-outside="hideHistory">
          <el-input
            ref="searchInputRef"
            v-model="searchKeyword"
            placeholder="搜索职位、公司"
            size="large"
            class="search-input"
            @keyup.enter="handleSearch"
            @focus="showHistoryDropdown"
            @input="onSearchInput"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
            <template #append>
              <el-button type="primary" @click="handleSearch">
                <el-icon><Search /></el-icon>
              </el-button>
            </template>
          </el-input>
          
          <!-- 搜索历史下拉框 -->
          <div class="search-history-dropdown" v-show="showHistory && (filteredHistory.length > 0 || searchKeyword)">
            <!-- 当前输入项 -->
            <div 
              v-if="searchKeyword && !filteredHistory.includes(searchKeyword)" 
              class="history-item current-input"
              @click="handleSearch"
            >
              <el-icon><Search /></el-icon>
              <span>{{ searchKeyword }}</span>
            </div>
            
            <!-- 历史记录列表 -->
            <div 
              v-for="(item, index) in filteredHistory" 
              :key="index" 
              class="history-item"
              @click="selectHistory(item)"
            >
              <el-icon><Clock /></el-icon>
              <span class="history-text">{{ item }}</span>
              <el-icon class="delete-icon" @click.stop="removeHistory(index)"><Close /></el-icon>
            </div>
            
            <!-- 清空历史 -->
            <div v-if="searchHistory.length > 0" class="history-footer">
              <el-button type="primary" link size="small" @click="clearHistory">清空搜索历史</el-button>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <div class="stats-section">
      <el-row :gutter="20">
        <el-col :span="6">
          <div class="stat-card">
            <el-icon size="48" color="#409EFF"><Briefcase /></el-icon>
            <div class="stat-number">{{ stats.jobCount }}+</div>
            <div class="stat-label">在招职位</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <el-icon size="48" color="#67C23A"><OfficeBuilding /></el-icon>
            <div class="stat-number">{{ stats.companyCount }}+</div>
            <div class="stat-label">入驻企业</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <el-icon size="48" color="#E6A23C"><User /></el-icon>
            <div class="stat-number">{{ stats.userCount }}+</div>
            <div class="stat-label">注册用户</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <el-icon size="48" color="#F56C6C"><DocumentChecked /></el-icon>
            <div class="stat-number">{{ stats.applyCount }}+</div>
            <div class="stat-label">成功投递</div>
          </div>
        </el-col>
      </el-row>
    </div>
    
    <div class="hot-jobs-section">
      <div class="section-header">
        <h2>热门职位</h2>
        <el-link type="primary" @click="goToJobs">查看更多</el-link>
      </div>
      
      <el-row :gutter="20">
        <el-col :span="8" v-for="job in hotJobs" :key="job.id">
          <el-card class="job-card" shadow="hover" @click="goToJobDetail(job.id)">
            <div class="job-header">
              <h3>{{ job.title }}</h3>
              <span class="salary">{{ job.salaryRange }}</span>
            </div>
            <div class="job-tags">
              <el-tag size="small">{{ job.workCity }}</el-tag>
              <el-tag size="small" type="info">{{ job.experience }}</el-tag>
              <el-tag size="small" type="success">{{ job.education }}</el-tag>
            </div>
            <div class="job-company">
              <el-avatar :size="32" :src="getImageUrl(job.companyLogo)" />
              <span>{{ job.companyName }}</span>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search, Briefcase, OfficeBuilding, User, DocumentChecked, Clock, Close } from '@element-plus/icons-vue'
import { listHotJobs, listJobs } from '../../api/job'
import { getImageUrl } from '../../utils/image'

const router = useRouter()
const searchInputRef = ref(null)
const searchKeyword = ref('')
const showHistory = ref(false)
const searchHistory = ref([])

const HISTORY_KEY = 'job_search_history'
const MAX_HISTORY = 10

// 从 localStorage 加载搜索历史
const loadHistory = () => {
  try {
    const saved = localStorage.getItem(HISTORY_KEY)
    if (saved) {
      searchHistory.value = JSON.parse(saved)
    }
  } catch (e) {
    searchHistory.value = []
  }
}

// 保存搜索历史到 localStorage
const saveHistory = () => {
  localStorage.setItem(HISTORY_KEY, JSON.stringify(searchHistory.value))
}

// 添加搜索记录
const addToHistory = (keyword) => {
  if (!keyword || !keyword.trim()) return
  
  const trimmed = keyword.trim()
  // 移除重复项
  const index = searchHistory.value.indexOf(trimmed)
  if (index > -1) {
    searchHistory.value.splice(index, 1)
  }
  // 添加到开头
  searchHistory.value.unshift(trimmed)
  // 限制数量
  if (searchHistory.value.length > MAX_HISTORY) {
    searchHistory.value = searchHistory.value.slice(0, MAX_HISTORY)
  }
  saveHistory()
}

// 删除单条历史
const removeHistory = (index) => {
  searchHistory.value.splice(index, 1)
  saveHistory()
}

// 清空历史
const clearHistory = () => {
  searchHistory.value = []
  saveHistory()
  showHistory.value = false
}

// 选择历史记录
const selectHistory = (item) => {
  searchKeyword.value = item
  showHistory.value = false
  handleSearch()
}

// 根据输入过滤历史记录
const filteredHistory = computed(() => {
  if (!searchKeyword.value) {
    return searchHistory.value
  }
  return searchHistory.value.filter(item => 
    item.toLowerCase().includes(searchKeyword.value.toLowerCase())
  )
})

// 显示历史下拉框
const showHistoryDropdown = () => {
  showHistory.value = true
}

// 隐藏历史下拉框
const hideHistory = () => {
  showHistory.value = false
}

// 输入时显示下拉框
const onSearchInput = () => {
  showHistory.value = true
}

const stats = ref({
  jobCount: 1200,
  companyCount: 580,
  userCount: 8600,
  applyCount: 3200
})
const hotJobs = ref([])

const getRecords = (res) => {
  return res?.data?.records || res?.records || res?.data?.data?.records || []
}

const fetchHotJobs = async () => {
  try {
    const res = await listHotJobs({ current: 1, size: 6 })
    const records = getRecords(res)
    if (records.length > 0) {
      hotJobs.value = records
      return
    }

    const fallback = await listJobs({ current: 1, size: 6 })
    hotJobs.value = getRecords(fallback)
  } catch (error) {
    console.error(error)
    try {
      const fallback = await listJobs({ current: 1, size: 6 })
      hotJobs.value = getRecords(fallback)
    } catch (fallbackError) {
      console.error(fallbackError)
    }
  }
}

const handleSearch = () => {
  if (searchKeyword.value && searchKeyword.value.trim()) {
    addToHistory(searchKeyword.value)
  }
  showHistory.value = false
  router.push({
    path: '/jobs',
    query: { keyword: searchKeyword.value }
  })
}

const goToJobs = () => {
  router.push('/jobs')
}

const goToJobDetail = (id) => {
  router.push(`/job/${id}`)
}

onMounted(() => {
  loadHistory()
  fetchHotJobs()
})

// 自定义指令：点击外部关闭
const vClickOutside = {
  mounted(el, binding) {
    el._clickOutside = (event) => {
      if (!(el === event.target || el.contains(event.target))) {
        binding.value()
      }
    }
    document.addEventListener('click', el._clickOutside)
  },
  unmounted(el) {
    document.removeEventListener('click', el._clickOutside)
  }
}
</script>

<style scoped>
.home-container {
  max-width: 1200px;
  margin: 0 auto;
}

.banner {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  padding: 60px 40px;
  text-align: center;
  color: #fff;
  margin-bottom: 40px;
}

.banner-content h1 {
  font-size: 36px;
  margin-bottom: 16px;
}

.banner-content p {
  font-size: 18px;
  margin-bottom: 32px;
  opacity: 0.9;
}

.search-input {
  max-width: 600px;
  margin: 0 auto;
}

.search-wrapper {
  position: relative;
  max-width: 600px;
  margin: 0 auto;
}

.search-input :deep(.el-input__wrapper) {
  padding: 4px 16px;
  background: #fff;
}

.search-history-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  margin-top: 4px;
  max-height: 400px;
  overflow-y: auto;
  z-index: 1000;
}

.history-item {
  display: flex;
  align-items: center;
  padding: 10px 16px;
  cursor: pointer;
  transition: background-color 0.2s;
  color: #606266;
  text-align: left;
}

.history-item:hover {
  background-color: #f5f7fa;
}

.history-item .el-icon {
  margin-right: 10px;
  color: #909399;
}

.history-item.current-input {
  color: #303133;
  font-weight: 500;
}

.history-item.current-input .el-icon {
  color: #409eff;
}

.history-text {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.delete-icon {
  opacity: 0;
  transition: opacity 0.2s;
  color: #909399;
  margin-left: auto;
}

.history-item:hover .delete-icon {
  opacity: 1;
}

.delete-icon:hover {
  color: #f56c6c;
}

.history-footer {
  padding: 8px 16px;
  border-top: 1px solid #ebeef5;
  text-align: center;
}

.stats-section {
  margin-bottom: 40px;
}

.stat-card {
  background: #fff;
  border-radius: 12px;
  padding: 30px;
  text-align: center;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.stat-number {
  font-size: 32px;
  font-weight: bold;
  color: #303133;
  margin: 16px 0 8px;
}

.stat-label {
  color: #909399;
  font-size: 14px;
}

.hot-jobs-section {
  background: #fff;
  border-radius: 12px;
  padding: 30px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.section-header h2 {
  margin: 0;
  font-size: 24px;
  color: #303133;
}

.job-card {
  margin-bottom: 20px;
  cursor: pointer;
  transition: transform 0.3s;
}

.job-card:hover {
  transform: translateY(-4px);
}

.job-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.job-header h3 {
  margin: 0;
  font-size: 16px;
  color: #303133;
}

.salary {
  color: #f56c6c;
  font-weight: bold;
}

.job-tags {
  margin-bottom: 16px;
}

.job-tags .el-tag {
  margin-right: 8px;
}

.job-company {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #606266;
  font-size: 14px;
}
</style>
