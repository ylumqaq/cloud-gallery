import { createBrowserRouter, Navigate } from 'react-router-dom'
import RequireAuth from './RequireAuth'
import LoginPage from '../views/LoginPage'
import RegisterPage from '../views/RegisterPage'
import PictureListPage from '../views/PictureListPage'
import PictureDetailPage from '../views/PictureDetailPage'
import PictureUploadPage from '../views/PictureUploadPage'
import SpaceManagePage from '../views/SpaceManagePage'
import SpacePicturePage from '../views/SpacePicturePage'
import SpaceMemberPage from '../views/SpaceMemberPage'
import SpaceAnalyzePage from '../views/SpaceAnalyzePage'
import PictureSearchPage from '../views/PictureSearchPage'
import SpaceRankPage from '../views/SpaceRankPage'
import UserRolePage from '../views/UserRolePage'
import MainLayout from '../layouts/MainLayout'

// 路由表：受保护路由用 RequireAuth 包裹，登录后页面统一套 MainLayout（Outlet 渲染子路由）
const router = createBrowserRouter([
  { path: '/login', element: <LoginPage /> },
  { path: '/register', element: <RegisterPage /> },
  {
    path: '/',
    element: (
      <RequireAuth>
        <MainLayout />
      </RequireAuth>
    ),
    children: [
      { index: true, element: <PictureListPage /> },
      { path: 'picture/:id', element: <PictureDetailPage /> },
      { path: 'picture/upload', element: <PictureUploadPage /> },
      { path: 'picture/search', element: <PictureSearchPage /> },
      { path: 'space/manage', element: <SpaceManagePage /> },
      { path: 'space/:spaceId', element: <SpacePicturePage /> },
      { path: 'space/:spaceId/members', element: <SpaceMemberPage /> },
      { path: 'space/:spaceId/analyze', element: <SpaceAnalyzePage /> },
      {
        path: 'admin/rank',
        element: (
          <RequireAuth requiredRole={['admin', 'super_admin']}>
            <SpaceRankPage />
          </RequireAuth>
        ),
      },
      {
        path: 'admin/user',
        element: (
          <RequireAuth requiredRole="super_admin">
            <UserRolePage />
          </RequireAuth>
        ),
      },
    ],
  },
  { path: '*', element: <Navigate to="/" replace /> },
])

export default router
