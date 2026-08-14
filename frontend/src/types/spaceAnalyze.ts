// 空间使用分析
export interface SpaceUsageAnalyzeVO {
  usedCount: number
  usedSize: number
  maxCount: number
  maxSize: number
  countUsageRatio: number
  sizeUsageRatio: number
}

// 分类分析
export interface SpaceCategoryAnalyzeVO {
  category: string
  count: number
  totalSize: number
}

// 标签分析
export interface SpaceTagAnalyzeVO {
  tag: string
  count: number
  totalSize: number
}

// 大小分析
export interface SpaceSizeAnalyzeVO {
  sizeRange: string // <100KB / 100KB-500KB / 500KB-1MB / >1MB / 未知
  count: number
}

// 空间用量排行
export interface SpaceRankAnalyzeVO {
  spaceId: number
  count: number
  totalSize: number
}
