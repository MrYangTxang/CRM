import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: '/api',
  timeout: 30000
})

// 请求拦截器：附加认证 token
request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => Promise.reject(error)
)

// 响应拦截器：解包统一 ApiResponse {code, message, data}
request.interceptors.response.use(
  response => {
    // 如果响应类型是 blob（Excel 导出等），直接返回 blob
    if (response.config?.responseType === 'blob') {
      return response.data
    }
    const body = response.data
    if (body && body.code === 200) {
      return body.data
    }
    // 业务异常（code !== 200）：提示错误并 reject，让调用方进入 catch
    const errMsg = body?.message || '操作失败'
    ElMessage.error(errMsg)
    return Promise.reject(new Error(errMsg))
  },
  error => {
    // HTTP 错误（网络异常、500等）
    let msg = '网络错误，请稍后重试'
    if (error.response?.data) {
      // 如果后端返回了 JSON（如 GlobalExceptionHandler 处理的异常）
      msg = error.response.data.message || msg
    }
    ElMessage.error(msg)
    return Promise.reject(error)
  }
)

export default request
