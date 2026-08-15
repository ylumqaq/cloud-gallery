import { useEffect, useState } from 'react'
import { Button, Card, Col, Empty, Input, InputNumber, Radio, Row, Select, Space, Spin, Typography, Upload, message } from 'antd'
import { InboxOutlined } from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'
import PictureCard from '../components/PictureCard'
import { searchByPictureApi } from '../api/picture'
import { useSpaceStore } from '../stores/space'
import type { PictureVO } from '../types/picture'

const { Title } = Typography

// 允许的查询图 MIME 类型
const ALLOWED_TYPES = ['image/jpeg', 'image/png', 'image/webp']
// 最大查询图大小（字节）：2MB
const MAX_FILE_SIZE = 2 * 1024 * 1024

// 以图搜图页：上传查询图检索相似图片
export default function PictureSearchPage() {
  const spaceList = useSpaceStore((s) => s.spaceList)
  const fetchMySpaces = useSpaceStore((s) => s.fetchMySpaces)
  const navigate = useNavigate()

  const [mode, setMode] = useState<'file' | 'url'>('file')
  const [file, setFile] = useState<File | null>(null)
  const [fileUrl, setFileUrl] = useState('')
  const [spaceId, setSpaceId] = useState<number>()
  const [topK, setTopK] = useState(20)
  const [resultList, setResultList] = useState<PictureVO[]>([])
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    fetchMySpaces().catch(() => {})
  }, [fetchMySpaces])

  // 搜索相似图片
  async function handleSearch() {
    if (mode === 'file' && !file) {
      message.warning('请选择查询图片')
      return
    }
    if (mode === 'url' && !fileUrl.trim()) {
      message.warning('请输入图片 URL')
      return
    }

    const formData = new FormData()
    if (mode === 'file' && file) {
      formData.append('file', file)
    } else {
      formData.append('fileUrl', fileUrl.trim())
    }
    if (spaceId != null) formData.append('spaceId', String(spaceId))
    formData.append('topK', String(topK))

    setLoading(true)
    try {
      const list = await searchByPictureApi(formData)
      setResultList(list)
    } catch {
      // 错误提示已由请求拦截器统一处理
    } finally {
      setLoading(false)
    }
  }

  return (
    <Space direction="vertical" size="large" style={{ width: '100%' }}>
      <Title level={4}>以图搜图</Title>

      <Card title="查询条件">
        <Space direction="vertical" size="large" style={{ width: '100%' }}>
          <Radio.Group value={mode} onChange={(e) => setMode(e.target.value)}>
            <Radio.Button value="file">本地文件</Radio.Button>
            <Radio.Button value="url">网络 URL</Radio.Button>
          </Radio.Group>

          {mode === 'file' ? (
            <Upload.Dragger
              accept={ALLOWED_TYPES.join(',')}
              maxCount={1}
              beforeUpload={(f) => {
                if (!ALLOWED_TYPES.includes(f.type)) {
                  message.error('仅支持 jpeg / png / webp 格式图片')
                  return Upload.LIST_IGNORE
                }
                if (f.size > MAX_FILE_SIZE) {
                  message.error('图片大小不能超过 2MB')
                  return Upload.LIST_IGNORE
                }
                setFile(f)
                return false // 阻止自动上传，由搜索按钮统一提交
              }}
              onRemove={() => setFile(null)}
            >
              <p className="ant-upload-drag-icon">
                <InboxOutlined />
              </p>
              <p className="ant-upload-text">点击或拖拽查询图片到此区域</p>
              <p className="ant-upload-hint">支持 jpeg / png / webp，大小不超过 2MB</p>
            </Upload.Dragger>
          ) : (
            <Input placeholder="请输入查询图片 URL" value={fileUrl} onChange={(e) => setFileUrl(e.target.value)} />
          )}

          <Space wrap>
            <span>限定空间：</span>
            <Select
              allowClear
              placeholder="不限定"
              style={{ width: 200 }}
              value={spaceId}
              onChange={setSpaceId}
              options={spaceList.map((s) => ({ value: s.id, label: s.spaceName }))}
            />
            <span>返回数量：</span>
            <InputNumber min={1} max={100} value={topK} onChange={(v) => setTopK(v ?? 20)} />
            <Button type="primary" loading={loading} onClick={handleSearch}>
              搜索
            </Button>
          </Space>
        </Space>
      </Card>

      <Spin spinning={loading}>
        {resultList.length === 0 ? (
          <Empty description="暂无搜索结果" />
        ) : (
          <Row gutter={[16, 16]}>
            {resultList.map((picture) => (
              <Col key={picture.id} xs={24} sm={12} md={8} lg={6}>
                <PictureCard picture={picture} onPreview={(p) => navigate(`/picture/${p.id}`)} />
              </Col>
            ))}
          </Row>
        )}
      </Spin>
    </Space>
  )
}
