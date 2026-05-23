import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../store/user'

const permissionPatterns = [
  '权限',
  '无权',
  '没有权限',
  'forbidden',
  'permission',
  '403',
  '鏃犳潈',
  '娌℃湁鏉冮檺'
]

const isPermissionMessage = (message) => {
  const text = String(message || '').toLowerCase()
  return permissionPatterns.some((item) => text.includes(item.toLowerCase()))
}

const goToLoginPage = () => {
  const current = `${window.location.pathname}${window.location.search}`
  const query = window.location.pathname !== '/login'
    ? `?redirect=${encodeURIComponent(current)}`
    : ''
  window.location.assign(`/login${query}`)
}

const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

request.interceptors.request.use(
  (config) => {
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers.Authorization = `Bearer ${userStore.token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

request.interceptors.response.use(
  (response) => {
    const res = response.data

    if (!res || typeof res !== 'object' || !Object.prototype.hasOwnProperty.call(res, 'code')) {
      ElMessage.error('后端返回了非标准响应格式')
      return Promise.reject(new Error('后端返回了非标准响应格式'))
    }

    if (res.code !== 200) {
      if (res.code === 401) {
        const userStore = useUserStore()
        userStore.logout()
        ElMessage.error('请先登录后再操作')
        setTimeout(() => {
          goToLoginPage()
        }, 500)
      } else if (res.code !== 403 && !isPermissionMessage(res.message)) {
        ElMessage.error(res.message || '请求失败')
      }
      return Promise.reject(new Error(res.message || '请求失败'))
    }

    return res
  },
  (error) => {
    if (error.response) {
      const status = error.response.status
      if (status === 401) {
        const userStore = useUserStore()
        userStore.logout()
        ElMessage.error('请先登录后再操作')
        setTimeout(() => {
          goToLoginPage()
        }, 500)
      } else if (status !== 403) {
        ElMessage.error(error.message || '网络错误')
      }
    } else {
      ElMessage.error(error.message || '网络错误')
    }
    return Promise.reject(error)
  }
)

export default request
