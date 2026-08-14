// 统一响应结构，与后端 BaseResponse<T> 保持一致
export interface BaseResponse<T> {
  code: number
  data: T
  message: string
}

// 分页结果，与后端分页查询接口返回结构保持一致
export interface PageResult<T> {
  current: number
  pageSize: number
  total: number
  records: T[]
}
