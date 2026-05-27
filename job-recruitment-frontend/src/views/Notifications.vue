<template>
  <div class="notifications-page">
    <el-card>
      <template #header>
        <div class="page-header">
          <h3>站内通知</h3>
          <el-button type="primary" :disabled="unreadCount === 0" @click="handleMarkAllRead">全部已读</el-button>
        </div>
      </template>

      <el-table
        class="notification-table"
        :data="notifications"
        stripe
        v-loading="loading"
        @row-click="handleView"
      >
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.readStatus === 0 ? 'danger' : 'info'">
              {{ row.readStatus === 0 ? '未读' : '已读' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" width="180" />
        <el-table-column prop="content" label="内容" min-width="360" show-overflow-tooltip />
        <el-table-column prop="notificationType" label="类型" width="110" />
        <el-table-column prop="createTime" label="时间" width="170" />
        <el-table-column label="操作" width="170">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click.stop="handleView(row)">查看</el-button>
            <el-button size="small" :disabled="row.readStatus !== 0" @click.stop="handleMarkRead(row)">已读</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="notifications.length === 0 && !loading" description="暂无通知" />
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../store/user'
import { getNotifications, markAllNotificationsRead, markNotificationRead } from '../api/user'

const notifications = ref([])
const loading = ref(false)
const router = useRouter()
const userStore = useUserStore()

const unreadCount = computed(() => notifications.value.filter(item => item.readStatus === 0).length)

const isPermissionLikeError = (error) => {
  const text = [error?.response?.status, error?.response?.data?.message, error?.message].filter(Boolean).join(' ')
  return /(401|403|权限|无权|没有权限|forbidden|permission|鏃犳潈|娌℃湁鏉冮檺)/i.test(text)
}

const fetchNotifications = async () => {
  loading.value = true
  try {
    const res = await getNotifications()
    notifications.value = res.data || []
  } catch (error) {
    if (!isPermissionLikeError(error)) {
      ElMessage.error('获取通知失败')
    }
  } finally {
    loading.value = false
  }
}

const getNotificationTarget = (row) => {
  const businessType = String(row?.businessType || row?.notificationType || '').toUpperCase()
  const businessId = row?.businessId

  if (businessType === 'JOB' && businessId) {
    if (userStore.isCompany) {
      return '/company/jobs'
    }
    if (userStore.isAdmin) {
      return '/admin/jobs'
    }
    if (userStore.isOperator) {
      return '/operator/jobs'
    }
    return `/job/${businessId}`
  }

  if (businessType === 'APPLICATION' && businessId) {
    if (userStore.isCompany) {
      return `/company/applications/${businessId}/process`
    }
    if (userStore.isUser) {
      return {
        path: '/applications',
        query: { applicationId: businessId }
      }
    }
    return '/admin/jobs'
  }

  if (businessType === 'OFFER') {
    return userStore.isCompany ? '/company/applications' : '/offers'
  }

  if (businessType === 'INTERVIEW') {
    return userStore.isCompany ? '/company/applications' : '/applications'
  }

  return null
}

const handleMarkRead = async (row) => {
  if (!row?.id) {
    return
  }
  await markNotificationRead(row.id)
  ElMessage.success('已标记为已读')
  await fetchNotifications()
}

const markReadSilently = async (row) => {
  if (!row?.id || row.readStatus !== 0) {
    return
  }
  await markNotificationRead(row.id)
  row.readStatus = 1
}

const handleView = async (row) => {
  const target = getNotificationTarget(row)
  if (!target) {
    ElMessage.info('该通知暂无可跳转的详情页')
    return
  }

  try {
    await markReadSilently(row)
    await router.push(target)
  } catch (error) {
    console.error(error)
    ElMessage.error('打开通知详情失败')
  }
}

const handleMarkAllRead = async () => {
  await markAllNotificationsRead()
  ElMessage.success('已全部标记为已读')
  await fetchNotifications()
}

onMounted(() => {
  fetchNotifications()
})
</script>

<style scoped>
.notifications-page {
  max-width: 1100px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.page-header h3 {
  margin: 0;
}

.notification-table :deep(.el-table__row) {
  cursor: pointer;
}
</style>
