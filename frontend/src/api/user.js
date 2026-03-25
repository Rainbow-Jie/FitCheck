import request from '@/utils/request'

export const userApi = {
  info: () => request.post('/user/info'),
  stats: () => request.post('/user/stats'),
  updateInfo: (data) => request.post('/user/update/info', data),
  uploadAvatar: (formData) => request.post('/user/avatar', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

