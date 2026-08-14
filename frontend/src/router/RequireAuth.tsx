import type { ReactNode } from 'react'
import { Navigate, useLocation } from 'react-router-dom'
import { useUserStore } from '../stores/user'

// 鉴权守卫组件：未登录则重定向到登录页，并记录来源路径
export default function RequireAuth({ children }: { children: ReactNode }) {
  const token = useUserStore((state) => state.token)
  const location = useLocation()

  if (!token) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />
  }

  return <>{children}</>
}
