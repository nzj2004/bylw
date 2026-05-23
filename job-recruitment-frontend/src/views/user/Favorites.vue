<template>
  <div class="user-favorites">
    <el-card>
      <template #header>
        <div class="card-header">
          <h3>我的收藏</h3>
        </div>
      </template>

      <el-empty v-if="favoriteList.length === 0" description="暂无收藏职位" />

      <div v-else class="job-list">
        <el-card v-for="job in favoriteList" :key="job.id" class="job-item" shadow="hover">
          <div class="job-main">
            <div class="job-info" @click="goToDetail(job.id)">
              <h3 class="job-title">{{ job.title }}</h3>
              <div class="job-tags">
                <el-tag size="small">{{ job.workCity }}</el-tag>
                <el-tag size="small" type="info">{{ job.experience }}</el-tag>
                <el-tag size="small" type="success">{{ job.education }}</el-tag>
                <el-tag size="small" type="warning">{{ job.jobTypeName }}</el-tag>
              </div>
              <div class="job-salary">{{ job.salaryRange }}</div>
            </div>
            <div class="job-actions">
              <el-button type="primary" @click="goToDetail(job.id)">查看详情</el-button>
              <el-button type="danger" @click="handleCancelFavorite(job.id)">取消收藏</el-button>
            </div>
          </div>
        </el-card>
      </div>

      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          layout="total, prev, pager, next"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listFavorites, removeFavorite } from '../../api/favorite'
import { useUserStore } from '../../store/user'

const router = useRouter()
const userStore = useUserStore()
const favoriteList = ref([])
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const fetchFavorites = async () => {
  // 检查是否登录
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    window.dispatchEvent(new CustomEvent('show-login-dialog'))
    return
  }
  
  try {
    const res = await listFavorites({
      current: pagination.current,
      size: pagination.size
    })
    favoriteList.value = res.data.records || []
    pagination.total = res.data.total || 0
  } catch (error) {
    console.error(error)
  }
}

const handleCancelFavorite = (jobId) => {
  ElMessageBox.confirm('确定要取消收藏该职位吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await removeFavorite(jobId)
      ElMessage.success('已取消收藏')
      fetchFavorites()
    } catch (error) {
      console.error(error)
    }
  })
}

const goToDetail = (id) => {
  router.push(`/job/${id}`)
}

const handlePageChange = (page) => {
  pagination.current = page
  fetchFavorites()
}

onMounted(() => {
  fetchFavorites()
})
</script>

<style scoped>
.user-favorites {
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

.job-actions {
  display: flex;
  gap: 10px;
}

.pagination {
  margin-top: 24px;
  text-align: center;
}
</style>
