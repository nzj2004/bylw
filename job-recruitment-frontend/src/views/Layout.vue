<template>
  <el-container class="layout-container">
    <el-header class="header">
      <div class="logo">
        <el-icon size="28" color="#409EFF"><OfficeBuilding /></el-icon>
        <span class="title">毕业生招聘系统</span>
      </div>
      <div class="nav-menu">
        <el-menu
          mode="horizontal"
          :default-active="activeMenu"
          router
          background-color="transparent"
          text-color="#fff"
          active-text-color="#ffd04b"
        >
          <el-menu-item index="/home">首页</el-menu-item>
          <el-menu-item index="/jobs">职位列表</el-menu-item>
          <el-menu-item v-if="userStore.isLoggedIn" index="/notifications">
            <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="nav-badge">通知</el-badge>
          </el-menu-item>
          
        <template v-if="userStore.isUser">
            <el-menu-item index="/resume">我的简历</el-menu-item>
            <el-menu-item index="/applications">投递记录</el-menu-item>
            <el-menu-item index="/offers">Offer</el-menu-item>
            <el-menu-item index="/favorites">我的收藏</el-menu-item>
          </template>
          
          <template v-if="userStore.isCompany">
            <el-menu-item index="/company/info">企业信息</el-menu-item>
            <el-menu-item index="/company/jobs">职位管理</el-menu-item>
            <el-menu-item index="/company/applications">简历管理</el-menu-item>
          </template>
          
          <template v-if="userStore.isOperator">
            <el-menu-item index="/operator/companies">企业审核</el-menu-item>
            <el-menu-item index="/operator/jobs">职位审核</el-menu-item>
          </template>
          
          <template v-if="userStore.isAdmin">
            <el-menu-item index="/admin/dashboard">数据概览</el-menu-item>
            <el-menu-item index="/admin/users">用户管理</el-menu-item>
            <el-menu-item index="/admin/job-audit">职位审核</el-menu-item>
            <el-menu-item index="/admin/jobs">职位管理</el-menu-item>
            <el-menu-item index="/admin/batch-publish">批量发布</el-menu-item>
            <el-menu-item index="/admin/logs">日志管理</el-menu-item>
          </template>
        </el-menu>
      </div>
      <div class="user-info">
        <template v-if="userStore.isLoggedIn">
          <el-dropdown @command="handleCommand">
            <span class="user-name">
              {{ userStore.userInfo?.realName || userStore.userInfo?.username }}
              <el-tag size="small" type="info" style="margin-left: 8px;">
                {{ userStore.roleName }}
              </el-tag>
              <el-icon class="el-icon--right"><arrow-down /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <template v-else>
          <el-button type="primary" size="small" @click="showLoginDialog">登录</el-button>
        </template>
      </div>
    </el-header>
    
    <el-main class="main-content">
      <router-view v-slot="{ Component }">
        <keep-alive :include="cachedViews">
          <component :is="Component" />
        </keep-alive>
      </router-view>
    </el-main>
    
    <el-footer class="footer">
      <p>毕业生招聘信息发布与管理系统 © 2026</p>
    </el-footer>
    
    <!-- 登录弹窗 -->
    <LoginDialog ref="loginDialogRef" />
  </el-container>
</template>

