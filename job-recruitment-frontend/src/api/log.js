import request from '../utils/request'

// 获取日志列表
export const listLogs = (params) => {
  return request.get('/log/list', { params })
}

// 清空日志
export const clearLogs = () => {
  return request.delete('/log/clear')
}
