import request from '../utils/request'

export const createJob = (data) => {
  return request.post('/job', data)
}

export const updateJob = (id, data) => {
  return request.put(`/job/${id}`, data)
}

export const deleteJob = (id) => {
  return request.delete(`/job/${id}`)
}

export const getJobById = (id) => {
  return request.get(`/job/${id}`)
}

export const listJobs = (params) => {
  return request.get('/job/list', { params })
}

export const listHotJobs = (params) => {
  return request.get('/job/hot', { params })
}

export const listCompanyJobs = (companyId, params) => {
  return request.get(`/job/company/${companyId}`, { params })
}

export const auditJob = (id, status, reason) => {
  return request.put(`/job/${id}/audit`, null, { params: { status, reason } })
}

export const batchAuditJobs = (data) => {
  return request.put('/job/batch-audit', data)
}

export const batchPublishJobs = (jobs) => {
  return request.post('/job/batch-publish', jobs)
}

export const toggleJobStatus = (id, status) => {
  return request.put(`/job/${id}/status`, null, { params: { status } })
}
