import { create } from 'zustand'
import type { UserVO } from '../types/user'
import { getLoginUserApi, loginApi, logoutApi } from '../api/user'
import { clearToken, getToken, setToken } from '../utils/token'

// 用户状态
interface UserState {
  token: string
  loginUser: UserVO | null
  login: (userAccount: string, userPassword: string) => Promise<void>
  fetchLoginUser: () => Promise<void>
  logout: () => Promise<void>
  reset: () => void
}

// 用户 store：管理登录态与当前用户信息
export const useUserStore = create<UserState>()((set) => ({
  // 初始化时从 localStorage 恢复 token
  token: getToken(),
  loginUser: null,

  // 登录：拆出 token 与脱敏信息，loginUser 仅存脱敏信息
  async login(userAccount, userPassword) {
    const res = await loginApi({ userAccount, userPassword }) // LoginUserVO（含 token）
    const { token, ...user } = res // 分离 token 与 UserVO
    setToken(token)
    set({ token, loginUser: user })
  },

  // 拉取当前登录用户信息
  async fetchLoginUser() {
    const user = await getLoginUserApi() // UserVO
    set({ loginUser: user })
  },

  // 退出登录：调用接口并清理本地状态
  async logout() {
    try {
      await logoutApi()
    } finally {
      clearToken()
      set({ token: '', loginUser: null })
    }
  },

  // 重置登录态（未登录跳转时使用）
  reset() {
    clearToken()
    set({ token: '', loginUser: null })
  },
}))
