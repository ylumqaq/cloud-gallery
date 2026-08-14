import { RouterProvider } from 'react-router-dom'
import router from './router'

// 根组件：渲染路由
export default function App() {
  return <RouterProvider router={router} />
}
