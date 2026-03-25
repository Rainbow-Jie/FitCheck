import request from '@/utils/request'

export const authApi = {
  login: (data) => request.post('/auth/login', data),
  register: (data) => request.post('/auth/register', data),
  me: () => request.post('/auth/me'),
  changePassword: (data) => request.post('/auth/change-password', data)
}
