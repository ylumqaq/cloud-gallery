import { useEffect, useState } from 'react'
import { useSpaceStore } from '../stores/space'
import { getSpaceApi } from '../api/space'
import { hasPermission } from '../utils/permission'
import type { PermissionCode } from '../constants/permission'
import type { SpaceVO } from '../types/space'

// 空间上下文加载 hook：加载空间信息、同步 spaceStore 并校验权限
export function useSpaceContext(spaceId: number, requiredPermission?: PermissionCode) {
  const setCurrentSpace = useSpaceStore((s) => s.setCurrentSpace)
  const fetchSpacePermission = useSpaceStore((s) => s.fetchSpacePermission)

  const [space, setSpace] = useState<SpaceVO | null>(null)
  const [loading, setLoading] = useState(true)
  const [denied, setDenied] = useState(false)

  useEffect(() => {
    let cancelled = false
    setLoading(true)
    ;(async () => {
      try {
        const space = await getSpaceApi(spaceId)
        if (cancelled) return
        setSpace(space)
        setCurrentSpace(space)
        await fetchSpacePermission(spaceId)
        if (cancelled) return
        // 校验空间权限：无权限则标记为拒绝访问
        if (requiredPermission && !hasPermission(requiredPermission)) {
          setDenied(true)
        }
      } catch {
        // 错误提示已由请求拦截器统一处理
      } finally {
        if (!cancelled) setLoading(false)
      }
    })()

    return () => {
      cancelled = true
      setCurrentSpace(null) // 离开空间时清理空间上下文（保留 spaceList）
    }
  }, [spaceId, requiredPermission, setCurrentSpace, fetchSpacePermission])

  return { space, loading, denied }
}
