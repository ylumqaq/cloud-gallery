import { useEffect, useState } from 'react'
import { Card, Form, Input, Select, Space, Typography } from 'antd'
import { useNavigate } from 'react-router-dom'
import PictureUploader from '../components/PictureUploader'
import { useSpaceStore } from '../stores/space'
import type { PictureVO } from '../types/picture'

const { Title } = Typography

// 上传图片页：目标空间选择 + 图片信息表单 + 上传器
export default function PictureUploadPage() {
  const spaceList = useSpaceStore((s) => s.spaceList)
  const fetchMySpaces = useSpaceStore((s) => s.fetchMySpaces)
  const navigate = useNavigate()

  const [spaceId, setSpaceId] = useState<number>()
  const [picName, setPicName] = useState<string>()
  const [category, setCategory] = useState<string>()
  const [tags, setTags] = useState<string[]>()

  useEffect(() => {
    fetchMySpaces().catch(() => {})
  }, [fetchMySpaces])

  // 上传成功：跳转图片列表
  function handleSuccess(_picture: PictureVO) {
    navigate('/')
  }

  return (
    <Space direction="vertical" size="large" style={{ width: '100%', maxWidth: 720 }}>
      <Title level={4}>上传图片</Title>

      <Card title="图片信息">
        <Form layout="vertical">
          <Form.Item label="目标空间">
            <Select
              allowClear
              placeholder="选择目标空间（清空表示公共图库）"
              value={spaceId}
              onChange={setSpaceId}
              options={spaceList.map((s) => ({ value: s.id, label: s.spaceName }))}
            />
          </Form.Item>
          <Form.Item label="图片名称">
            <Input
              placeholder="可选，不填则用原始文件名"
              value={picName}
              onChange={(e) => setPicName(e.target.value)}
            />
          </Form.Item>
          <Form.Item label="分类">
            <Input placeholder="可选" value={category} onChange={(e) => setCategory(e.target.value)} />
          </Form.Item>
          <Form.Item label="标签">
            <Select mode="tags" placeholder="可选，输入后回车" value={tags} onChange={setTags} />
          </Form.Item>
        </Form>
      </Card>

      <Card title="上传">
        <PictureUploader
          spaceId={spaceId}
          picName={picName}
          category={category}
          tags={tags}
          onSuccess={handleSuccess}
        />
      </Card>
    </Space>
  )
}
