import { request } from './request'
import type {
  SpaceCategoryAnalyzeVO,
  SpaceRankAnalyzeVO,
  SpaceSizeAnalyzeVO,
  SpaceTagAnalyzeVO,
  SpaceUsageAnalyzeVO,
} from '../types/spaceAnalyze'

// 空间使用分析（spaceId 为空 = 公共图库）
export function getUsageAnalyzeApi(spaceId?: number) {
  return request<SpaceUsageAnalyzeVO>({ url: '/space/analyze/usage', method: 'get', params: { spaceId } })
}

// 分类分析
export function getCategoryAnalyzeApi(spaceId?: number) {
  return request<SpaceCategoryAnalyzeVO[]>({ url: '/space/analyze/category', method: 'get', params: { spaceId } })
}

// 标签分析
export function getTagAnalyzeApi(spaceId?: number) {
  return request<SpaceTagAnalyzeVO[]>({ url: '/space/analyze/tag', method: 'get', params: { spaceId } })
}

// 大小分析
export function getSizeAnalyzeApi(spaceId?: number) {
  return request<SpaceSizeAnalyzeVO[]>({ url: '/space/analyze/size', method: 'get', params: { spaceId } })
}

// 空间用量排行（系统管理员）
export function getSpaceRankApi(topN = 10) {
  return request<SpaceRankAnalyzeVO[]>({ url: '/space/analyze/rank', method: 'get', params: { topN } })
}
