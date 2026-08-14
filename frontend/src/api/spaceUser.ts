import { request } from './request'
import type {
  SpaceUserAddRequest,
  SpaceUserEditRequest,
  SpaceUserPermissionVO,
  SpaceUserVO,
} from '../types/spaceUser'

// 添加空间成员
export function addSpaceUserApi(data: SpaceUserAddRequest) {
  return request<null>({ url: '/spaceUser/add', method: 'post', data })
}

// 修改空间成员角色
export function editSpaceUserApi(data: SpaceUserEditRequest) {
  return request<null>({ url: '/spaceUser/edit', method: 'post', data })
}

// 移除空间成员
export function deleteSpaceUserApi(data: { spaceId: number; userId: number }) {
  return request<null>({ url: '/spaceUser/delete', method: 'post', data })
}

// 空间成员列表
export function listSpaceUserApi(spaceId: number) {
  return request<SpaceUserVO[]>({ url: '/spaceUser/list', method: 'get', params: { spaceId } })
}

// 获取当前用户在某空间的权限
export function getMySpacePermissionApi(spaceId: number) {
  return request<SpaceUserPermissionVO>({ url: '/spaceUser/get', method: 'get', params: { spaceId } })
}
