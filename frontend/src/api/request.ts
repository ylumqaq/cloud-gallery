import axios, { type AxiosRequestConfig } from 'axios'
import { message } from 'antd'
import { clearToken, getToken } from '../utils/token'
import type { BaseResponse } from '../types/base'

// Sa-Token 鉴权头名称，需与后端 token-name 配置保持一致（Sa-Token 默认 satoken）
const TOKEN_HEADER = 'satoken'

// 创建统一的 axios 实例
const service = axios.create({
  baseURL: '/api',
  timeout: 15000,
})

// 请求拦截器：注入登录 token
service.interceptors.request.use((config) => {
  const token = getToken()
  if (token) {
    config.headers[TOKEN_HEADER] = token
  }
  return config
})

// 统一处理业务错误码
function handleBusinessError(res: BaseResponse<unknown>) {
  message.error(res.message || '请求失败')
  // 未登录：清除 token 并整页跳转登录页（整页刷新使内存状态归零）
  if (res.code === 40100) {
    clearToken()
    if (window.location.pathname !== '/login') {
      window.location.href = '/login'
    }
  }
}

// 响应拦截器：仅处理网络层错误，业务错误码在 request 函数中统一解包
service.interceptors.response.use(
  (response) => response,
  (error) => {
    message.error('网络异常，请稍后重试')
    return Promise.reject(error)
  },
)

// 类型安全的请求辅助函数：解包 BaseResponse 并统一处理错误码
export async function request<T>(config: AxiosRequestConfig): Promise<T> {
  const response = await service.request<BaseResponse<T>>(config)
  const res = response.data
  if (res.code !== 0) {
    handleBusinessError(res)
    throw res
  }
  return res.data
}

export default request
