import request from '../utils/request'

// 获取企业列表（运营端使用）
export const listCompanies = (params) => {
  return request.get('/operator/companies', { params })
}

// 审核企业
export const auditCompany = (id, status, reason) => {
  return request.put(`/operator/company/${id}/audit`, null, { params: { status, reason } })
}
