import { request } from './request'
import type { SpaceAddRequest, SpaceEditRequest, SpaceVO } from '../types/space'

// 创建空间
export function addSpaceApi(data: SpaceAddRequest) {
  return request<SpaceVO>({ url: '/space/add', method: 'post', data })
}

// 编辑空间
export function editSpaceApi(data: SpaceEditRequest) {
  return request<null>({ url: '/space/edit', method: 'post', data })
}

// 删除空间
export function deleteSpaceApi(id: number) {
  return request<null>({ url: '/space/delete', method: 'post', data: { id } })
}

// 空间详情
export function getSpaceApi(id: number) {
  return request<SpaceVO>({ url: `/space/get/${id}`, method: 'get' })
}

// 我的空间列表
export function listMySpaceApi() {
  return request<SpaceVO[]>({ url: '/space/list', method: 'get' })
}
