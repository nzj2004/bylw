<template>
  <div class="company-info-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <div class="title-area">
            <h3>企业信息</h3>
            <el-tag :type="statusType" size="large">{{ statusText }}</el-tag>
            <el-tag v-if="form.hasPendingChanges && form.status === 1" type="warning" size="large">
              有待提交修改
            </el-tag>
          </div>
          <div class="actions">
            <el-button v-if="isEditing" @click="cancelEdit">取消</el-button>
            <el-button v-if="isEditing" type="primary" plain @click="handleSave" :loading="loading">
              保存草稿
            </el-button>
            <el-button v-else-if="canEdit" type="primary" plain @click="startEdit">修改资料</el-button>
            <el-button v-if="canSubmitAudit" type="success" @click="handleSubmitAudit" :loading="loading">
              提交审核
            </el-button>
          </div>
        </div>
      </template>

      <el-alert
        v-if="form.status === 0"
        title="企业信息正在审核"
        description="审核期间正式企业资料不会被更新；审核通过后，新资料才会生效。"
        type="warning"
        :closable="false"
        show-icon
        class="status-alert"
      />
      <el-alert
        v-else-if="form.status === 2"
        title="企业审核未通过"
        :description="form.rejectReason || '请修改企业资料后重新提交审核。'"
        type="error"
        :closable="false"
        show-icon
        class="status-alert"
      />
      <el-alert
        v-else-if="form.hasPendingChanges"
        title="有已保存但未提交审核的修改"
        description="点击右上角“提交审核”后，运营审核通过才会更新正式企业资料。"
        type="info"
        :closable="false"
        show-icon
        class="status-alert"
      />

      <el-form :model="form" label-position="top">
        <el-form-item label="企业 Logo">
          <el-upload
            class="logo-uploader"
            action=""
            :auto-upload="false"
            :show-file-list="false"
            :on-change="handleLogoChange"
            accept="image/jpeg,image/png,image/gif,image/webp"
            :disabled="!isEditing"
          >
            <img v-if="form.logoUrl" :src="getImageUrl(form.logoUrl)" class="logo" />
            <el-icon v-else class="logo-uploader-icon"><Plus /></el-icon>
          </el-upload>
          <div class="upload-tip" v-if="isEditing">支持 JPG、PNG、GIF、WEBP，大小不超过 5MB</div>
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
import { getImageUrl } from '../../utils/image'

const loading = ref(false)
const isEditing = ref(false)
const originalForm = ref(null)

const industryOptions = [
  '互联网', '电子商务', '计算机软件', '生活服务(O2O)', '企业服务', '医疗健康',
  '游戏', '人工智能', '云计算', '大数据', '半导体/芯片', '电子/硬件开发',
  '通信/网络设备', '智能硬件/消费电子', '银行', '证券', '保险', '基金',
  '互联网金融', '房地产开发', '物业管理', '建筑设计', '工程施工', '汽车研发',
  '汽车制造', '机械设计', '机械制造', '快消品', '零售', '食品饮料',
  '学前教育', 'K12教育', '高等教育', '职业培训', '医疗器械', '制药',
  '医院', '其他行业'
].map(item => ({ value: item, label: item }))

const form = reactive({
  id: null,
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
  statusName: '',
  rejectReason: '',
  hasPendingChanges: false
})

const statusText = computed(() => {
  if (form.status === 0) return '审核中'
  if (form.status === 1) return '已通过'
  if (form.status === 2) return '未通过'
  return '未提交'
})

const statusType = computed(() => {
  if (form.status === 0) return 'warning'
  if (form.status === 1) return 'success'
  if (form.status === 2) return 'danger'
  return 'info'
})

const canEdit = computed(() => form.status !== 0)
const canSubmitAudit = computed(() => !isEditing.value && (form.status === 2 || (form.status === 1 && form.hasPendingChanges)))

const snapshot = () => JSON.parse(JSON.stringify(form))

const fillForm = (data = {}) => {
  Object.keys(form).forEach(key => {
    form[key] = data[key] ?? (typeof form[key] === 'boolean' ? false : '')
  })
  form.id = data.id || null
  form.status = data.status ?? 0
  form.hasPendingChanges = Boolean(data.hasPendingChanges)
  originalForm.value = snapshot()
}

const fetchCompanyInfo = async () => {
  try {
    loading.value = true
    const res = await getCompanyInfo()
    fillForm(res.data || {})
    if (!form.companyName) {
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
  originalForm.value = snapshot()
  isEditing.value = true
}

const cancelEdit = () => {
  if (originalForm.value) {
    fillForm(originalForm.value)
  }
  isEditing.value = false
}

const handleLogoChange = async (file) => {
  try {
    loading.value = true
    const response = await uploadImage(file.raw)
    form.logoUrl = response.data.url
    ElMessage.success('Logo 上传成功')
  } catch (error) {
    ElMessage.error(error.response?.data?.message || 'Logo 上传失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleSave = async () => {
  try {
    loading.value = true
    await saveCompanyInfo(form)
    ElMessage.success(form.id ? '修改已保存，请提交审核' : '企业信息已保存，正在等待审核')
    isEditing.value = false
    await fetchCompanyInfo()
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    loading.value = false
  }
}

const handleSubmitAudit = async () => {
  try {
    await ElMessageBox.confirm(
      '提交审核后，企业岗位将暂时下架。审核通过后，新企业资料才会生效。',
      '提交审核确认',
      {
        confirmButtonText: '提交审核',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    loading.value = true
    await submitCompanyAudit()
    ElMessage.success('已提交审核，请等待运营审核')
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
  gap: 16px;
}

.title-area,
.actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.card-header h3 {
  margin: 0;
}

.status-alert {
  margin-bottom: 20px;
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
  object-fit: contain;
  display: block;
}

.upload-tip {
  font-size: 12px;
  color: #999;
  margin-top: 10px;
}
</style>
