import request from '../utils/request'

// 收藏职位
export const addFavorite = (jobId) => {
  return request.post(`/favorite/${jobId}`)
}

// 取消收藏
export const removeFavorite = (jobId) => {
  return request.delete(`/favorite/${jobId}`)
}

// 查询是否已收藏
export const isFavorite = (jobId) => {
  return request.get(`/favorite/${jobId}/status`)
}

// 获取收藏列表
export const listFavorites = (params) => {
  return request.get('/favorite/list', { params })
}
