import { useSpaceStore } from '../stores/space'
import { PUBLIC_PERMISSIONS, type PermissionCode } from '../constants/permission'

// 判断当前用户在「当前空间 / 公共图库」下是否拥有指定权限码
export function hasPermission(code: PermissionCode): boolean {
  const { currentSpace, spacePermission } = useSpaceStore.getState()
  // 公共图库：使用后端配置的公共权限（picture:view / picture:upload）
  if (!currentSpace) {
    return PUBLIC_PERMISSIONS.includes(code)
  }
  // 空间：以当前用户在空间内的权限码列表为准
  return spacePermission?.permissions?.includes(code) ?? false
}
