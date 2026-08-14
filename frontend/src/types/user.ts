// 系统角色，与后端三级角色保持一致
export type SystemRole = 'user' | 'admin' | 'super_admin'

// 登录用户（脱敏信息）
export interface LoginUserVO {
  id: number
  userAccount: string
  userName: string
  userAvatar: string
  userRole: SystemRole
}

// 登录结果：用户脱敏信息 + token（字段以实际后端返回为准）
export interface LoginResultVO extends LoginUserVO {
  token: string
}

// 注册请求
export interface UserRegisterRequest {
  userAccount: string
  userPassword: string
  checkPassword: string
}

// 登录请求
export interface UserLoginRequest {
  userAccount: string
  userPassword: string
}
