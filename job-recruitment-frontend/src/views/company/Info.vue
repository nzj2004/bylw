<template>
  <div class="company-info-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <h3>企业信息</h3>
          <div>
            <el-tag v-if="form.status === 0" type="warning" size="large">待审核</el-tag>
            <el-tag v-else-if="form.status === 1" type="success" size="large">已通过</el-tag>
            <el-tag v-else-if="form.status === 2" type="danger" size="large">已拒绝</el-tag>
            <el-button v-if="isEditing" type="primary" @click="handleSave" :loading="loading">保存</el-button>
            <el-button v-else-if="form.status !== 0" type="primary" @click="startEdit">修改</el-button>
            <el-button v-if="canSubmitAudit" type="success" @click="handleSubmitAudit" :loading="loading">提交审核</el-button>
          </div>
        </div>
      </template>
      
      <el-form :model="form" label-position="top">
        <!-- Logo上传 -->
        <el-form-item label="企业Logo">
          <el-upload
            class="logo-uploader"
            action=""
            :auto-upload="false"
            :show-file-list="false"
            :on-change="handleLogoChange"
            accept="image/jpeg,image/png,image/gif,image/webp"
            :disabled="!isEditing"
          >
            <img v-if="form.logoUrl" :src="getFullImageUrl(form.logoUrl)" class="logo" />
            <el-icon v-else class="logo-uploader-icon"><Plus /></el-icon>
          </el-upload>
          <div class="upload-tip" v-if="isEditing">支持 JPG、PNG、GIF、WEBP 格式，大小不超过5MB</div>
        </el-form-item>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="企业名称">
              <el-input v-model="form.companyName" placeholder="请输入企业名称" :disabled="!isEditing" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属行业">
              <el-select
                v-model="form.industry"
                placeholder="请选择行业"
                clearable
                filterable
                :disabled="!isEditing"
                style="width: 100%"
              >
                <el-option
                  v-for="item in industryOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="企业规模">
              <el-select v-model="form.scale" placeholder="请选择规模" :disabled="!isEditing" style="width: 100%">
                <el-option label="少于15人" value="少于15人" />
                <el-option label="15-50人" value="15-50人" />
                <el-option label="50-150人" value="50-150人" />
                <el-option label="150-500人" value="150-500人" />
                <el-option label="500-1000人" value="500-1000人" />
                <el-option label="1000人以上" value="1000人以上" />
                <el-option label="10000人以上" value="10000人以上" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="企业官网">
              <el-input v-model="form.website" placeholder="请输入官网地址" :disabled="!isEditing" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="企业地址">
          <el-input v-model="form.address" placeholder="请输入企业地址" :disabled="!isEditing" />
        </el-form-item>
        
        <el-form-item label="企业简介">
          <el-input v-model="form.description" type="textarea" :rows="5" placeholder="请描述企业简介" :disabled="!isEditing" />
        </el-form-item>
        
        <el-divider>联系人信息</el-divider>
        
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="联系人">
              <el-input v-model="form.contactName" placeholder="请输入联系人姓名" :disabled="!isEditing" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="联系电话">
              <el-input v-model="form.contactPhone" placeholder="请输入联系电话" :disabled="!isEditing" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="联系邮箱">
              <el-input v-model="form.contactEmail" placeholder="请输入联系邮箱" :disabled="!isEditing" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-alert 
          v-if="form.status === 2 && form.rejectReason" 
          title="拒绝原因" 
          type="error" 
          :closable="false"
          show-icon
        >
          {{ form.rejectReason }}
        </el-alert>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getCompanyInfo, saveCompanyInfo, submitCompanyAudit } from '../../api/user'
import { uploadImage } from '../../api/upload'

const loading = ref(false)
const isEditing = ref(false)
const uploading = ref(false)

