<template>
  <div class="resume-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <h3>我的简历</h3>
          <el-button type="primary" @click="handleSave" :loading="saving">保存简历</el-button>
        </div>
      </template>
      
      <el-form :model="form" label-position="top">
        <!-- 头像上传 -->
        <el-form-item label="个人照片">
          <el-upload
            class="avatar-uploader"
            action=""
            :auto-upload="false"
            :show-file-list="false"
            :on-change="handleAvatarChange"
            accept="image/jpeg,image/png,image/gif,image/webp"
          >
            <img v-if="form.avatarUrl" :src="getFullImageUrl(form.avatarUrl)" class="avatar" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
          <div class="upload-tip">支持 JPG、PNG、GIF、WEBP 格式，大小不超过5MB</div>
        </el-form-item>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="姓名">
              <el-input v-model="form.realName" placeholder="请输入姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="性别">
              <el-radio-group v-model="form.gender">
                <el-radio :label="1">男</el-radio>
                <el-radio :label="0">女</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="手机号">
              <el-input v-model="form.phone" placeholder="请输入手机号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱">
              <el-input v-model="form.email" placeholder="请输入邮箱" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="最高学历">
              <el-select v-model="form.education" placeholder="请选择学历" style="width: 100%">
                <el-option label="大专" value="大专" />
                <el-option label="本科" value="本科" />
                <el-option label="硕士" value="硕士" />
                <el-option label="博士" value="博士" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="毕业院校">
              <el-input v-model="form.school" placeholder="请输入毕业院校" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="专业">
              <el-input v-model="form.major" placeholder="请输入专业" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="工作经历">
          <el-input v-model="form.workExperience" type="textarea" :rows="4" placeholder="请描述您的工作经历" />
        </el-form-item>
        
        <el-form-item label="项目经验">
          <el-input v-model="form.projectExp" type="textarea" :rows="4" placeholder="请描述您的项目经验" />
        </el-form-item>
        
        <el-form-item label="技能特长">
          <el-input v-model="form.skills" type="textarea" :rows="3" placeholder="请描述您的技能特长" />
        </el-form-item>
        
        <el-form-item label="自我评价">
          <el-input v-model="form.selfEval" type="textarea" :rows="4" placeholder="请进行自我评价" />
        </el-form-item>
        
        <!-- PDF简历上传 -->
        <el-divider>简历附件</el-divider>
        <el-form-item label="上传PDF简历">
          <el-upload
            class="pdf-uploader"
            action=""
            :auto-upload="false"
            :limit="1"
            :on-change="handlePdfChange"
            accept=".pdf"
          >
            <el-button type="primary" :icon="Upload">
              {{ form.pdfUrl ? '更换PDF简历' : '上传PDF简历' }}
            </el-button>
          </el-upload>
          <div v-if="form.pdfUrl" class="pdf-preview">
            <el-link :href="getFullImageUrl(form.pdfUrl)" target="_blank" type="primary">
              查看已上传的PDF简历
            </el-link>
            <el-button type="danger" size="small" @click="removePdf" style="margin-left: 10px">
              删除
            </el-button>
          </div>
          <div class="upload-tip">仅支持PDF格式，大小不超过10MB</div>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Upload } from '@element-plus/icons-vue'
import { uploadImage, uploadPdf } from '../../api/upload'
import { getResumeInfo, saveResume } from '../../api/resume'

const saving = ref(false)
const uploading = ref(false)

const form = reactive({
  realName: '',
  gender: 1,
  phone: '',
  email: '',
  education: '',
  school: '',
  major: '',
  workExperience: '',
  projectExp: '',
  skills: '',
  selfEval: '',
  avatarUrl: '',
  pdfUrl: ''
})

// 获取完整的图片URL
const getFullImageUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('http')) return url
  return 'http://localhost:8080' + url
}

// 处理头像上传
const handleAvatarChange = async (file) => {
  try {
    uploading.value = true
    const response = await uploadImage(file.raw)
    form.avatarUrl = response.data.url
    ElMessage.success('头像上传成功')
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '头像上传失败')
    console.error(error)
  } finally {
    uploading.value = false
  }
}

// 处理PDF上传
const handlePdfChange = async (file) => {
  try {
    uploading.value = true
    const response = await uploadPdf(file.raw)
    form.pdfUrl = response.data.url
    ElMessage.success('PDF上传成功')
  } catch (error) {
    ElMessage.error(error.response?.data?.message || 'PDF上传失败')
    console.error(error)
  } finally {
    uploading.value = false
  }
}

// 删除PDF
const removePdf = () => {
  form.pdfUrl = ''
  ElMessage.success('已删除PDF')
}

// 获取简历信息
const fetchResume = async () => {
  try {
    const res = await getResumeInfo()
    if (res.data) {
      Object.keys(form).forEach(key => {
        if (res.data[key] !== undefined && res.data[key] !== null) {
          form[key] = res.data[key]
        }
      })
    }
  } catch (error) {
    console.error(error)
  }
}

// 保存简历
const handleSave = async () => {
  try {
    saving.value = true
    await saveResume(form)
    ElMessage.success('保存成功')
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '保存失败')
    console.error(error)
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  fetchResume()
})
</script>

<style scoped>
.resume-container {
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

.avatar-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: all 0.3s;
  width: 178px;
  height: 178px;
}

.avatar-uploader:hover {
  border-color: #409eff;
}

.avatar {
  width: 178px;
  height: 178px;
  display: block;
  object-fit: cover;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 178px;
  height: 178px;
  text-align: center;
  line-height: 178px;
}

.upload-tip {
  font-size: 12px;
  color: #999;
  margin-top: 8px;
}

.pdf-preview {
  margin-top: 10px;
  display: flex;
  align-items: center;
}

.pdf-uploader {
  display: inline-block;
}
</style>
