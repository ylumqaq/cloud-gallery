import type { ReactNode } from 'react'
import { hasPermission } from '../utils/permission'
import type { PermissionCode } from '../constants/permission'

// 权限组件 props
interface PermissionProps {
  code: PermissionCode // 所需权限码
  fallback?: ReactNode // 无权限时的替代内容，默认 null
  children: ReactNode // 有权限时渲染的内容
}

// 按钮级权限组件：无权限时不渲染 children（可自定义 fallback）
export default function Permission({ code, fallback = null, children }: PermissionProps) {
  return <>{hasPermission(code) ? children : fallback}</>
}
