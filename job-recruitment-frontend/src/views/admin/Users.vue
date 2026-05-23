<template>
  <div class="admin-users">
    <el-card>
      <template #header>
        <div class="card-header">
          <h3>用户管理</h3>
          <el-button type="primary" @click="showCreateDialog">新增用户</el-button>
        </div>
        <!-- 搜索和筛选区域 -->
        <div class="search-bar">
          <el-row :gutter="20">
            <el-col :span="6">
              <el-input
                v-model="searchForm.keyword"
                placeholder="搜索用户名/真实姓名/邮箱"
                clearable
                @keyup.enter="handleSearch"
                @clear="handleSearch"
              />
            </el-col>
            <el-col :span="4">
              <el-select v-model="searchForm.role" placeholder="角色" clearable @change="handleSearch">
                <el-option label="管理员" :value="1" />
                <el-option label="运营" :value="2" />
                <el-option label="企业" :value="3" />
                <el-option label="求职者" :value="4" />
              </el-select>
            </el-col>
            <el-col :span="4">
              <el-select v-model="searchForm.status" placeholder="状态" clearable @change="handleSearch">
                <el-option label="启用" :value="1" />
                <el-option label="禁用" :value="0" />
              </el-select>
            </el-col>
            <el-col :span="4">
              <el-button type="primary" @click="handleSearch">搜索</el-button>
              <el-button @click="resetSearch">重置</el-button>
            </el-col>
          </el-row>
        </div>
      </template>
      
      <el-table :data="userList" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="realName" label="真实姓名" />
        <el-table-column prop="roleName" label="角色" width="100">
          <template #default="{ row }">
            <el-tag :type="getRoleType(row.role)">{{ row.roleName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="email" label="邮箱" />
        <el-table-column prop="phone" label="手机号" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button 
              :type="row.status === 1 ? 'danger' : 'success'" 
              size="small"
              @click="toggleStatus(row)"
            >
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button type="warning" size="small" @click="handleResetPassword(row)">重置密码</el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
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
    
    <!-- 新增用户对话框 -->
    <el-dialog v-model="createDialogVisible" title="新增用户" width="500px">
      <el-form :model="createForm" :rules="createRules" ref="createFormRef" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="createForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="createForm.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="createForm.confirmPassword" type="password" placeholder="请再次输入密码" show-password />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="createForm.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="createForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="createForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="createForm.role" placeholder="请选择角色" style="width: 100%">
            <el-option label="管理员" :value="1" />
            <el-option label="运营" :value="2" />
            <el-option label="企业" :value="3" />
            <el-option label="求职者" :value="4" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCreate" :loading="creating">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listUsers, updateUserStatus, deleteUser, createUser, resetPassword } from '../../api/user'

const loading = ref(false)
const userList = ref([])
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 搜索表单
const searchForm = reactive({
  keyword: '',
  role: null,
  status: null
})

// 创建用户表单
const createDialogVisible = ref(false)
const creating = ref(false)
const createFormRef = ref(null)
const createForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  realName: '',
  email: '',
  phone: '',
  role: null
})

// 表单验证规则
const createRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== createForm.password) {
          callback(new Error('两次密码输入不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

const getRoleType = (role) => {
  const types = { 1: 'danger', 2: 'warning', 3: 'success', 4: 'info' }
  return types[role] || 'info'
}

const fetchUsers = async () => {
  loading.value = true
  try {
    const res = await listUsers({
      current: pagination.current,
      size: pagination.size,
      keyword: searchForm.keyword || undefined,
      role: searchForm.role || undefined,
      status: searchForm.status !== null ? searchForm.status : undefined
    })
    userList.value = res.data.records
    pagination.total = res.data.total
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.current = 1
  fetchUsers()
}

// 重置搜索
const resetSearch = () => {
  searchForm.keyword = ''
  searchForm.role = null
  searchForm.status = null
  pagination.current = 1
  fetchUsers()
}

const toggleStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  try {
    await updateUserStatus(row.id, newStatus)
    ElMessage.success('操作成功')
    fetchUsers()
  } catch (error) {
    console.error(error)
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该用户吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteUser(row.id)
      ElMessage.success('删除成功')
      fetchUsers()
    } catch (error) {
      console.error(error)
    }
  })
}

const handlePageChange = (page) => {
  pagination.current = page
  fetchUsers()
}

// 显示创建用户对话框
const showCreateDialog = () => {
  createForm.username = ''
  createForm.password = ''
  createForm.confirmPassword = ''
  createForm.realName = ''
  createForm.email = ''
  createForm.phone = ''
  createForm.role = null
  createDialogVisible.value = true
}

// 创建用户
const handleCreate = async () => {
  if (!createFormRef.value) return
  
  await createFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    creating.value = true
    try {
      await createUser(createForm)
      ElMessage.success('创建成功')
      createDialogVisible.value = false
      fetchUsers()
    } catch (error) {
      ElMessage.error(error.response?.data?.message || '创建失败')
      console.error(error)
    } finally {
      creating.value = false
    }
  })
}

// 重置密码
const handleResetPassword = (row) => {
  const defaultPassword = row.username + '123456'
  ElMessageBox.confirm(
    `确定要重置用户 "${row.username}" 的密码吗？\n\n重置后的默认密码为：${defaultPassword}`,
    '重置密码确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
      dangerouslyUseHTMLString: false
    }
  ).then(async () => {
    try {
      await resetPassword(row.id)
      ElMessage.success('密码重置成功')
    } catch (error) {
      ElMessage.error(error.response?.data?.message || '密码重置失败')
      console.error(error)
    }
  })
}

onMounted(() => {
  fetchUsers()
})
</script>

<style scoped>
.admin-users {
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

.search-bar {
  padding: 15px 0;
  border-bottom: 1px solid #ebeef5;
  margin-bottom: 15px;
}

.search-bar :deep(.el-input__wrapper),
.search-bar :deep(.el-select .el-input__wrapper) {
  box-shadow: 0 0 0 1px #dcdfe6 inset;
}

.search-bar :deep(.el-input__wrapper:hover),
.search-bar :deep(.el-select .el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #409eff inset;
}

.pagination {
  margin-top: 20px;
  text-align: center;
}
</style>
