import { Card, Progress, Space, Typography } from 'antd'
import type { SpaceUsageAnalyzeVO } from '../../types/spaceAnalyze'

const { Text } = Typography

// 字节大小格式化
function formatSize(size?: number): string {
  if (size == null) return '-'
  if (size < 1024) return `${size} B`
  if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)} KB`
  if (size < 1024 * 1024 * 1024) return `${(size / 1024 / 1024).toFixed(1)} MB`
  return `${(size / 1024 / 1024 / 1024).toFixed(1)} GB`
}

// 空间使用分析卡片：数量与大小使用率进度条
export default function UsageAnalyzeCard({ usage }: { usage: SpaceUsageAnalyzeVO }) {
  return (
    <Card title="空间使用分析">
      <Space direction="vertical" style={{ width: '100%' }}>
        <Text>
          图片数量：{usage.usedCount} / {usage.maxCount}
        </Text>
        <Progress percent={Math.round(usage.countUsageRatio ?? 0)} />
        <Text>
          存储大小：{formatSize(usage.usedSize)} / {formatSize(usage.maxSize)}
        </Text>
        <Progress percent={Math.round(usage.sizeUsageRatio ?? 0)} />
      </Space>
    </Card>
  )
}
