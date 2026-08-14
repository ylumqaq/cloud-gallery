// 空间类型：0 私有 / 1 团队
export type SpaceType = 0 | 1

// 空间视图对象
export interface SpaceVO {
  id: number
  spaceName: string
  spaceType: SpaceType
  userId?: number // 创建者
  createTime?: string
  editTime?: string
}

// 创建空间请求
export interface SpaceAddRequest {
  spaceName: string
  spaceType: SpaceType
}

// 编辑空间请求
export interface SpaceEditRequest {
  id: number
  spaceName: string
}
