import { useEffect, useState } from 'react'
import { Button, Card, Descriptions, Empty, Form, Input, Modal, Select, Space, Spin, Tag, message } from 'antd'
import { useNavigate, useParams } from 'react-router-dom'
import { PERMISSION } from '../constants/permission'
import { hasPermission } from '../utils/permission'
import { deletePictureApi, editPictureApi, getPictureApi } from '../api/picture'
import { useSpaceStore } from '../stores/space'
import type { PictureVO } from '../types/picture'

// 将字节大小格式化为 B / KB / MB
function formatSize(size?: number): string {
  if (size == null) return '-'
  if (size < 1024) return `${size} B`
  if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)} KB`
  return `${(size / 1024 / 1024).toFixed(1)} MB`
}

// 编辑图片表单字段
interface EditForm {
  name?: string
  category?: string
  tags?: string[]
  spaceId?: number
}

// 图片详情页：元信息展示 + 编辑弹窗 + 删除
export default function PictureDetailPage() {
  const { id } = useParams()
  const pictureId = Number(id)
  const navigate = useNavigate()
  const spaceList = useSpaceStore((s) => s.spaceList)
  const fetchMySpaces = useSpaceStore((s) => s.fetchMySpaces)

  const [picture, setPicture] = useState<PictureVO | null>(null)
  const [loading, setLoading] = useState(false)
  const [editOpen, setEditOpen] = useState(false)
  const [submitting, setSubmitting] = useState(false)
  const [form] = Form.useForm<EditForm>()

  useEffect(() => {
    setLoading(true)
    getPictureApi(pictureId)
      .then(setPicture)
      .catch(() => {
        // 错误提示已由请求拦截器统一处理
      })
      .finally(() => setLoading(false))
    // 拉取空间列表供编辑弹窗选择目标空间
    fetchMySpaces().catch(() => {})
  }, [pictureId, fetchMySpaces])

  // 打开编辑弹窗并回填表单
  function openEdit() {
    form.setFieldsValue({
      name: picture?.name,
      category: picture?.category,
      tags: picture?.tags,
      spaceId: picture?.spaceId,
    })
    setEditOpen(true)
  }

  // 保存编辑：tags 转 JSON 数组字符串提交
  async function handleEditSubmit(values: EditForm) {
    setSubmitting(true)
    try {
      await editPictureApi({
        id: pictureId,
        name: values.name,
        category: values.category,
        tags: values.tags?.length ? JSON.stringify(values.tags) : undefined,
        spaceId: values.spaceId,
      })
      message.success('保存成功')
      setEditOpen(false)
      // 重新拉取详情
      const updated = await getPictureApi(pictureId)
      setPicture(updated)
    } catch {
      // 错误提示已由请求拦截器统一处理
    } finally {
      setSubmitting(false)
    }
  }

  // 删除图片（二次确认）
  function handleDelete() {
    Modal.confirm({
      title: '删除图片',
      content: `确定删除「${picture?.name}」吗？删除后不可恢复。`,
      onOk: async () => {
        await deletePictureApi(pictureId)
        message.success('删除成功')
        navigate(-1)
      },
    })
  }

  if (loading) {
    return <Spin style={{ display: 'block', margin: '40px auto' }} />
  }

  if (!picture) {
    return <Empty description="图片不存在" />
  }

  return (
    <Space direction="vertical" size="large" style={{ width: '100%' }}>
      <Space>
        {hasPermission(PERMISSION.PICTURE_EDIT) && (
          <Button type="primary" onClick={openEdit}>
            编辑
          </Button>
        )}
        {hasPermission(PERMISSION.PICTURE_DELETE) && (
          <Button danger onClick={handleDelete}>
            删除
          </Button>
        )}
        <Button onClick={() => navigate(-1)}>返回</Button>
      </Space>

      <Card>
        <img
          src={picture.url}
          alt={picture.name}
          style={{ maxWidth: '100%', display: 'block', marginBottom: 16 }}
        />
        <Descriptions bordered column={2}>
          <Descriptions.Item label="名称">{picture.name}</Descriptions.Item>
          <Descriptions.Item label="分类">{picture.category || '-'}</Descriptions.Item>
          <Descriptions.Item label="大小">{formatSize(picture.picSize)}</Descriptions.Item>
          <Descriptions.Item label="尺寸">
            {picture.picWidth && picture.picHeight ? `${picture.picWidth} × ${picture.picHeight}` : '-'}
          </Descriptions.Item>
          <Descriptions.Item label="格式">{picture.picFormat || '-'}</Descriptions.Item>
          <Descriptions.Item label="主色调">
            {picture.picColor ? (
              <span style={{ display: 'inline-flex', alignItems: 'center', gap: 8 }}>
                <span
                  style={{
                    width: 16,
                    height: 16,
                    borderRadius: 2,
                    background: `#${picture.picColor.replace(/^0x/i, '')}`,
                    display: 'inline-block',
                  }}
                />
                {picture.picColor}
              </span>
            ) : (
              '-'
            )}
          </Descriptions.Item>
          <Descriptions.Item label="标签" span={2}>
            {picture.tags?.length ? picture.tags.map((t) => <Tag key={t} color="blue">{t}</Tag>) : '-'}
          </Descriptions.Item>
          <Descriptions.Item label="上传者">{picture.userId ?? '-'}</Descriptions.Item>
          <Descriptions.Item label="创建时间">{picture.createTime ?? '-'}</Descriptions.Item>
          <Descriptions.Item label="最近编辑" span={2}>
            {picture.editTime ?? '-'}
          </Descriptions.Item>
        </Descriptions>
      </Card>

      <Modal
        title="编辑图片"
        open={editOpen}
        onOk={form.submit}
        confirmLoading={submitting}
        onCancel={() => setEditOpen(false)}
      >
        <Form form={form} layout="vertical" onFinish={handleEditSubmit}>
          <Form.Item name="name" label="名称">
            <Input placeholder="请输入图片名称" />
          </Form.Item>
          <Form.Item name="category" label="分类">
            <Input placeholder="请输入分类" />
          </Form.Item>
          <Form.Item name="tags" label="标签">
            <Select mode="tags" placeholder="输入标签后回车" />
          </Form.Item>
          <Form.Item name="spaceId" label="目标空间">
            <Select
              allowClear
              placeholder="选择目标空间（清空表示公共图库）"
              options={spaceList.map((s) => ({ value: s.id, label: s.spaceName }))}
            />
          </Form.Item>
        </Form>
      </Modal>
    </Space>
  )
}
