import request from '../utils/request'

// 上传图片
export const uploadImage = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/upload/image', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 上传PDF
export const uploadPdf = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/upload/pdf', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 删除文件
export const deleteFile = (filePath) => {
  return request.delete('/upload/file', { params: { filePath } })
}
