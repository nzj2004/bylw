<template>
  <el-dialog
    v-model="visible"
    title="登录"
    width="420px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-form :model="form" :rules="rules" ref="formRef" label-position="top">
      <el-form-item label="用户名" prop="username">
        <el-input 
          v-model="form.username" 
          placeholder="请输入用户名"
          :prefix-icon="User"
          size="large"
        />
      </el-form-item>
      
      <el-form-item label="密码" prop="password">
        <el-input 
          v-model="form.password" 
          type="password" 
          placeholder="请输入密码"
          :prefix-icon="Lock"
          size="large"
          show-password
          @keyup.enter="handleLogin"
        />
      </el-form-item>
      
      <el-form-item>
        <el-button 
          type="primary" 
          size="large" 
          :loading="loading"
          style="width: 100%"
          @click="handleLogin"
        >
          登录
        </el-button>
      </el-form-item>
      
      <div class="login-footer">
        <span>还没有账号？</span>
        <el-link type="primary" @click="goToRegister">立即注册</el-link>
      </div>
    </el-form>
  </el-dialog>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { login } from '../api/auth'
import { useUserStore } from '../store/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)
const visible = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

// 显示弹窗
const show = () => {
  visible.value = true
  // 重置表单
  form.username = ''
  form.password = ''
  if (formRef.value) {
    formRef.value.clearValidate()
  }
}

// 关闭弹窗
const handleClose = () => {
  visible.value = false
  if (formRef.value) {
    formRef.value.resetFields()
  }
}

const handleLogin = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const res = await login(form)
        userStore.setToken(res.data.token)
        await userStore.getUserInfo()
        ElMessage.success('登录成功')
        visible.value = false
        // 登录成功后刷新当前页面或跳转到首页
        router.push('/')
      } catch (error) {
        console.error(error)
      } finally {
        loading.value = false
      }
    }
  })
}

const goToRegister = () => {
  visible.value = false
  router.push('/register')
}

// 暴露方法给父组件
defineExpose({
  show
})
</script>

<style scoped>
.login-footer {
  text-align: center;
  margin-top: 16px;
  color: #606266;
}
</style>
