<template>
  <div class="login-page">
    <header class="login-topbar">
      <button class="brand-button" type="button" @click="goHome">
        <el-icon><OfficeBuilding /></el-icon>
        <span>毕业生招聘系统</span>
      </button>
      <el-button text class="home-link" @click="goHome">返回首页</el-button>
    </header>

    <main class="login-shell">
      <section class="brand-panel" aria-label="平台信息">
        <div class="brand-kicker">Graduate Recruitment Platform</div>
        <h1>找到理想的工作，也找到合适的人</h1>
        <p class="brand-copy">企业招聘、求职投递、面试进度与Offer处理统一管理。</p>

        <div class="metrics">
          <div class="metric-item">
            <strong>580+</strong>
            <span>入驻企业</span>
          </div>
          <div class="metric-item">
            <strong>8600+</strong>
            <span>注册用户</span>
          </div>
          <div class="metric-item">
            <strong>4类</strong>
            <span>角色协作</span>
          </div>
        </div>
      </section>

      <section class="auth-panel" aria-label="登录">
        <div class="auth-heading">
          <span class="auth-icon">
            <el-icon><UserFilled /></el-icon>
          </span>
          <div>
            <h2>欢迎登录</h2>
            <p>使用账号进入招聘管理工作台</p>
          </div>
        </div>

        <el-form
          ref="formRef"
          class="login-form"
          :model="form"
          :rules="rules"
          label-position="top"
        >
          <el-form-item label="用户名" prop="username">
            <el-input
              v-model.trim="form.username"
              placeholder="请输入用户名"
              :prefix-icon="User"
              size="large"
              autocomplete="username"
            />
          </el-form-item>

          <el-form-item label="密码" prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="请输入密码"
              :prefix-icon="Lock"
              size="large"
              autocomplete="current-password"
              show-password
              @keyup.enter="handleLogin"
            />
          </el-form-item>

          <div class="form-extra">
            <el-link type="primary" :underline="false" @click="handleForgotPassword">忘记密码？</el-link>
          </div>

          <el-button
            class="login-submit"
            type="primary"
            size="large"
            :icon="Right"
            :loading="loading"
            @click="handleLogin"
          >
            登录
          </el-button>
        </el-form>

        <div class="demo-accounts">
          <span>常用账号</span>
          <button
            v-for="account in demoAccounts"
            :key="account.username"
            type="button"
            @click="fillDemoAccount(account)"
          >
            {{ account.label }}
          </button>
        </div>

        <div class="login-footer">
          <span>还没有账号？</span>
          <el-link type="primary" @click="goToRegister">立即注册</el-link>
        </div>
      </section>
    </main>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Lock,
  OfficeBuilding,
  Right,
  User,
  UserFilled
} from '@element-plus/icons-vue'
import { login } from '../api/auth'
import { useUserStore } from '../store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const demoAccounts = [
  { label: '求职者', username: 'zhouhao', password: 'admin123' },
  { label: '企业', username: 'alibaba', password: 'admin123' },
  { label: '运营', username: 'operator', password: 'admin123' },
  { label: '管理员', username: 'admin', password: 'admin123' }
]

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const redirectTarget = computed(() => {
  const redirect = route.query.redirect
  if (typeof redirect === 'string' && redirect && redirect !== '/login') {
    return redirect
  }
  return '/'
})

const handleLogin = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      const res = await login(form)
      userStore.setToken(res.data.token)
      await userStore.getUserInfo()
      ElMessage.success('登录成功')
      router.replace(redirectTarget.value)
    } catch (error) {
      console.error(error)
    } finally {
      loading.value = false
    }
  })
}

const fillDemoAccount = (account) => {
  form.username = account.username
  form.password = account.password
}

const handleForgotPassword = () => {
  ElMessage.warning('忘记密码请联系管理员')
}

const goHome = () => {
  router.push('/')
}

const goToRegister = () => {
  router.push('/register')
}

onMounted(() => {
  if (userStore.isLoggedIn) {
    router.replace(redirectTarget.value)
  }
})
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  color: #172033;
  background:
    linear-gradient(90deg, rgba(15, 118, 110, 0.08) 1px, transparent 1px),
    linear-gradient(rgba(20, 33, 61, 0.06) 1px, transparent 1px),
    #f4f7fb;
  background-size: 34px 34px;
}

.login-topbar {
  height: 68px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 48px;
}

.brand-button {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  border: 0;
  padding: 0;
  color: #14213d;
  background: transparent;
  font-size: 18px;
  font-weight: 700;
  cursor: pointer;
}

.brand-button .el-icon {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  color: #fff;
  background: #0f766e;
}

