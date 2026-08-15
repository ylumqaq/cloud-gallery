import { useEffect, useState } from 'react'
import { Button, Card, Col, Empty, Form, Input, Modal, Radio, Row, Space, Spin, Tag, Typography, message } from 'antd'
import { BarChartOutlined, DeleteOutlined, EditOutlined, PlusOutlined, TeamOutlined } from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'
import { useSpaceStore } from '../stores/space'
import { addSpaceApi, deleteSpaceApi, editSpaceApi } from '../api/space'
import type { SpaceType, SpaceVO } from '../types/space'

const { Title, Text } = Typography

// 空间列表页：创建 / 编辑 / 删除空间，并提供进入空间、成员管理、分析的入口
export default function SpaceManagePage() {
  const spaceList = useSpaceStore((s) => s.spaceList)
  const fetchMySpaces = useSpaceStore((s) => s.fetchMySpaces)
  const navigate = useNavigate()

  const [loading, setLoading] = useState(false)
  const [modalOpen, setModalOpen] = useState(false)
  const [editingSpace, setEditingSpace] = useState<SpaceVO | null>(null)
  const [submitting, setSubmitting] = useState(false)
  const [form] = Form.useForm()

  useEffect(() => {
    setLoading(true)
    fetchMySpaces().finally(() => setLoading(false))
  }, [fetchMySpaces])

  // 打开创建弹窗
  function openCreate() {
    setEditingSpace(null)
    form.resetFields()
    form.setFieldsValue({ spaceType: 1 }) // 默认团队空间
    setModalOpen(true)
  }

  // 打开编辑弹窗
  function openEdit(space: SpaceVO) {
    setEditingSpace(space)
    form.setFieldsValue({ spaceName: space.spaceName })
    setModalOpen(true)
  }

  // 提交创建 / 编辑
  async function handleSubmit(values: { spaceName: string; spaceType?: SpaceType }) {
    setSubmitting(true)
    try {
      if (editingSpace) {
        await editSpaceApi({ id: editingSpace.id, spaceName: values.spaceName })
        message.success('编辑成功')
      } else {
        await addSpaceApi({ spaceName: values.spaceName, spaceType: values.spaceType ?? 1 })
        message.success('创建成功')
      }
      setModalOpen(false)
      fetchMySpaces()
    } catch {
      // 错误提示已由请求拦截器统一处理
    } finally {
      setSubmitting(false)
    }
  }

  // 删除空间（二次确认）
  function handleDelete(space: SpaceVO) {
    Modal.confirm({
      title: '删除空间',
      content: `确定删除「${space.spaceName}」吗？空间内图片与成员关系将一并删除。`,
      onOk: async () => {
        await deleteSpaceApi(space.id)
        message.success('删除成功')
        fetchMySpaces()
      },
    })
  }

  return (
    <Space direction="vertical" size="large" style={{ width: '100%' }}>
      <Space style={{ width: '100%', justifyContent: 'space-between' }}>
        <Title level={4} style={{ margin: 0 }}>
          我的空间
        </Title>
        <Button type="primary" icon={<PlusOutlined />} onClick={openCreate}>
          创建空间
        </Button>
      </Space>

      <Spin spinning={loading}>
        {spaceList.length === 0 ? (
          <Empty description="暂无空间，点击右上角创建" />
        ) : (
          <Row gutter={[16, 16]}>
            {spaceList.map((space) => (
              <Col key={space.id} xs={24} sm={12} md={8} lg={6}>
                <Card
                  hoverable
                  title={space.spaceName}
                  extra={
                    <Tag color={space.spaceType === 1 ? 'blue' : 'default'}>
                      {space.spaceType === 1 ? '团队' : '私有'}
                    </Tag>
                  }
                  onClick={() => navigate(`/space/${space.id}`)}
                  actions={[
                    <TeamOutlined
                      key="members"
                      onClick={(e) => {
                        e.stopPropagation()
                        navigate(`/space/${space.id}/members`)
                      }}
                    />,
                    <BarChartOutlined
                      key="analyze"
                      onClick={(e) => {
                        e.stopPropagation()
                        navigate(`/space/${space.id}/analyze`)
                      }}
                    />,
                    <EditOutlined
                      key="edit"
                      onClick={(e) => {
                        e.stopPropagation()
                        openEdit(space)
                      }}
                    />,
                    <DeleteOutlined
                      key="delete"
                      onClick={(e) => {
                        e.stopPropagation()
                        handleDelete(space)
                      }}
                    />,
                  ]}
                >
                  <Text type="secondary">创建时间：{space.createTime ?? '-'}</Text>
                </Card>
              </Col>
            ))}
          </Row>
        )}
      </Spin>

      <Modal
        title={editingSpace ? '编辑空间' : '创建空间'}
        open={modalOpen}
        onOk={form.submit}
        confirmLoading={submitting}
        onCancel={() => setModalOpen(false)}
      >
        <Form form={form} layout="vertical" onFinish={handleSubmit}>
          <Form.Item name="spaceName" label="空间名称" rules={[{ required: true, message: '请输入空间名称' }]}>
            <Input placeholder="请输入空间名称" maxLength={128} />
          </Form.Item>
          {!editingSpace && (
            <Form.Item name="spaceType" label="空间类型" rules={[{ required: true, message: '请选择空间类型' }]}>
              <Radio.Group>
                <Radio value={0}>私有</Radio>
                <Radio value={1}>团队</Radio>
              </Radio.Group>
            </Form.Item>
          )}
        </Form>
      </Modal>
    </Space>
  )
}
