import { request } from './request'
import type { PageResult } from '../types/base'
import type { PictureEditRequest, PictureQueryRequest, PictureVO } from '../types/picture'

// 分页查询图片
export function listPictureByPageApi(params: PictureQueryRequest) {
  return request<PageResult<PictureVO>>({ url: '/picture/list/page/vo', method: 'get', params })
}

// 图片详情
export function getPictureApi(id: number) {
  return request<PictureVO>({ url: `/picture/get/${id}`, method: 'get' })
}

// 上传图片（本地文件或网络 URL 二选一，使用 FormData 提交）
export function uploadPictureApi(data: FormData) {
  return request<PictureVO>({ url: '/picture/upload', method: 'post', data })
}

// 编辑图片
export function editPictureApi(data: PictureEditRequest) {
  return request<null>({ url: '/picture/edit', method: 'post', data })
}

// 删除图片
export function deletePictureApi(id: number) {
  return request<null>({ url: '/picture/delete', method: 'post', data: { id } })
}

// 批量抓取上传
export function uploadPictureByBatchApi(data: { searchText: string; count: number; spaceId?: number }) {
  return request<null>({ url: '/picture/upload/batch', method: 'post', data })
}

// 以图搜图（返回结构以实际后端为准）
export function searchByPictureApi(data: FormData) {
  return request<PictureVO[]>({ url: '/picture/search/by/picture', method: 'post', data })
}

// 按颜色搜索
export function searchByColorApi(params: { picColor: string; spaceId?: number; topN?: number }) {
  return request<PictureVO[]>({ url: '/picture/search/color', method: 'get', params })
}