// 行业分类选项 - 支持选择大类或子类
const industryOptions = [
  { value: '互联网', label: '互联网' },
  { value: '电子商务', label: '电子商务' },
  { value: '计算机软件', label: '计算机软件' },
  { value: '生活服务(O2O)', label: '生活服务(O2O)' },
  { value: '企业服务', label: '企业服务' },
  { value: '医疗健康', label: '医疗健康' },
  { value: '游戏', label: '游戏' },
  { value: '人工智能', label: '人工智能' },
  { value: '云计算', label: '云计算' },
  { value: '大数据', label: '大数据' },
  { value: '半导体/芯片', label: '半导体/芯片' },
  { value: '电子/硬件开发', label: '电子/硬件开发' },
  { value: '通信/网络设备', label: '通信/网络设备' },
  { value: '智能硬件/消费电子', label: '智能硬件/消费电子' },
  { value: '银行', label: '银行' },
  { value: '证券', label: '证券' },
  { value: '保险', label: '保险' },
  { value: '基金', label: '基金' },
  { value: '互联网金融', label: '互联网金融' },
  { value: '房地产开发', label: '房地产开发' },
  { value: '物业管理', label: '物业管理' },
  { value: '建筑设计', label: '建筑设计' },
  { value: '工程施工', label: '工程施工' },
  { value: '汽车研发', label: '汽车研发' },
  { value: '汽车制造', label: '汽车制造' },
  { value: '机械设计', label: '机械设计' },
  { value: '机械制造', label: '机械制造' },
  { value: '快消品', label: '快消品' },
  { value: '零售', label: '零售' },
  { value: '食品饮料', label: '食品饮料' },
  { value: '学前教育', label: '学前教育' },
  { value: 'K12教育', label: 'K12教育' },
  { value: '高等教育', label: '高等教育' },
  { value: '职业培训', label: '职业培训' },
  { value: '医疗器械', label: '医疗器械' },
  { value: '制药', label: '制药' },
  { value: '医院', label: '医院' },
  { value: '其他行业', label: '其他行业' }
]

const form = reactive({
  companyName: '',
  industry: '',
  scale: '',
  address: '',
  description: '',
  website: '',
  contactName: '',
  contactPhone: '',
  contactEmail: '',
  logoUrl: '',
  status: 0,
  rejectReason: ''
})

// 获取完整的图片URL
const getFullImageUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('http')) return url
  return 'http://localhost:8080' + url
}

// 处理Logo上传
const handleLogoChange = async (file) => {
  try {
    uploading.value = true
    const response = await uploadImage(file.raw)
    form.logoUrl = response.data.url
    ElMessage.success('Logo上传成功')
  } catch (error) {
    ElMessage.error(error.response?.data?.message || 'Logo上传失败')
    console.error(error)
  } finally {
    uploading.value = false
  }
}

// 计算是否可以提交审核
const canSubmitAudit = computed(() => {
  // 未提交过审核，或者被拒绝后可以重新提交
  return !isEditing.value && (form.status === 0 || form.status === 2)
})

const fetchCompanyInfo = async () => {
  try {
    loading.value = true
    const res = await getCompanyInfo()
    if (res.data) {
      Object.keys(form).forEach(key => {
        if (res.data[key] !== undefined && res.data[key] !== null) {
          form[key] = res.data[key]
        }
      })
      // 如果没有企业信息，默认进入编辑模式
      if (!res.data.companyName) {
        isEditing.value = true
      }
    } else {
      isEditing.value = true
    }
  } catch (error) {
    console.error(error)
    isEditing.value = true
  } finally {
    loading.value = false
  }
}

const startEdit = () => {
  isEditing.value = true
}

const handleSave = async () => {
  try {
    loading.value = true
    await saveCompanyInfo(form)
    ElMessage.success('企业信息保存成功')
    isEditing.value = false
    await fetchCompanyInfo()
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    loading.value = false
  }
}

// 提交审核
const handleSubmitAudit = async () => {
  try {
    await ElMessageBox.confirm(
      '提交审核后，运营人员将对您的企业进行审核，确认提交吗？',
      '提交审核确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    loading.value = true
    await submitCompanyAudit()
    ElMessage.success('已提交审核，请等待运营人员审核')
    await fetchCompanyInfo()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('提交审核失败')
      console.error(error)
    }
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchCompanyInfo()
})
</script>

<style scoped>
.company-info-container {
  max-width: 900px;
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

.logo-uploader .el-upload {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
}

.logo-uploader .el-upload:hover {
  border-color: #409EFF;
}

.logo-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 178px;
  height: 178px;
  text-align: center;
}

.logo {
  width: 178px;
  height: 178px;
  display: block;
}

.upload-tip {
  font-size: 12px;
  color: #999;
  margin-top: 10px;
}

</style>
