// 空间权限码，与后端 spaceUserAuthConfig.json 保持一致
export const PERMISSION = {
  PICTURE_VIEW: 'picture:view',
  PICTURE_UPLOAD: 'picture:upload',
  PICTURE_EDIT: 'picture:edit',
  PICTURE_DELETE: 'picture:delete',
  SPACE_USER_MANAGE: 'spaceUser:manage',
} as const

// 权限码联合类型
export type PermissionCode = (typeof PERMISSION)[keyof typeof PERMISSION]

// 系统角色
export const SYSTEM_ROLE = {
  USER: 'user',
  ADMIN: 'admin',
  SUPER_ADMIN: 'super_admin',
} as const

// 空间角色
export const SPACE_ROLE = {
  VIEWER: 'viewer',
  EDITOR: 'editor',
  ADMIN: 'admin',
} as const

// 公共图库默认权限（与后端 spaceUserAuthConfig.json 的 publicPermissions 一致）
export const PUBLIC_PERMISSIONS: PermissionCode[] = [
  PERMISSION.PICTURE_VIEW,
  PERMISSION.PICTURE_UPLOAD,
]
