import { useState } from 'react'
import { Button, Card, Form, Input, message } from 'antd'
import { useNavigate } from 'react-router-dom'
import { registerApi } from '../api/user'

// 注册表单字段
interface RegisterForm {
  userAccount: string
  userPassword: string
  checkPassword: string
}

// 注册页：账号密码注册
export default function RegisterPage() {
  const [loading, setLoading] = useState(false)
  const navigate = useNavigate()

  async function onFinish(values: RegisterForm) {
    setLoading(true)
    try {
      await registerApi(values)
      message.success('注册成功，请登录')
      navigate('/login', { replace: true })
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
      <Card title="云图库注册" style={{ width: 360 }}>
        <Form layout="vertical" onFinish={onFinish}>
          <Form.Item name="userAccount" label="账号" rules={[{ required: true, message: '请输入账号' }]}>
            <Input placeholder="请输入账号" />
          </Form.Item>
          <Form.Item name="userPassword" label="密码" rules={[{ required: true, message: '请输入密码' }]}>
            <Input.Password placeholder="请输入密码" />
          </Form.Item>
          <Form.Item
            name="checkPassword"
            label="确认密码"
            dependencies={['userPassword']}
            rules={[
              { required: true, message: '请再次输入密码' },
              ({ getFieldValue }) => ({
                validator(_, value) {
                  if (!value || getFieldValue('userPassword') === value) {
                    return Promise.resolve()
                  }
                  return Promise.reject(new Error('两次输入的密码不一致'))
                },
              }),
            ]}
          >
            <Input.Password placeholder="请再次输入密码" />
          </Form.Item>
          <Form.Item>
            <Button type="primary" htmlType="submit" block loading={loading}>
              注册
            </Button>
          </Form.Item>
          <div style={{ textAlign: 'center' }}>
            <a onClick={() => navigate('/login')}>已有账号？去登录</a>
          </div>
        </Form>
      </Card>
    </div>
  )
}
