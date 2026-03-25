import axios from 'axios'
import { showToast } from 'vant'

const request = axios.create({
  baseURL: '/fitcheck-api',
  timeout: 10000,
  headers: { 'Content-Type': 'application/json' }
})

// 请求拦截器：自动带 token
request.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = 'Bearer ' + token
  }
  return config
})

// 响应拦截器：统一处理错误
request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code !== 200) {
      showToast({ message: res.message || '请求失败', forbidClick: true })
      return Promise.reject(new Error(res.message))
    }
    return res
  },
  error => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      location.href = '/login'
    } else {
      showToast({ message: error.response?.data?.message || '网络错误', forbidClick: true })
    }
    return Promise.reject(error)
  }
)

export default request
