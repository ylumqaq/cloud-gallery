import { create } from 'zustand'
import type { SpaceVO } from '../types/space'
import type { SpaceUserPermissionVO } from '../types/spaceUser'
import { listMySpaceApi } from '../api/space'
import { getMySpacePermissionApi } from '../api/spaceUser'

// 空间状态
interface SpaceState {
  currentSpace: SpaceVO | null
  spaceList: SpaceVO[]
  spacePermission: SpaceUserPermissionVO | null
  setCurrentSpace: (space: SpaceVO | null) => void
  fetchMySpaces: () => Promise<void>
  fetchSpacePermission: (spaceId: number) => Promise<void>
  reset: () => void
}

// 空间 store：管理当前空间、空间列表与当前用户在该空间的权限
export const useSpaceStore = create<SpaceState>()((set) => ({
  currentSpace: null, // null = 公共图库
  spaceList: [],
  spacePermission: null,

  // 设置当前空间：切换空间时同步清空旧权限，由页面重新拉取
  setCurrentSpace(space) {
    set({ currentSpace: space, spacePermission: null })
  },

  // 拉取我的空间列表
  async fetchMySpaces() {
    const list = await listMySpaceApi()
    set({ spaceList: list })
  },

  // 拉取当前用户在某空间的权限（进入空间页面时调用）
  async fetchSpacePermission(spaceId) {
    const permission = await getMySpacePermissionApi(spaceId)
    set({ spacePermission: permission })
  },

  // 清空空间相关状态
  reset() {
    set({ currentSpace: null, spaceList: [], spacePermission: null })
  },
}))
