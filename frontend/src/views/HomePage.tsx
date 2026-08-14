import { useEffect } from 'react'
import { Button, Space, Typography } from 'antd'
import { useNavigate } from 'react-router-dom'
import { useUserStore } from '../stores/user'

const { Title, Text } = Typography

// 首页：展示当前登录用户，验证登录态链路
export default function HomePage() {
  const loginUser = useUserStore((state) => state.loginUser)
  const fetchLoginUser = useUserStore((state) => state.fetchLoginUser)
  const logout = useUserStore((state) => state.logout)
  const navigate = useNavigate()

  // 进入页面时拉取当前登录用户信息
  useEffect(() => {
    fetchLoginUser().catch(() => {
      // 错误已由请求拦截器统一处理
    })
  }, [fetchLoginUser])

  async function handleLogout() {
    await logout()
    navigate('/login', { replace: true })
  }

  return (
    <div style={{ padding: 24 }}>
      <Title level={3}>云图库</Title>
      <Space direction="vertical">
        <Text>当前登录用户：{loginUser?.userName ?? loginUser?.userAccount ?? '未知'}</Text>
        <Text type="secondary">角色：{loginUser?.userRole ?? '-'}</Text>
        <Button type="primary" danger onClick={handleLogout}>
          退出登录
        </Button>
      </Space>
    </div>
  )
}
