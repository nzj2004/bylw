import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../store/user'

const routes = [
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue'),
    meta: { public: true }
  },
  {
    path: '/',
    name: 'Layout',
    component: () => import('../views/Layout.vue'),
    redirect: '/home',
    children: [
      {
        path: 'home',
        name: 'Home',
        component: () => import('../views/user/Home.vue')
      },
      {
        path: 'jobs',
        name: 'JobList',
        component: () => import('../views/user/JobList.vue')
      },
      {
        path: 'job/:id',
        name: 'JobDetail',
        component: () => import('../views/user/JobDetail.vue')
      },
      {
        path: 'resume',
        name: 'Resume',
        component: () => import('../views/user/Resume.vue'),
        meta: { requireUser: true }
      },
      {
        path: 'applications',
        name: 'Applications',
        component: () => import('../views/user/Applications.vue'),
        meta: { requireUser: true }
      },
      {
        path: 'offers',
        name: 'Offers',
        component: () => import('../views/user/Offers.vue'),
        meta: { requireUser: true }
      },
      {
        path: 'favorites',
        name: 'Favorites',
        component: () => import('../views/user/Favorites.vue'),
        meta: { requireUser: true }
      },
      {
        path: 'notifications',
        name: 'Notifications',
        component: () => import('../views/Notifications.vue'),
        meta: { requireAuth: true }
      },
      {
        path: 'admin/dashboard',
        name: 'AdminDashboard',
        component: () => import('../views/admin/Dashboard.vue'),
        meta: { requireAdmin: true }
      },
      {
        path: 'admin/users',
        name: 'AdminUsers',
        component: () => import('../views/admin/Users.vue'),
        meta: { requireAdmin: true }
      },
      {
        path: 'admin/job-audit',
        name: 'AdminJobAudit',
        component: () => import('../views/operator/Jobs.vue'),
        meta: { requireAdmin: true }
      },
      {
        path: 'admin/jobs',
        name: 'AdminJobs',
        component: () => import('../views/admin/Jobs.vue'),
        meta: { requireAdmin: true }
      },
      {
        path: 'admin/batch-publish',
        name: 'AdminBatchPublish',
        component: () => import('../views/admin/BatchPublish.vue'),
        meta: { requireAdmin: true }
      },
      {
        path: 'admin/logs',
        name: 'AdminLogs',
        component: () => import('../views/admin/Logs.vue'),
        meta: { requireAdmin: true }
      },
      {
        path: 'operator/companies',
        name: 'OperatorCompanies',
        component: () => import('../views/operator/Companies.vue'),
        meta: { requireOperator: true }
      },
      {
        path: 'operator/jobs',
        name: 'OperatorJobs',
        component: () => import('../views/operator/Jobs.vue'),
        meta: { requireOperator: true }
      },
      {
        path: 'company/info',
        name: 'CompanyInfo',
        component: () => import('../views/company/Info.vue'),
        meta: { requireCompany: true }
      },
      {
        path: 'company/jobs',
        name: 'CompanyJobs',
        component: () => import('../views/company/Jobs.vue'),
        meta: { requireCompany: true }
      },
      {
        path: 'company/applications',
        name: 'CompanyApplications',
        component: () => import('../views/company/Applications.vue'),
        meta: { requireCompany: true }
      },
      {
        path: 'company/applications/:applicationId/process',
        name: 'CompanyApplicationProcess',
        component: () => import('../views/company/ApplicationProcess.vue'),
        meta: { requireCompany: true },
        props: true
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

const showLoginDialog = () => {
  window.dispatchEvent(new CustomEvent('show-login-dialog'))
}

router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore()

  if (to.meta.public) {
    next()
    return
  }

  const needsAuth =
    to.meta.requireAuth ||
    to.meta.requireAdmin ||
    to.meta.requireOperator ||
    to.meta.requireCompany ||
    to.meta.requireUser

  if (needsAuth && !userStore.isLoggedIn) {
    ElMessage.error('请先登录后访问该页面')
    showLoginDialog()
    next('/')
    return
  }

  if (needsAuth && (!userStore.userInfo || !Number.isFinite(userStore.normalizedRole))) {
    const info = await userStore.getUserInfo()
    if (!info) {
      userStore.logout()
      ElMessage.error('登录已失效，请重新登录')
      showLoginDialog()
      next('/')
      return
    }
  }

  if (to.meta.requireAdmin && !userStore.isAdmin) {
    next('/')
    return
  }

  if (to.meta.requireOperator && !userStore.isOperator && !userStore.isAdmin) {
    next('/')
    return
  }

  if (to.meta.requireCompany && !userStore.isCompany) {
    next('/')
    return
  }

  if (to.meta.requireUser && !userStore.isUser) {
    next('/')
    return
  }

  next()
})

export default router
