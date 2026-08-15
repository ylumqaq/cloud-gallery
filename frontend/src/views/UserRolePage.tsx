import { useState } from 'react'
import { Button, Card, Form, InputNumber, Modal, Select, Space, Typography, message } from 'antd'
import { updateUserRoleApi } from '../api/user'
import { SYSTEM_ROLE } from '../constants/permission'
import type { SystemRole } from '../types/user'

const { Title } = Typography

// 系统角色中文映射
const ROLE_LABEL: Record<SystemRole, string> = {
  user: '普通用户',
  admin: '管理员',
  super_admin: '超级管理员',
}

// 用户角色管理页（仅 super_admin）
export default function UserRolePage() {
  const [submitting, setSubmitting] = useState(false)
  const [form] = Form.useForm<{ targetUserId: number; userRole: SystemRole }>()

  function handleSubmit(values: { targetUserId: number; userRole: SystemRole }) {
    // 修改角色为敏感操作，二次确认
    Modal.confirm({
      title: '修改用户角色',
      content: `确定将用户 ${values.targetUserId} 的角色修改为「${ROLE_LABEL[values.userRole]}」吗？`,
      onOk: async () => {
        setSubmitting(true)
        try {
          await updateUserRoleApi({ targetUserId: values.targetUserId, userRole: values.userRole })
          message.success('修改成功')
          form.resetFields()
        } catch {
          // 错误提示已由请求拦截器统一处理
        } finally {
          setSubmitting(false)
        }
      },
    })
  }

  return (
    <Space direction="vertical" size="large" style={{ width: '100%', maxWidth: 480 }}>
      <Title level={4}>用户角色管理</Title>
      <Card>
        <Form form={form} layout="vertical" onFinish={handleSubmit}>
          <Form.Item name="targetUserId" label="目标用户 ID" rules={[{ required: true, message: '请输入用户 ID' }]}>
            <InputNumber placeholder="请输入要修改的用户 ID" style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="userRole" label="角色" rules={[{ required: true, message: '请选择角色' }]}>
            <Select
              placeholder="请选择目标角色"
              options={[
                { value: SYSTEM_ROLE.USER, label: ROLE_LABEL.user },
                { value: SYSTEM_ROLE.ADMIN, label: ROLE_LABEL.admin },
                { value: SYSTEM_ROLE.SUPER_ADMIN, label: ROLE_LABEL.super_admin },
              ]}
            />
          </Form.Item>
          <Form.Item>
            <Button type="primary" htmlType="submit" loading={submitting}>
              提交
            </Button>
          </Form.Item>
        </Form>
      </Card>
    </Space>
  )
}
