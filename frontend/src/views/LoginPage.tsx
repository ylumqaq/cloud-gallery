import { useState } from 'react'
import { Button, Card, Form, Input, message } from 'antd'
import { useNavigate } from 'react-router-dom'
import { useUserStore } from '../stores/user'

// 登录表单字段
interface LoginForm {
  userAccount: string
  userPassword: string
}

// 登录页：账号密码登录
export default function LoginPage() {
  const [loading, setLoading] = useState(false)
  const login = useUserStore((state) => state.login)
  const navigate = useNavigate()

  async function onFinish(values: LoginForm) {
    setLoading(true)
    try {
      await login(values.userAccount, values.userPassword)
      message.success('登录成功')
      navigate('/', { replace: true })
    } catch {
      // 错误提示已由请求拦截器统一处理
    } finally {
      setLoading(false)
    }
  }

  return (
    <div
      style={{
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        height: '100%',
        background: '#f5f5f5',
      }}
    >
      <Card title="云图库登录" style={{ width: 360 }}>
        <Form layout="vertical" onFinish={onFinish}>
          <Form.Item name="userAccount" label="账号" rules={[{ required: true, message: '请输入账号' }]}>
            <Input placeholder="请输入账号" />
          </Form.Item>
          <Form.Item name="userPassword" label="密码" rules={[{ required: true, message: '请输入密码' }]}>
            <Input.Password placeholder="请输入密码" />
          </Form.Item>
          <Form.Item>
            <Button type="primary" htmlType="submit" block loading={loading}>
              登录
            </Button>
          </Form.Item>
        </Form>
      </Card>
    </div>
  )
}
