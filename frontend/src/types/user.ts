// 系统角色，与后端三级角色保持一致
export type SystemRole = 'user' | 'admin' | 'super_admin'

// 用户脱敏信息（GET /api/user/get/login 返回，不含密码与 token）
export interface UserVO {
  id: number
  userAccount: string
  userName: string
  userAvatar: string
  userProfile?: string
  userRole: SystemRole
  createTime?: string
}

// 登录结果：脱敏信息 + token（POST /api/user/login 返回）
export interface LoginUserVO extends UserVO {
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