.home-link {
  color: #475569;
}

.login-shell {
  width: min(1120px, calc(100vw - 48px));
  min-height: calc(100vh - 108px);
  margin: 0 auto;
  display: grid;
  grid-template-columns: minmax(0, 1.05fr) 420px;
  align-items: center;
  gap: 56px;
  padding: 20px 0 56px;
}

.brand-panel {
  padding: 52px 0;
}

.brand-kicker {
  display: inline-flex;
  align-items: center;
  height: 30px;
  padding: 0 12px;
  border: 1px solid rgba(15, 118, 110, 0.22);
  border-radius: 8px;
  color: #0f766e;
  background: rgba(255, 255, 255, 0.76);
  font-size: 13px;
  font-weight: 700;
}

.brand-panel h1 {
  max-width: 680px;
  margin: 24px 0 18px;
  color: #111827;
  font-size: 48px;
  line-height: 1.15;
  font-weight: 800;
}

.brand-copy {
  max-width: 560px;
  margin: 0;
  color: #526071;
  font-size: 17px;
  line-height: 1.8;
}

.metrics {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
  max-width: 620px;
  margin-top: 44px;
}

.metric-item {
  min-height: 86px;
  padding: 18px;
  border: 1px solid rgba(148, 163, 184, 0.3);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.76);
  box-shadow: 0 16px 36px rgba(15, 23, 42, 0.06);
}

.metric-item strong {
  display: block;
  color: #0f766e;
  font-size: 27px;
  line-height: 1;
}

.metric-item span {
  display: block;
  margin-top: 10px;
  color: #64748b;
  font-size: 13px;
}

.auth-panel {
  padding: 34px;
  border: 1px solid rgba(148, 163, 184, 0.28);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.94);
  box-shadow: 0 22px 58px rgba(15, 23, 42, 0.14);
}

.auth-heading {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 28px;
}

.auth-icon {
  width: 48px;
  height: 48px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  color: #0f766e;
  background: #e8f6f3;
  font-size: 22px;
}

.auth-heading h2 {
  margin: 0 0 6px;
  color: #111827;
  font-size: 25px;
}

.auth-heading p {
  margin: 0;
  color: #64748b;
  font-size: 14px;
}

.login-form :deep(.el-form-item__label) {
  color: #334155;
  font-weight: 700;
}

.login-form :deep(.el-input__wrapper) {
  border-radius: 8px;
  box-shadow: 0 0 0 1px #d9e2ec inset;
}

.login-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #0f766e inset;
}

.login-submit {
  width: 100%;
  height: 46px;
  margin-top: 8px;
  border: 0;
  border-radius: 8px;
  background: #0f766e;
  font-weight: 700;
}

.form-extra {
  display: flex;
  justify-content: flex-end;
  margin: -6px 0 6px;
  font-size: 14px;
}

.login-submit:hover,
.login-submit:focus {
  background: #115e59;
}

.demo-accounts {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  margin-top: 18px;
}

.demo-accounts span {
  width: 100%;
  color: #64748b;
  font-size: 13px;
}

.demo-accounts button {
  height: 30px;
  border: 1px solid #cbd5e1;
  border-radius: 8px;
  padding: 0 12px;
  color: #334155;
  background: #fff;
  cursor: pointer;
}

.demo-accounts button:hover {
  border-color: #0f766e;
  color: #0f766e;
  background: #f0fdfa;
}

.login-footer {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  margin-top: 24px;
  color: #64748b;
}

@media (max-width: 900px) {
  .login-topbar {
    padding: 0 24px;
  }

  .login-shell {
    grid-template-columns: 1fr;
    gap: 20px;
    width: min(520px, calc(100vw - 32px));
    padding-top: 8px;
  }

  .brand-panel {
    padding: 16px 0 0;
  }

  .brand-panel h1 {
    margin-top: 18px;
    font-size: 34px;
  }

  .brand-copy {
    font-size: 15px;
  }

  .metrics {
    grid-template-columns: repeat(3, 1fr);
    gap: 8px;
    margin-top: 22px;
  }

  .metric-item {
    min-height: 72px;
    padding: 12px;
  }

  .metric-item strong {
    font-size: 22px;
  }

  .auth-panel {
    padding: 24px;
  }
}

@media (max-width: 560px) {
  .login-topbar {
    height: 60px;
  }

  .brand-button span {
    font-size: 16px;
  }

  .home-link {
    display: none;
  }

  .brand-panel h1 {
    font-size: 29px;
  }

  .metrics {
    grid-template-columns: 1fr;
  }

  .auth-heading {
    align-items: flex-start;
  }
}
</style>
