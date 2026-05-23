<template>
  <div class="batch-publish">
    <el-card>
      <template #header>
        <div class="card-header">
          <h3>批量发布职位</h3>
          <div>
            <el-button type="success" @click="downloadTemplate">下载 Excel 模板</el-button>
            <el-upload
              class="excel-upload"
              accept=".xlsx,.xls"
              :auto-upload="false"
              :on-change="handleExcelUpload"
              :show-file-list="false"
              style="display: inline-block; margin: 0 10px"
            >
              <el-button type="warning">Excel 导入</el-button>
            </el-upload>
            <el-button type="primary" @click="addJob">新增职位</el-button>
          </div>
        </div>
      </template>

      <el-form :model="form" ref="formRef">
        <div v-for="(job, index) in form.jobs" :key="index" class="job-form-item">
          <el-divider>职位 {{ index + 1 }}</el-divider>
          <el-row :gutter="20">
            <el-col :span="8">
              <el-form-item
                :label="`职位标题`"
                :prop="`jobs.${index}.title`"
                :rules="{ required: true, message: '请填写职位标题' }"
              >
                <el-input v-model="job.title" placeholder="职位标题" />
              </el-form-item>
            </el-col>

            <el-col :span="8">
              <el-form-item
                :label="`企业ID`"
                :prop="`jobs.${index}.companyId`"
                :rules="{ required: true, message: '请填写企业ID' }"
              >
                <el-input-number v-model="job.companyId" placeholder="企业ID" style="width: 100%" />
              </el-form-item>
            </el-col>

            <el-col :span="8">
              <el-form-item :label="`职位类别`" :prop="`jobs.${index}.category`">
                <el-cascader
                  v-model="job.category"
                  :options="categoryOptions"
                  :props="{ emitPath: false, checkStrictly: true }"
                  placeholder="职位类别"
                  style="width: 100%"
                  clearable
                />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="6">
              <el-form-item :label="`最低薪资(K)`">
                <el-input-number v-model="job.salaryMin" :min="0" style="width: 100%" />
              </el-form-item>
            </el-col>

            <el-col :span="6">
              <el-form-item :label="`最高薪资(K)`">
                <el-input-number v-model="job.salaryMax" :min="0" style="width: 100%" />
              </el-form-item>
            </el-col>

            <el-col :span="6">
              <el-form-item :label="`工作城市`">
                <el-input v-model="job.workCity" placeholder="工作城市" />
              </el-form-item>
            </el-col>

            <el-col :span="6">
              <el-form-item :label="`学历要求`">
                <el-select v-model="job.education" placeholder="学历要求" style="width: 100%">
                  <el-option label="不限" value="不限" />
                  <el-option label="大专" value="大专" />
                  <el-option label="本科" value="本科" />
                  <el-option label="硕士" value="硕士" />
                  <el-option label="博士" value="博士" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="职位描述">
            <el-input v-model="job.jobDesc" type="textarea" :rows="3" placeholder="职位描述" />
          </el-form-item>

          <el-form-item label="岗位要求">
            <el-input v-model="job.requirements" type="textarea" :rows="3" placeholder="岗位要求" />
          </el-form-item>

          <el-form-item>
            <el-button type="danger" @click="removeJob(index)">删除此条</el-button>
          </el-form-item>
        </div>
      </el-form>

      <div class="form-actions" v-if="form.jobs.length > 0">
        <el-button type="primary" size="large" :loading="submitting" @click="handleSubmit">
          批量发布
        </el-button>
        <el-button size="large" @click="resetForm">重置</el-button>
      </div>

      <el-empty v-else description="点击上方按钮新增职位" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import * as XLSX from 'xlsx'
import { batchPublishJobs } from '../../api/job'
import { JOB_CATEGORY_OPTIONS } from '../../constants/jobCategoryOptions'

const formRef = ref(null)
const submitting = ref(false)

const form = reactive({
  jobs: []
})

const categoryOptions = JOB_CATEGORY_OPTIONS

const createEmptyJob = () => ({
  title: '',
  companyId: null,
  category: '',
  salaryMin: null,
  salaryMax: null,
  workCity: '',
  education: '',
  experience: '',
  jobDesc: '',
  requirements: '',
  jobType: 1,
  status: 1
})

const addJob = () => {
  form.jobs.push(createEmptyJob())
}

const removeJob = (index) => {
  form.jobs.splice(index, 1)
}

const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) {
      return
    }

    if (form.jobs.length === 0) {
      ElMessage.warning('请至少添加一条职位')
      return
    }

    submitting.value = true
    try {
      await batchPublishJobs(form.jobs)
      ElMessage.success('批量发布成功')
      form.jobs = []
    } catch (error) {
      console.error(error)
      ElMessage.error('批量发布失败')
    } finally {
      submitting.value = false
    }
  })
}

const resetForm = () => {
  form.jobs = []
}

const downloadTemplate = () => {
  const templateData = [
    {
      '职位标题': 'Java 开发工程师',
      '企业ID': 1,
      '职位类别': '开发/测试/运维',
      '最低薪资(K)': 15,
      '最高薪资(K)': 25,
      '工作城市': '上海',
      '学历要求': '本科',
      '经验要求': '3-5年',
      '职位描述': '负责后端系统设计与开发',
      '岗位要求': '1. 熟练 Java\n2. 具备 Spring Boot 使用经验'
    }
  ]

  const ws = XLSX.utils.json_to_sheet(templateData)
  const wb = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(wb, ws, '职位模板')
  XLSX.writeFile(wb, '职位批量发布模板.xlsx')
  ElMessage.success('模板下载成功')
}

const handleExcelUpload = (file) => {
  const reader = new FileReader()
  reader.onload = (e) => {
    try {
      const data = new Uint8Array(e.target.result)
      const workbook = XLSX.read(data, { type: 'array' })
      const firstSheet = workbook.Sheets[workbook.SheetNames[0]]
      const jsonData = XLSX.utils.sheet_to_json(firstSheet)

      if (jsonData.length === 0) {
        ElMessage.warning('Excel 文件为空')
        return
      }

      const jobs = jsonData.map((row) => ({
        title: row['职位标题'] || row['title'] || '',
        companyId: Number(row['企业ID'] || row['companyId'] || 0),
        category: row['职位类别'] || row['category'] || '',
        salaryMin: Number(row['最低薪资(K)'] || row['salaryMin'] || 0),
        salaryMax: Number(row['最高薪资(K)'] || row['salaryMax'] || 0),
        workCity: row['工作城市'] || row['workCity'] || '',
        education: row['学历要求'] || row['education'] || '',
        experience: row['经验要求'] || row['experience'] || '',
        jobDesc: row['职位描述'] || row['jobDesc'] || '',
        requirements: row['岗位要求'] || row['requirements'] || '',
        jobType: 1,
        status: 1
      }))

      const validJobs = jobs.filter((job) => job.title && job.companyId)
      if (validJobs.length === 0) {
        ElMessage.error('Excel 数据格式不正确，请检查数据是否完整')
        return
      }

      form.jobs = validJobs
      ElMessage.success(`成功导入 ${validJobs.length} 条职位`)
    } catch (error) {
      console.error(error)
      ElMessage.error('Excel 解析失败，请检查文件格式')
    }
  }
  reader.readAsArrayBuffer(file.raw)
}
</script>

<style scoped>
.batch-publish {
  max-width: 1200px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.job-form-item {
  background: #f5f7fa;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  border: 1px solid #e4e7ed;
}

.form-actions {
  text-align: center;
  margin-top: 30px;
}
</style>
