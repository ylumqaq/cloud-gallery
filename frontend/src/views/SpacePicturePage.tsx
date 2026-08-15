import { Button, Result, Space, Spin, Typography } from 'antd'
import { useNavigate, useParams } from 'react-router-dom'
import PictureListPage from './PictureListPage'
import { useSpaceContext } from '../hooks/useSpaceContext'
import { PERMISSION } from '../constants/permission'

const { Title } = Typography

// 空间图片页：复用图片列表，进入时同步空间上下文与权限
export default function SpacePicturePage() {
  const { spaceId } = useParams()
  const id = Number(spaceId)
  const navigate = useNavigate()
  const { space, loading, denied } = useSpaceContext(id, PERMISSION.PICTURE_VIEW)

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
          {space?.spaceName ?? '空间'}
        </Title>
        <Space>
          <Button onClick={() => navigate(`/space/${id}/members`)}>成员管理</Button>
          <Button onClick={() => navigate(`/space/${id}/analyze`)}>空间分析</Button>
          <Button onClick={() => navigate('/space/manage')}>返回空间列表</Button>
        </Space>
      </Space>

      <PictureListPage />
    </Space>
  )
}
