import request from '../utils/request'

export const getUserInfo = () => {
  return request.get('/user/info')
}

export const listUsers = (params) => {
  return request.get('/user/list', { params })
}

export const updateUserStatus = (id, status) => {
  return request.put(`/user/${id}/status`, null, { params: { status } })
}

export const deleteUser = (id) => {
  return request.delete(`/user/${id}`)
}

export const createUser = (data) => {
  return request.post('/user', data)
}

export const resetPassword = (id) => {
  return request.put(`/user/${id}/reset-password`)
}

export const changePassword = (data) => {
  return request.put('/user/password', data)
}

// ========== 企业信息相关 API ==========

export const getCompanyInfo = () => {
  return request.get('/user/company/info')
}

export const saveCompanyInfo = (data) => {
  return request.post('/user/company/info', data)
}

export const submitCompanyAudit = () => {
  return request.post('/user/company/submit-audit')
}

// ========== 投递简历相关 API ==========

export const applyJob = (jobId) => {
  return request.post(`/user/apply/${jobId}`)
}

export const checkApplyStatus = (jobId) => {
  return request.get(`/user/apply/${jobId}/status`)
}

export const getUserApplications = () => {
  return request.get('/user/applications')
}

export const getCompanyApplications = (params) => {
  return request.get('/user/company/applications', { params })
}

export const updateApplicationStatus = (id, status) => {
  return request.put(`/user/company/application/${id}/status`, null, { params: { status } })
}

export const getResumeById = (resumeId, applicationId) => {
  return request.get(`/user/resume/${resumeId}`, applicationId ? { params: { applicationId } } : {})
}

export const getCompanyApplicationInterviews = (applicationId) => {
  return request.get(`/user/company/application/${applicationId}/interviews`)
}

export const getUserApplicationInterviews = (applicationId) => {
  return request.get(`/user/application/${applicationId}/interviews`)
}

export const getCompanyApplicationStatusHistory = (applicationId) => {
  return request.get(`/user/company/application/${applicationId}/status-history`)
}

export const getUserApplicationStatusHistory = (applicationId) => {
  return request.get(`/user/application/${applicationId}/status-history`)
}

export const confirmInterviewRound = (roundId) => {
  return request.put(`/user/interview-round/${roundId}/confirm`)
}

export const requestInterviewReschedule = (roundId, data) => {
  return request.put(`/user/interview-round/${roundId}/reschedule`, data)
}

export const acceptInterviewReschedule = (roundId) => {
  return request.put(`/user/company/interview-round/${roundId}/reschedule/accept`)
}

export const createInterviewRound = (applicationId, data) => {
  return request.post(`/user/company/application/${applicationId}/interviews`, data)
}

export const updateInterviewRoundResult = (roundId, data) => {
  return request.put(`/user/company/interview-round/${roundId}/result`, data)
}

export const getCompanyApplicationOffer = (applicationId) => {
  return request.get(`/user/company/application/${applicationId}/offer`)
}

export const sendOffer = (applicationId, data) => {
  return request.post(`/user/company/application/${applicationId}/offer`, data)
}

export const getUserOffers = () => {
  return request.get('/user/offers')
}

export const respondOffer = (offerId, data) => {
  return request.put(`/user/offer/${offerId}/response`, data)
}

export const confirmOnboard = (applicationId) => {
  return request.put(`/user/company/application/${applicationId}/onboard`)
}

export const getNotifications = () => {
  return request.get('/user/notifications')
}

export const getUnreadNotificationCount = () => {
  return request.get('/user/notifications/unread-count')
}

export const markNotificationRead = (id) => {
  return request.put(`/user/notifications/${id}/read`)
}

export const markAllNotificationsRead = () => {
  return request.put('/user/notifications/read-all')
}
