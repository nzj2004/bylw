import request from '../utils/request'

// 获取简历信息
export const getResumeInfo = () => {
  return request.get('/user/resume/info')
}

// 保存简历
export const saveResume = (data) => {
  return request.post('/user/resume/info', data)
}
