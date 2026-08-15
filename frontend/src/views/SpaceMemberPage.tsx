import { useEffect, useState } from 'react'
import { Button, Form, InputNumber, Modal, Result, Select, Space, Spin, Table, Tag, Typography, message } from 'antd'
import type { TableColumnsType } from 'antd'
import { PlusOutlined } from '@ant-design/icons'
import { useNavigate, useParams } from 'react-router-dom'
import { useSpaceContext } from '../hooks/useSpaceContext'
import { PERMISSION } from '../constants/permission'
import { addSpaceUserApi, deleteSpaceUserApi, editSpaceUserApi, listSpaceUserApi } from '../api/spaceUser'
import type { SpaceRole, SpaceUserVO } from '../types/spaceUser'

const { Title } = Typography

// 空间角色中文映射
const ROLE_LABEL: Record<SpaceRole, string> = {
  viewer: '浏览者',
  editor: '编辑者',
  admin: '管理员',
}

// 空间成员页：成员增删改角色（仅 spaceUser:manage 权限）
export default function SpaceMemberPage() {
  const { spaceId } = useParams()
  const id = Number(spaceId)
  const navigate = useNavigate()
  const { space, loading, denied } = useSpaceContext(id, PERMISSION.SPACE_USER_MANAGE)

  const [memberList, setMemberList] = useState<SpaceUserVO[]>([])
  const [listLoading, setListLoading] = useState(false)
  const [modalOpen, setModalOpen] = useState(false)
  const [editingMember, setEditingMember] = useState<SpaceUserVO | null>(null)
  const [submitting, setSubmitting] = useState(false)
  const [form] = Form.useForm()

  // 拉取成员列表
  async function fetchMembers() {
    setListLoading(true)
    try {
      const list = await listSpaceUserApi(id)
      setMemberList(list)
    } catch {
      // 错误提示已由请求拦截器统一处理
    } finally {
      setListLoading(false)
    }
  }

  useEffect(() => {
    if (!denied && space) {
      fetchMembers()
    }
  }, [denied, space])

  // 打开添加成员弹窗
  function openAdd() {
    setEditingMember(null)
    form.resetFields()
    form.setFieldsValue({ spaceRole: 'viewer' })
    setModalOpen(true)
  }

  // 打开修改角色弹窗
  function openEdit(member: SpaceUserVO) {
    setEditingMember(member)
    form.setFieldsValue({ spaceRole: member.spaceRole })
    setModalOpen(true)
  }

  // 提交添加 / 修改角色
  async function handleSubmit(values: { userId?: number; spaceRole: SpaceRole }) {
    const userId = editingMember ? editingMember.userId : values.userId
    if (userId == null) return
    setSubmitting(true)
    try {
      if (editingMember) {
        await editSpaceUserApi({ spaceId: id, userId, spaceRole: values.spaceRole })
        message.success('修改成功')
      } else {
        await addSpaceUserApi({ spaceId: id, userId, spaceRole: values.spaceRole })
        message.success('添加成功')
      }
      setModalOpen(false)
      fetchMembers()
    } catch {
      // 错误提示已由请求拦截器统一处理
    } finally {
      setSubmitting(false)
    }
  }

  // 移除成员（二次确认）
  function handleRemove(member: SpaceUserVO) {
    Modal.confirm({
      title: '移除成员',
      content: `确定移除成员「${member.userName ?? member.userAccount ?? member.userId}」吗？`,
      onOk: async () => {
        await deleteSpaceUserApi({ spaceId: id, userId: member.userId })
        message.success('移除成功')
        fetchMembers()
      },
    })
  }

  if (loading) {
    return <Spin style={{ display: 'block', margin: '40px auto' }} />
  }

  if (denied) {
    return (
      <Result
        status="403"
        title="无权访问"
        subTitle="您没有管理该空间成员的权限"
        extra={
          <Button type="primary" onClick={() => navigate('/')}>
            返回首页
          </Button>
        }
      />
    )
  }

  const columns: TableColumnsType<SpaceUserVO> = [
    { title: '账号', dataIndex: 'userAccount' },
    { title: '昵称', dataIndex: 'userName' },
    {
      title: '角色',
      dataIndex: 'spaceRole',
      render: (role: SpaceRole) => <Tag color="blue">{ROLE_LABEL[role]}</Tag>,
    },
    { title: '加入时间', dataIndex: 'createTime' },
    {
      title: '操作',
      render: (_: unknown, member: SpaceUserVO) => (
        <Space>
          <Button size="small" onClick={() => openEdit(member)}>
            修改角色
          </Button>
          {member.userId !== space?.userId && (
            <Button size="small" danger onClick={() => handleRemove(member)}>
              移除
            </Button>
          )}
        </Space>
      ),
    },
  ]

  return (
    <Space direction="vertical" size="large" style={{ width: '100%' }}>
      <Space style={{ width: '100%', justifyContent: 'space-between' }}>
        <Title level={4} style={{ margin: 0 }}>
          {space?.spaceName ?? '空间'} - 成员管理
        </Title>
        <Space>
          <Button type="primary" icon={<PlusOutlined />} onClick={openAdd}>
            添加成员
          </Button>
          <Button onClick={() => navigate(`/space/${id}`)}>返回空间</Button>
        </Space>
      </Space>

      <Table rowKey="id" loading={listLoading} columns={columns} dataSource={memberList} pagination={false} />

      <Modal
        title={editingMember ? '修改角色' : '添加成员'}
        open={modalOpen}
        onOk={form.submit}
        confirmLoading={submitting}
        onCancel={() => setModalOpen(false)}
      >
        <Form form={form} layout="vertical" onFinish={handleSubmit}>
          {!editingMember && (
            <Form.Item name="userId" label="用户 ID" rules={[{ required: true, message: '请输入用户 ID' }]}>
              <InputNumber placeholder="请输入要添加的用户 ID" style={{ width: '100%' }} />
            </Form.Item>
          )}
          <Form.Item name="spaceRole" label="角色" rules={[{ required: true, message: '请选择角色' }]}>
            <Select
              options={[
                { value: 'viewer', label: '浏览者' },
                { value: 'editor', label: '编辑者' },
                { value: 'admin', label: '管理员' },
              ]}
            />
          </Form.Item>
        </Form>
      </Modal>
    </Space>
  )
}
