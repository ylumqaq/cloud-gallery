import { useEffect, useState } from 'react'
import { Button, Result, Space, Spin, Typography } from 'antd'
import { useNavigate, useParams } from 'react-router-dom'
import { useSpaceContext } from '../hooks/useSpaceContext'
import { PERMISSION } from '../constants/permission'
import { getCategoryAnalyzeApi, getSizeAnalyzeApi, getTagAnalyzeApi, getUsageAnalyzeApi } from '../api/spaceAnalyze'
import UsageAnalyzeCard from '../components/charts/UsageAnalyzeCard'
import CategoryAnalyzeChart from '../components/charts/CategoryAnalyzeChart'
import TagAnalyzeChart from '../components/charts/TagAnalyzeChart'
import SizeAnalyzeChart from '../components/charts/SizeAnalyzeChart'
import type {
  SpaceCategoryAnalyzeVO,
  SpaceSizeAnalyzeVO,
  SpaceTagAnalyzeVO,
  SpaceUsageAnalyzeVO,
} from '../types/spaceAnalyze'

const { Title } = Typography

// 空间分析页：使用 / 分类 / 标签 / 大小四类分析可视化
export default function SpaceAnalyzePage() {
  const { spaceId } = useParams()
  const id = Number(spaceId)
  const navigate = useNavigate()
  const { space, loading, denied } = useSpaceContext(id, PERMISSION.PICTURE_VIEW)

  const [usage, setUsage] = useState<SpaceUsageAnalyzeVO | null>(null)
  const [categoryList, setCategoryList] = useState<SpaceCategoryAnalyzeVO[]>([])
  const [tagList, setTagList] = useState<SpaceTagAnalyzeVO[]>([])
  const [sizeList, setSizeList] = useState<SpaceSizeAnalyzeVO[]>([])
  const [chartLoading, setChartLoading] = useState(false)

  useEffect(() => {
    if (denied || !space) return
    setChartLoading(true)
    // 四类分析并行拉取
    Promise.all([getUsageAnalyzeApi(id), getCategoryAnalyzeApi(id), getTagAnalyzeApi(id), getSizeAnalyzeApi(id)])
      .then(([u, c, t, s]) => {
        setUsage(u)
        setCategoryList(c)
        setTagList(t)
        setSizeList(s)
      })
      .catch(() => {
        // 错误提示已由请求拦截器统一处理
      })
      .finally(() => setChartLoading(false))
  }, [id, denied, space])

  if (loading) {
    return <Spin style={{ display: 'block', margin: '40px auto' }} />
  }

  if (denied) {
    return (
      <Result
        status="403"
        title="无权访问"
        subTitle="您没有查看该空间的权限"
        extra={
          <Button type="primary" onClick={() => navigate('/')}>
            返回首页
          </Button>
        }
      />
    )
  }

  return (
    <Space direction="vertical" size="large" style={{ width: '100%' }}>
      <Space style={{ width: '100%', justifyContent: 'space-between' }}>
        <Title level={4} style={{ margin: 0 }}>
          {space?.spaceName ?? '空间'} - 空间分析
        </Title>
        <Button onClick={() => navigate(`/space/${id}`)}>返回空间</Button>
      </Space>

      <Spin spinning={chartLoading}>
        {usage && <UsageAnalyzeCard usage={usage} />}
        <CategoryAnalyzeChart data={categoryList} />
        <TagAnalyzeChart data={tagList} />
        <SizeAnalyzeChart data={sizeList} />
      </Spin>
    </Space>
  )
}
