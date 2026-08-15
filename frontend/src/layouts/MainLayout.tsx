import { useEffect, useMemo } from 'react'
import { Layout, Menu, Dropdown, Avatar, Space, Typography } from 'antd'
import { UserOutlined, LogoutOutlined } from '@ant-design/icons'
import { Outlet, useLocation, useNavigate } from 'react-router-dom'
import { useUserStore } from '../stores/user'
import { SYSTEM_ROLE } from '../constants/permission'
import type { SystemRole } from '../types/user'

const { Sider, Header, Content } = Layout
const { Text } = Typography

// 菜单项：requiredRole 为空表示所有登录用户可见
interface MenuItem {
  key: string
  label: string
  requiredRole?: SystemRole[]
}

// 侧边栏菜单配置
const MENU_ITEMS: MenuItem[] = [
  { key: '/', label: '图片列表' },
  { key: '/picture/upload', label: '上传图片' },
  { key: '/picture/search', label: '以图搜图' },
  { key: '/space/manage', label: '我的空间' },
  { key: '/admin/rank', label: '空间用量排行', requiredRole: [SYSTEM_ROLE.ADMIN, SYSTEM_ROLE.SUPER_ADMIN] },
  { key: '/admin/user', label: '用户角色管理', requiredRole: [SYSTEM_ROLE.SUPER_ADMIN] },
]

// 后台主布局：侧边栏导航 + 顶栏用户信息 + 内容区（Outlet）
export default function MainLayout() {
  const loginUser = useUserStore((s) => s.loginUser)
  const fetchLoginUser = useUserStore((s) => s.fetchLoginUser)
  const logout = useUserStore((s) => s.logout)
  const navigate = useNavigate()
  const location = useLocation()

  // 进入后台时确保登录用户信息已加载（刷新后 token 存在但 loginUser 为空）
  useEffect(() => {
    if (!loginUser) {
      fetchLoginUser().catch(() => {
        // 错误已由请求拦截器统一处理
      })
    }
  }, [loginUser, fetchLoginUser])

  // 按当前用户系统角色过滤可见菜单
  const visibleMenuItems = useMemo(() => {
    const role = loginUser?.userRole
    return MENU_ITEMS.filter((item) => {
      if (!item.requiredRole) return true
      return role != null && item.requiredRole.includes(role)
    })
  }, [loginUser])

  // 退出登录
  async function handleLogout() {
    await logout()
    navigate('/login', { replace: true })
  }

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider theme="dark" width={200}>
        <div style={{ color: '#fff', padding: 16, fontSize: 16, fontWeight: 600 }}>云图库</div>
        <Menu
          theme="dark"
          mode="inline"
          selectedKeys={[location.pathname]}
          items={visibleMenuItems.map((item) => ({ key: item.key, label: item.label }))}
          onClick={({ key }) => navigate(key)}
        />
      </Sider>
      <Layout>
        <Header
          style={{
            background: '#fff',
            padding: '0 16px',
            display: 'flex',
            justifyContent: 'flex-end',
            alignItems: 'center',
          }}
        >
          <Dropdown
            menu={{
              items: [
                { key: 'logout', icon: <LogoutOutlined />, label: '退出登录', onClick: handleLogout },
              ],
            }}
          >
            <Space style={{ cursor: 'pointer' }}>
              <Avatar size="small" icon={<UserOutlined />} src={loginUser?.userAvatar} />
              <Text>{loginUser?.userName ?? loginUser?.userAccount ?? '未知用户'}</Text>
            </Space>
          </Dropdown>
        </Header>
        <Content style={{ margin: 16 }}>
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  )
}
