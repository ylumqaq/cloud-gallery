import { useEffect, useState } from 'react'
import { Card, InputNumber, Space, Table, Typography } from 'antd'
import type { TableColumnsType } from 'antd'
import { getSpaceRankApi } from '../api/spaceAnalyze'
import type { SpaceRankAnalyzeVO } from '../types/spaceAnalyze'

const { Title } = Typography

// 字节大小格式化
function formatSize(size?: number): string {
  if (size == null) return '-'
  if (size < 1024) return `${size} B`
  if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)} KB`
  if (size < 1024 * 1024 * 1024) return `${(size / 1024 / 1024).toFixed(1)} MB`
  return `${(size / 1024 / 1024 / 1024).toFixed(1)} GB`
}

// 空间用量排行页（系统管理员）
export default function SpaceRankPage() {
  const [rankList, setRankList] = useState<SpaceRankAnalyzeVO[]>([])
  const [topN, setTopN] = useState(10)
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    setLoading(true)
    getSpaceRankApi(topN)
      .then(setRankList)
      .catch(() => {
        // 错误提示已由请求拦截器统一处理
      })
      .finally(() => setLoading(false))
  }, [topN])

  const columns: TableColumnsType<SpaceRankAnalyzeVO> = [
    { title: '排名', render: (_: unknown, _record: SpaceRankAnalyzeVO, index: number) => index + 1 },
    { title: '空间 ID', dataIndex: 'spaceId' },
    { title: '图片数量', dataIndex: 'count' },
    { title: '总大小', dataIndex: 'totalSize', render: (v: number) => formatSize(v) },
  ]

  return (
    <Space direction="vertical" size="large" style={{ width: '100%' }}>
      <Space style={{ width: '100%', justifyContent: 'space-between' }}>
        <Title level={4} style={{ margin: 0 }}>
          空间用量排行
        </Title>
        <Space>
          <span>TopN：</span>
          <InputNumber min={1} max={100} value={topN} onChange={(v) => setTopN(v ?? 10)} />
        </Space>
      </Space>

      <Card>
        <Table rowKey="spaceId" loading={loading} columns={columns} dataSource={rankList} pagination={false} />
      </Card>
    </Space>
  )
}
