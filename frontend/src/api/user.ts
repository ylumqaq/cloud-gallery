import { request } from './request'
import type { LoginResultVO, LoginUserVO, UserLoginRequest, UserRegisterRequest } from '../types/user'

// 注册
export function registerApi(data: UserRegisterRequest) {
  return request<null>({ url: '/user/register', method: 'post', data })
}

// 登录
export function loginApi(data: UserLoginRequest) {
  return request<LoginResultVO>({ url: '/user/login', method: 'post', data })
}

// 获取当前登录用户
export function getLoginUserApi() {
  return request<LoginUserVO>({ url: '/user/get/login', method: 'get' })
}

// 退出登录
export function logoutApi() {
  return request<null>({ url: '/user/logout', method: 'post' })
}

// 修改用户角色（仅 super_admin）
export function updateUserRoleApi(data: { targetUserId: number; userRole: string }) {
  return request<null>({ url: '/user/role', method: 'put', data })
}
