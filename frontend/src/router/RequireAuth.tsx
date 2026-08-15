import type { ReactNode } from 'react'
import { Navigate, useLocation } from 'react-router-dom'
import { useUserStore } from '../stores/user'
import type { SystemRole } from '../types/user'

// 鉴权守卫 props：requiredRole 用于需要特定系统角色的路由
interface RequireAuthProps {
  children: ReactNode
  requiredRole?: SystemRole | SystemRole[]
}

// 鉴权守卫组件：未登录重定向登录页；角色不足重定向首页
export default function RequireAuth({ children, requiredRole }: RequireAuthProps) {
  const token = useUserStore((state) => state.token)
  const loginUser = useUserStore((state) => state.loginUser)
  const location = useLocation()

  // 未登录：重定向登录页并记录来源路径
  if (!token) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />
  }

  // 角色校验：loginUser 已加载且角色不匹配时拦截（后端接口会做最终兜底校验）
  if (requiredRole) {
    const roles = Array.isArray(requiredRole) ? requiredRole : [requiredRole]
    const userRole = loginUser?.userRole
    if (userRole && !roles.includes(userRole)) {
      return <Navigate to="/" replace />
    }
  }

  return <>{children}</>
}
