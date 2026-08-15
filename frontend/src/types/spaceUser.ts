// 空间角色：viewer / editor / admin
export type SpaceRole = 'viewer' | 'editor' | 'admin'

// 空间成员视图对象
export interface SpaceUserVO {
  id: number
  spaceId: number
  userId: number
  spaceRole: SpaceRole
  createTime?: string
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

// 当前用户在某空间的权限（对应后端 SpaceUserAuthVO）
export interface SpaceUserPermissionVO {
  role?: 'creator' | SpaceRole // creator（创建者）/ viewer / editor / admin；公共图库或非成员为 null
  permissions: string[] // 权限码列表
}
