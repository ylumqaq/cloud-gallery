// 空间角色：viewer / editor / admin
export type SpaceRole = 'viewer' | 'editor' | 'admin'

// 空间成员视图对象
export interface SpaceUserVO {
  id: number
  spaceId: number
  userId: number
  spaceRole: SpaceRole
  userAccount?: string
  userName?: string
  userAvatar?: string
}

// 添加空间成员请求
export interface SpaceUserAddRequest {
  spaceId: number
  userId: number
  spaceRole: SpaceRole
}

// 修改空间成员请求
export interface SpaceUserEditRequest {
  spaceId: number
  userId: number
  spaceRole: SpaceRole
}

// 当前用户在某空间的权限响应
export interface SpaceUserPermissionVO {
  spaceRole?: SpaceRole
  permissionList: string[] // 权限码列表
}