<script setup>
import { computed, ref, onMounted, onBeforeUnmount, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { OfficeBuilding, ArrowDown } from '@element-plus/icons-vue'
import { useUserStore } from '../store/user'
import { getUnreadNotificationCount } from '../api/user'
import LoginDialog from '../components/LoginDialog.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const loginDialogRef = ref(null)
const unreadCount = ref(0)

const activeMenu = computed(() => route.path)

// 需要缓存的页面
const cachedViews = ref([
  'AdminDashboard',
  'AdminUsers',
  'AdminJobs',
  'AdminBatchPublish',
  'AdminLogs',
  'JobList',
  'Home',
  'Offers',
  'Notifications',
  'CompanyApplications',
  'CompanyApplicationProcess'
])

// 显示登录弹窗
const showLoginDialog = () => {
  if (loginDialogRef.value) {
    loginDialogRef.value.show()
  }
}

const fetchUnreadCount = async () => {
  if (!userStore.isLoggedIn) {
    unreadCount.value = 0
    return
  }
  try {
    const res = await getUnreadNotificationCount()
    unreadCount.value = res.data || 0
  } catch (error) {
    unreadCount.value = 0
  }
}

// 检查是否需要登录
const checkAuth = () => {
  // 如果未登录且访问了需要登录的页面
  if (!userStore.isLoggedIn) {
    const requiresAuth = route.meta.requireUser || 
                        route.meta.requireAdmin || 
                        route.meta.requireOperator || 
                        route.meta.requireCompany
    
    if (requiresAuth) {
      ElMessage.warning('请先登录后再访问')
      setTimeout(() => {
        showLoginDialog()
      }, 500)
    }
  }
}

// 组件挂载时检查权限
onMounted(() => {
  checkAuth()
  fetchUnreadCount()
  
  // 监听全局登录弹窗事件
  window.addEventListener('show-login-dialog', showLoginDialog)
})

// 监听路由变化
watch(() => route.path, () => {
  checkAuth()
  fetchUnreadCount()
})

watch(() => userStore.isLoggedIn, () => {
  fetchUnreadCount()
})

// 组件卸载时移除事件监听
onBeforeUnmount(() => {
  window.removeEventListener('show-login-dialog', showLoginDialog)
})

const handleCommand = (command) => {
  if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      userStore.logout()
      ElMessage.success('已退出登录')
      router.push('/')
    })
  }
}
</script>

<style scoped>
.layout-container {
  min-height: 100vh;
}

.header {
  background: linear-gradient(90deg, #1a237e 0%, #3949ab 100%);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 40px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo .title {
  font-size: 20px;
  font-weight: bold;
  color: #fff;
}

.nav-menu {
  flex: 1;
  margin-left: 40px;
}

.nav-menu :deep(.el-menu) {
  border-bottom: none;
}

.nav-menu :deep(.el-menu-item),
.nav-menu :deep(.el-sub-menu__title) {
  font-size: 15px;
}

.nav-badge :deep(.el-badge__content) {
  top: 12px;
}

/* 修复子菜单下拉背景色 - 使用更深层的选择器 */
.nav-menu :deep(.el-sub-menu__title) {
  background-color: transparent !important;
}

/* 子菜单下拉面板 - 多种选择器确保覆盖 */
.nav-menu :deep(.el-menu--popup),
.nav-menu :deep(.el-menu--horizontal .el-menu--popup),
:deep(.el-menu--popup) {
  background-color: #1a237e !important;
  border: none !important;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.3) !important;
}

/* 子菜单项 */
.nav-menu :deep(.el-menu--popup .el-menu-item),
:deep(.el-menu--popup .el-menu-item) {
  background-color: #1a237e !important;
  color: #fff !important;
  height: 40px !important;
  line-height: 40px !important;
}

/* 子菜单项hover效果 */
.nav-menu :deep(.el-menu--popup .el-menu-item:hover),
:deep(.el-menu--popup .el-menu-item:hover) {
  background-color: #3949ab !important;
  color: #ffd04b !important;
}

/* 激活状态的子菜单项 */
.nav-menu :deep(.el-menu--popup .el-menu-item.is-active),
:deep(.el-menu--popup .el-menu-item.is-active) {
  background-color: #3949ab !important;
  color: #ffd04b !important;
}

.user-info {
  color: #fff;
}

.user-name {
  cursor: pointer;
  display: flex;
  align-items: center;
  color: #fff;
}

.main-content {
  padding: 20px 40px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 120px);
}

.footer {
  background-color: #fff;
  text-align: center;
  color: #909399;
  padding: 20px;
  border-top: 1px solid #e4e7ed;
}
</style>
