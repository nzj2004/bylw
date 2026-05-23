<template>
  <div class="notifications-page">
    <el-card>
      <template #header>
        <div class="page-header">
          <h3>站内通知</h3>
          <el-button type="primary" :disabled="unreadCount === 0" @click="handleMarkAllRead">全部已读</el-button>
        </div>
      </template>

      <el-table :data="notifications" stripe v-loading="loading">
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
        <el-table-column label="操作" width="110">
          <template #default="{ row }">
            <el-button size="small" :disabled="row.readStatus !== 0" @click="handleMarkRead(row)">标记已读</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="notifications.length === 0 && !loading" description="暂无通知" />
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getNotifications, markAllNotificationsRead, markNotificationRead } from '../api/user'

const notifications = ref([])
const loading = ref(false)

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

const handleMarkRead = async (row) => {
  if (!row?.id) {
    return
  }
  await markNotificationRead(row.id)
  ElMessage.success('已标记为已读')
  await fetchNotifications()
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
</style>
