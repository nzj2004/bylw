import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import request from '../utils/request'

const ROLE_NAME_TO_CODE = {
  admin: 1,
  operator: 2,
  company: 3,
  user: 4,
  '管理员': 1,
  '运营': 2,
  '企业': 3,
  '求职者': 4,
  role_admin: 1,
  role_operator: 2,
  role_company: 3,
  role_user: 4
}

function parseRoleFromNumberOrString(value) {
  if (typeof value === 'number' && Number.isFinite(value)) {
    return value
  }

  if (typeof value === 'string' && value.trim()) {
    const trimmed = value.trim()
    if (/^\d+$/.test(trimmed)) {
      const parsed = Number(trimmed)
      return Number.isFinite(parsed) ? parsed : null
    }
  }

  return null
}

function parseRoleFromName(roleName) {
  const normalized = String(roleName || '').trim().toLowerCase()
  if (!normalized) {
    return null
  }
  return ROLE_NAME_TO_CODE[normalized] || null
}

function decodeJwtRole(token) {
  if (!token || typeof token !== 'string') {
    return null
  }
  try {
    const parts = token.split('.')
    if (parts.length < 2) {
      return null
    }

    const base64 = parts[1].replace(/-/g, '+').replace(/_/g, '/')
    const padded = base64.padEnd(base64.length + (4 - (base64.length % 4 || 4)) % 4, '=')
    const payload = JSON.parse(atob(padded))
    return parseRoleFromNumberOrString(payload?.role)
  } catch (error) {
    console.warn('解析 token role 失败', error)
    return null
  }
}

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(null)

  const isLoggedIn = computed(() => !!token.value)

  const normalizedRole = computed(() => {
    const direct = parseRoleFromNumberOrString(userInfo.value?.role)
    if (Number.isFinite(direct)) {
      return direct
    }

    const fromName = parseRoleFromName(userInfo.value?.roleName)
    if (Number.isFinite(fromName)) {
      return fromName
    }

    return decodeJwtRole(token.value)
  })

  const isAdmin = computed(() => normalizedRole.value === 1)
  const isOperator = computed(() => normalizedRole.value === 2)
  const isCompany = computed(() => normalizedRole.value === 3)
  const isUser = computed(() => normalizedRole.value === 4)

  const roleName = computed(() => {
    const roles = {
      1: '管理员',
      2: '运营',
      3: '企业',
      4: '求职者'
    }
    return roles[normalizedRole.value] || ''
  })

  const setToken = (newToken) => {
    token.value = newToken
    userInfo.value = null
    localStorage.setItem('token', newToken)
  }

  const setUserInfo = (info) => {
    userInfo.value = info
  }

  const getUserInfo = async () => {
    try {
      const res = await request.get('/user/info')
      userInfo.value = res.data
      return res.data
    } catch (error) {
      userInfo.value = null
      return null
    }
  }

  const logout = () => {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
  }

  return {
    token,
    userInfo,
    normalizedRole,
    isLoggedIn,
    isAdmin,
    isOperator,
    isCompany,
    isUser,
    roleName,
    setToken,
    setUserInfo,
    getUserInfo,
    logout
  }
})
