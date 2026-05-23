<template>
  <div class="job-detail-container">
    <el-card v-if="job" class="job-detail">
      <div class="job-header">
        <div class="job-title-section">
          <h1>{{ job.title }}</h1>
          <div class="job-salary">{{ job.salaryRange }}</div>
        </div>
        <div class="job-actions">
          <el-button 
            type="primary" 
            size="large" 
            @click="handleApply" 
            :loading="applying"
            :disabled="hasApplied"
          >
            {{ hasApplied ? '已投递' : (applying ? '投递中...' : '立即投递') }}
          </el-button>
          <el-button 
            :type="isFavorited ? 'warning' : 'default'" 
            size="large" 
            @click="handleFavorite"
            :loading="favoriting"
          >
            <el-icon><Star /></el-icon>
            {{ isFavorited ? '已收藏' : '收藏' }}
          </el-button>
        </div>
      </div>
      
      <div class="job-tags">
        <el-tag size="large">{{ job.workCity }}</el-tag>
        <el-tag size="large" type="info">{{ job.experience }}</el-tag>
        <el-tag size="large" type="success">{{ job.education }}</el-tag>
        <el-tag size="large" type="warning">{{ job.jobTypeName }}</el-tag>
      </div>
      
      <el-divider />
      
      <div class="company-section">
        <div class="company-info">
          <el-avatar :size="64" :src="job.companyLogo" />
          <div class="company-detail">
            <h3>{{ job.companyName }}</h3>
            <p>浏览 {{ job.viewCount }} · 投递 {{ job.applyCount }}</p>
          </div>
        </div>
      </div>
      
      <el-divider />
      
      <div class="job-content">
        <h3>职位描述</h3>
        <div class="content-text">{{ job.jobDesc }}</div>
        
        <h3>岗位要求</h3>
        <div class="content-text">{{ job.requirements }}</div>
        
        <h3>福利待遇</h3>
        <div class="content-text">{{ job.welfare || '暂无' }}</div>
      </div>
    </el-card>
    
    <el-skeleton v-else :rows="10" animated />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Star } from '@element-plus/icons-vue'
import { getJobById } from '../../api/job'
import { applyJob, checkApplyStatus } from '../../api/user'
import { addFavorite, removeFavorite, isFavorite as checkFavorite } from '../../api/favorite'
import { useUserStore } from '../../store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const job = ref(null)
const applying = ref(false)
const isFavorited = ref(false)
const favoriting = ref(false)
const hasApplied = ref(false) // 是否已投递

const fetchJobDetail = async () => {
  try {
    const res = await getJobById(route.params.id)
    job.value = res.data
    // 检查是否已收藏和已投递
    if (userStore.isLoggedIn && userStore.isUser) {
      checkFavoriteStatus()
      checkApplyStatusForJob()
    }
  } catch (error) {
    console.error(error)
  }
}

const checkFavoriteStatus = async () => {
  try {
    const res = await checkFavorite(route.params.id)
    isFavorited.value = res.data
  } catch (error) {
    console.error(error)
  }
}

const checkApplyStatusForJob = async () => {
  try {
    const res = await checkApplyStatus(route.params.id)
    hasApplied.value = res.data
  } catch (error) {
    console.error(error)
  }
}

const handleFavorite = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    // 触发登录弹窗事件
    window.dispatchEvent(new CustomEvent('show-login-dialog'))
    return
  }
  if (!userStore.isUser) {
    ElMessage.warning('只有求职者可以收藏职位')
    return
  }
  
  try {
    favoriting.value = true
    if (isFavorited.value) {
      await removeFavorite(route.params.id)
      isFavorited.value = false
      ElMessage.success('已取消收藏')
    } else {
      await addFavorite(route.params.id)
      isFavorited.value = true
      ElMessage.success('收藏成功')
    }
  } catch (error) {
    ElMessage.error('操作失败')
  } finally {
    favoriting.value = false
  }
}

const handleApply = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    // 触发登录弹窗事件
    window.dispatchEvent(new CustomEvent('show-login-dialog'))
    return
  }
  if (!userStore.isUser) {
    ElMessage.warning('只有求职者可以投递简历')
    return
  }
  
  // 如果已投递，提示用户
  if (hasApplied.value) {
    ElMessage.info('您已投递过该职位')
    return
  }
  
  try {
    await ElMessageBox.confirm('确定要投递该职位吗？', '投递确认', {
      type: 'info',
      confirmButtonText: '确定投递',
      cancelButtonText: '取消'
    })
    
    applying.value = true
    await applyJob(route.params.id)
    ElMessage.success('投递成功！')
    hasApplied.value = true // 更新投递状态
  } catch (error) {
    if (error !== 'cancel') {
      const msg = error.response?.data?.message || '投递失败，请稍后重试'
      ElMessage.error(msg)
    }
  } finally {
    applying.value = false
  }
}

onMounted(() => {
  fetchJobDetail()
})
</script>

<style scoped>
.job-detail-container {
  max-width: 900px;
  margin: 0 auto;
}

.job-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
}

.job-title-section h1 {
  margin: 0 0 12px 0;
  font-size: 28px;
  color: #303133;
}

.job-salary {
  color: #f56c6c;
  font-size: 24px;
  font-weight: bold;
}

.job-tags {
  margin-bottom: 20px;
}

.job-tags .el-tag {
  margin-right: 12px;
}

.company-section {
  padding: 20px 0;
}

.company-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.company-detail h3 {
  margin: 0 0 8px 0;
  font-size: 18px;
}

.company-detail p {
  margin: 0;
  color: #909399;
}

.job-content h3 {
  margin: 24px 0 16px 0;
  font-size: 18px;
  color: #303133;
}

.content-text {
  color: #606266;
  line-height: 1.8;
  white-space: pre-line;
}

/* 已投递按钮样式 */
.job-actions .el-button.is-disabled {
  background-color: #f5f7fa !important;
  border-color: #dcdfe6 !important;
  color: #a8abb2 !important;
  cursor: not-allowed;
}
</style>
