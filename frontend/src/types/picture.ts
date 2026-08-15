// 图片视图对象，与后端 PictureVO 对应
export interface PictureVO {
  id: number
  url: string
  thumbnailUrl?: string
  name: string
  category?: string
  tags?: string[] // 后端存储为 JSON 数组字符串，前端解析为数组
  picSize?: number
  picWidth?: number
  picHeight?: number
  picFormat?: string
  picColor?: string
  spaceId?: number // 空 = 公共图库
  userId?: number
  createTime?: string
  editTime?: string
}

// 图片分页查询请求
export interface PictureQueryRequest {
  current: number
  pageSize: number
  spaceId?: number
  searchText?: string
  picColor?: string
}

// 编辑图片请求
export interface PictureEditRequest {
  id: number
  name?: string
  category?: string
  tags?: string // JSON 数组字符串
  spaceId?: number
}
